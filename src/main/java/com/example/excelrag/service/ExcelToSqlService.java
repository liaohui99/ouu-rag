package com.example.excelrag.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

/**
 * Excel到SQL转换服务
 * 负责将Excel文件数据读取并存储到H2数据库
 * 支持动态表名和表结构创建
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelToSqlService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Excel数据读取包装类
     */
    private static class ExcelDataWrapper {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        boolean tableChecked = false;
        String tableName;
        int headRowNumber;
    }

    /**
     * 读取Excel文件并保存到数据库
     *
     * @param filePath Excel文件路径
     * @param tableName 表名
     * @param headRowNumber 表头行数（有表头=1，无表头=0）
     * @return 成功导入的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int importExcelToDatabase(String filePath, String tableName, int headRowNumber) {
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            throw new RuntimeException("Excel文件不存在：" + filePath);
        }

        final ExcelDataWrapper wrapper = new ExcelDataWrapper();
        wrapper.tableName = tableName;
        wrapper.headRowNumber = headRowNumber;
        
        try {
            EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
                
                private boolean isFirstRow = true;
                
                /**
                 * 每读取一行数据都会调用此方法
                 *
                 * @param data 读取到的数据
                 * @param context 分析上下文
                 */
                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    if (isFirstRow && wrapper.headRowNumber > 0) {
                        wrapper.columnNames.addAll(data.values());
                        isFirstRow = false;
                        
                        if (!wrapper.tableChecked) {
                            wrapper.tableChecked = true;
                            if (!tableExists(wrapper.tableName)) {
                                createTable(wrapper.tableName, wrapper.columnNames);
                                log.info("表{}不存在，已自动创建", wrapper.tableName);
                            }
                        }
                        return;
                    }
                    
                    Map<String, Object> rowData = new HashMap<>();
                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        String columnName = wrapper.columnNames.size() > entry.getKey() ? wrapper.columnNames.get(entry.getKey()) : "column_" + entry.getKey();
                        rowData.put(columnName, entry.getValue());
                    }
                    wrapper.dataList.add(rowData);
                    
                    if (wrapper.dataList.size() >= 1000) {
                        saveDataList(wrapper.tableName, wrapper.dataList);
                        wrapper.dataList.clear();
                    }
                }

                /**
                 * 所有数据读取完成后调用此方法
                 *
                 * @param context 分析上下文
                 */
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    saveDataList(wrapper.tableName, wrapper.dataList);
                    wrapper.dataList.clear();
                }
            }).headRowNumber(headRowNumber).sheet().doRead();

            log.info("Excel文件导入成功，共导入{}条记录到表{}", getTotalCount(tableName), tableName);
            return (int) getTotalCount(tableName);
            
        } catch (Exception e) {
            log.error("Excel文件导入失败：{}", e.getMessage(), e);
            throw new RuntimeException("Excel文件导入失败：" + e.getMessage(), e);
        }
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 表是否存在
     */
    public boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName.toUpperCase());
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查表是否存在失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param columnNames 列名列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTable(String tableName, List<String> columnNames) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(tableName).append(" (");
            sql.append("id BIGINT AUTO_INCREMENT PRIMARY KEY, ");
            
            for (int i = 0; i < columnNames.size(); i++) {
                String columnName = columnNames.get(i);
                if (columnName == null || columnName.trim().isEmpty()) {
                    columnName = "column_" + i;
                }
                sql.append(columnName).append(" VARCHAR(500)");
                if (i < columnNames.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            
            jdbcTemplate.execute(sql.toString());
            log.info("成功创建表：{}", tableName);
        } catch (Exception e) {
            log.error("创建表失败：{}", e.getMessage(), e);
            throw new RuntimeException("创建表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量保存数据到数据库
     *
     * @param tableName 表名
     * @param dataList 待保存的数据列表
     */
    @Transactional(rollbackFor = Exception.class)
    private void saveDataList(String tableName, List<Map<String, Object>> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        
        try {
            if (!tableExists(tableName)) {
                if (!dataList.isEmpty()) {
                    List<String> columnNames = new ArrayList<>(dataList.get(0).keySet());
                    createTable(tableName, columnNames);
                } else {
                    throw new RuntimeException("无法创建表：数据为空");
                }
            }
            
            for (Map<String, Object> data : dataList) {
                insertData(tableName, data);
            }
            
            log.info("成功保存{}条数据到表{}", dataList.size(), tableName);
        } catch (Exception e) {
            log.error("保存数据到数据库失败：{}", e.getMessage(), e);
            throw new RuntimeException("保存数据到数据库失败：" + e.getMessage(), e);
        }
    }

    /**
     * 插入单条数据
     *
     * @param tableName 表名
     * @param data 数据
     */
    private void insertData(String tableName, Map<String, Object> data) {
        if (data.isEmpty()) {
            return;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        
        List<String> columns = new ArrayList<>(data.keySet());
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") VALUES (");
        
        for (int i = 0; i < columns.size(); i++) {
            sql.append("?");
            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        
        Object[] values = columns.stream().map(data::get).toArray();
        jdbcTemplate.update(sql.toString(), values);
    }

    /**
     * 清空指定表中的所有数据
     *
     * @param tableName 表名
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearTable(String tableName) {
        try {
            if (tableExists(tableName)) {
                jdbcTemplate.execute("DELETE FROM " + tableName);
                log.info("已清空表{}中的所有数据", tableName);
            } else {
                log.warn("表{}不存在，无法清空数据", tableName);
            }
        } catch (Exception e) {
            log.error("清空表数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("清空表数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除指定表
     *
     * @param tableName 表名
     */
    @Transactional(rollbackFor = Exception.class)
    public void dropTable(String tableName) {
        try {
            if (tableExists(tableName)) {
                jdbcTemplate.execute("DROP TABLE " + tableName);
                log.info("已删除表：{}", tableName);
            } else {
                log.warn("表{}不存在，无法删除", tableName);
            }
        } catch (Exception e) {
            log.error("删除表失败：{}", e.getMessage(), e);
            throw new RuntimeException("删除表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表中的记录总数
     *
     * @param tableName 表名
     * @return 记录总数
     */
    public long getTotalCount(String tableName) {
        try {
            if (!tableExists(tableName)) {
                return 0;
            }
            String sql = "SELECT COUNT(*) FROM " + tableName;
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取记录总数失败：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取指定表的所有数据
     *
     * @param tableName 表名
     * @return 数据列表
     */
    public List<Map<String, Object>> getAllData(String tableName) {
        try {
            if (!tableExists(tableName)) {
                return new ArrayList<>();
            }
            String sql = "SELECT * FROM " + tableName;
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("获取数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询数据
     *
     * @param tableName 表名
     * @param columnName 列名
     * @param value 值
     * @return 数据列表
     */
    public List<Map<String, Object>> searchByColumn(String tableName, String columnName, String value) {
        try {
            if (!tableExists(tableName)) {
                return new ArrayList<>();
            }
            String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
            return jdbcTemplate.queryForList(sql, value);
        } catch (Exception e) {
            log.error("查询数据失败：{}", e.getMessage(), e);
            throw new RuntimeException("查询数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取所有表名
     *
     * @return 表名列表
     */
    public List<String> getAllTables() {
        try {
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.error("获取表名列表失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取表名列表失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取指定表的结构信息
     *
     * @param tableName 表名
     * @return 列信息列表
     */
    public List<Map<String, Object>> getTableStructure(String tableName) {
        try {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS " +
                       "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'PUBLIC' ORDER BY ORDINAL_POSITION";
            return jdbcTemplate.queryForList(sql, tableName.toUpperCase());
        } catch (Exception e) {
            log.error("获取表结构失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取表结构失败：" + e.getMessage(), e);
        }
    }
}
