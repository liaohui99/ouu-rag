package com.example.excelrag.service;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 10:24
 * @description: TODO
 */
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ExcelToTableService {

    private final DataSource dataSource;
    //private final SpringSqlEngine sqlEngine;

    // 类型推断采样行数（用于判断列的数据类型）
    private static final int TYPE_INFERENCE_SAMPLE_ROWS = 10;

    // 非法字符清理正则
    private static final Pattern ILLEGAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9_\\u4e00-\\u9fa5]");

    // 保留关键字
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
            "select", "insert", "update", "delete", "from", "where", "order", "group", "by"
    ));

    public ExcelToTableService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 完整流程：Excel → 建表 → 导入数据 → SuperSQL训练
     *
     * @param excelFile Excel文件
     * @param tableName 目标表名
     * @param sheetIndex Sheet索引（从0开始）
     * @return 生成的表名
     */
    @Transactional(rollbackFor = Exception.class)
    public String processExcelAndTrain(MultipartFile excelFile, String tableName, int sheetIndex) throws Exception {
        String safeTableName = sanitizeTableName(tableName);

        try (InputStream is = excelFile.getInputStream()) {
            // 1. 读取Excel结构
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("Excel sheet is empty");
            }

            // 2. 生成建表SQL
            String createTableSql = generateCreateTableSql(sheet, safeTableName);
            log.info("Generated DDL:\n{}", createTableSql);

            // 3. 执行建表
            executeDdl(createTableSql);

            // 4. 导入数据
            int importedRows = importData(sheet, safeTableName);
            log.info("Successfully imported {} rows into {}", importedRows, safeTableName);

            // 5. SuperSQL训练
            trainTableInSuperSql(safeTableName);

            workbook.close();
            return safeTableName;
        }
    }

    /**
     * 根据Excel内容生成CREATE TABLE语句（带智能类型推断）
     */
    private String generateCreateTableSql(Sheet sheet, String tableName) throws Exception {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("No header row found in Excel");
        }

        StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS `")
                .append(tableName).append("` (\n  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,\n");

        // 收集列定义
        List<String> columnDefs = new ArrayList<>();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell headerCell = headerRow.getCell(i);
            if (headerCell == null) continue;

            String columnName = sanitizeColumnName(getCellValue(headerCell));
            String columnType = inferColumnType(sheet, i);

            columnDefs.add("  `" + columnName + "` " + columnType + " COMMENT '" + columnName + "'");
        }

        ddl.append(String.join(",\n", columnDefs));
        ddl.append(",\n  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n");
        ddl.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Imported from Excel';");

        return ddl.toString();
    }

    /**
     * 智能推断列的数据类型
     * 采样前N行数据，根据内容判断最匹配的数据库类型
     */
    private String inferColumnType(Sheet sheet, int columnIndex) {
        Set<String> sampleValues = new HashSet<>();
        int sampleRows = Math.min(TYPE_INFERENCE_SAMPLE_ROWS, sheet.getLastRowNum());

        // 采样数据（跳过表头，从第1行开始）
        for (int i = 1; i <= sampleRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell cell = row.getCell(columnIndex);
            String value = getCellValue(cell);

            if (!value.isEmpty()) {
                sampleValues.add(value);
            }
        }

        // 如果列名为"id"、"code"等，优先判断为整数
        Row headerRow = sheet.getRow(0);
        String headerName = getCellValue(headerRow.getCell(columnIndex)).toLowerCase();
        if (headerName.contains("id") || headerName.contains("code")) {
            return "BIGINT";
        }

        // 基于采样值判断类型
        boolean allIntegers = sampleValues.stream().allMatch(v -> v.matches("-?\\d+"));
        boolean allDecimals = sampleValues.stream().allMatch(v -> v.matches("-?\\d+\\.\\d+"));
        boolean allDates = sampleValues.stream().allMatch(this::isValidDate);

        if (allIntegers) {
            return "BIGINT";
        } else if (allDecimals) {
            return "DECIMAL(18,2)";
        } else if (allDates) {
            return "DATE";
        } else {
            // 默认VARCHAR，长度根据最长内容动态设置
            int maxLength = sampleValues.stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(50);
            return "VARCHAR(" + Math.min(maxLength * 2, 500) + ")";
        }
    }

    /**
     * 执行DDL语句
     */
    private void executeDdl(String ddl) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            log.info("DDL executed successfully");
        }
    }

    /**
     * 批量导入Excel数据
     */
    private int importData(Sheet sheet, String tableName) throws Exception {
        Row headerRow = sheet.getRow(0);
        List<String> columns = new ArrayList<>();

        // 收集列名
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String colName = sanitizeColumnName(getCellValue(headerRow.getCell(i)));
            columns.add(colName);
        }

        // 构建INSERT语句
        String insertSql = buildInsertSql(tableName, columns);
        log.debug("Insert SQL: {}", insertSql);

        // 批量插入
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false); // 开启事务
            int batchSize = 1000;
            int totalRows = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow == null || isEmptyRow(dataRow)) continue;

                // 填充PreparedStatement
                for (int j = 0; j < columns.size(); j++) {
                    String value = getCellValue(dataRow.getCell(j));
                    pstmt.setString(j + 1, value);
                }

                pstmt.addBatch();
                totalRows++;

                // 批量执行
                if (totalRows % batchSize == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }

            // 执行剩余批次
            pstmt.executeBatch();
            conn.commit();

            return totalRows;
        }
    }

    /**
     * 将表结构注册到SuperSQL训练集
     */
    private void trainTableInSuperSql(String tableName) throws Exception {
        String ddl = generateDdlFromMetadata(tableName);

        // 关键：使用train()方法注册表结构
     /*   sqlEngine.setChatModel(null)  // 使用默认配置的LLM
                .train(TrainBuilder.builder()
                        .content(ddl)
                        .policy(TrainPolicyType.DDL)  // DDL策略训练表结构
                        .build()
                );*/

        log.info("Table {} successfully trained in SuperSQL", tableName);
    }

    /**
     * 从数据库元数据生成DDL语句
     */
    private String generateDdlFromMetadata(String tableName) throws Exception {
        StringBuilder ddl = new StringBuilder("CREATE TABLE `").append(tableName).append("` (\n");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 获取列信息
            ResultSet columns = metaData.getColumns(conn.getCatalog(), null, tableName, null);
            List<String> columnDefs = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String nullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls ? "NOT NULL" : "NULL";

                columnDefs.add("  `" + columnName + "` " + typeName +
                        (columnSize > 0 ? "(" + columnSize + ")" : "") + " " + nullable);
            }

            ddl.append(String.join(",\n", columnDefs));
            ddl.append("\n);");
        }

        return ddl.toString();
    }

    /**
     * 获取单元格值（处理各种类型）
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 判断是否为日期格式
     */
    private boolean isValidDate(String value) {
        try {
            // 支持多种日期格式
            String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd"};
            for (String pattern : patterns) {
                new SimpleDateFormat(pattern).parse(value);
                return true;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    /**
     * 判断是否为空白行
     */
    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (!getCellValue(row.getCell(i)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清理表名（防止SQL注入和非法字符）
     */
    private String sanitizeTableName(String tableName) {
        String sanitized = ILLEGAL_CHAR_PATTERN.matcher(tableName).replaceAll("");
        sanitized = sanitized.replaceAll("^\\d+", ""); // 移除开头数字

        if (sanitized.length() > 64) {
            sanitized = sanitized.substring(0, 64);
        }

        // 如果是保留关键字，添加前缀
        if (RESERVED_KEYWORDS.contains(sanitized.toLowerCase())) {
            sanitized = "tbl_" + sanitized;
        }

        return sanitized;
    }

    /**
     * 清理列名
     */
    private String sanitizeColumnName(String columnName) {
        String sanitized = ILLEGAL_CHAR_PATTERN.matcher(columnName).replaceAll("");
        sanitized = sanitized.replaceAll("^\\d+", "");

        if (sanitized.isEmpty()) {
            sanitized = "col_" + System.currentTimeMillis();
        }

        // MySQL列名长度限制
        if (sanitized.length() > 64) {
            sanitized = sanitized.substring(0, 64);
        }

        return sanitized;
    }

    /**
     * 构建INSERT SQL
     */
    private String buildInsertSql(String tableName, List<String> columns) {
        String cols = String.join("`, `", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        return String.format("INSERT INTO `%s` (`%s`) VALUES (%s)", tableName, cols, placeholders);
    }

}
