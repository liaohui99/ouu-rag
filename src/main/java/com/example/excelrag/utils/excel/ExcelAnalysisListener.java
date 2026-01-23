package com.example.excelrag.utils.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.excelrag.model.excel.ExcelCellData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel解析监听器（EasyExcel核心回调类）
 * ✅ 逐行解析 | ✅ 过滤空单元格 | ✅ 封装ExcelCellData | ✅ 记录工作表元数据
 */
public class ExcelAnalysisListener extends AnalysisEventListener<Map<String, Object>> {
    // 存储所有有效单元格数据
    private final List<ExcelCellData> cellDataList = new ArrayList<>();
    // 当前解析的工作表名称
    private String currentSheetName;

    /**
     * 核心回调：逐行解析Excel数据（每行触发一次）
     */
    @Override
    public void invoke(Map<String, Object> rowDataMap, AnalysisContext context) {
        // 1. 获取元数据：工作表名、当前行号（+1 匹配Excel视觉行号）
        currentSheetName = context.readSheetHolder().getSheetName();
        int rowNum = context.readRowHolder().getRowIndex() + 1;

        // 2. 遍历当前行所有单元格，过滤空值并封装实体
        int colNum = 1; // 列号从1开始计数，符合使用习惯
        for (Map.Entry<String, Object> entry : rowDataMap.entrySet()) {
            Object cellValueObj = entry.getValue();
            // 跳过空单元格，避免无效数据占用内存
            if (cellValueObj == null || cellValueObj.toString().trim().isEmpty()) {
                colNum++;
                continue;
            }

            // 3. 封装单元格数据+全量元数据
            String cellValue = cellValueObj.toString().trim();
            ExcelCellData cellData = new ExcelCellData();
            cellData.setSheetName(currentSheetName);
            cellData.setRowNum(rowNum);
            cellData.setColNum(colNum);
            cellData.setCellValue(cellValue);

            cellDataList.add(cellData);
            colNum++;
        }
    }

    /**
     * 解析完成回调：所有工作表解析完毕后触发
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.printf("✅ Excel解析完成 | 工作表：%s | 提取有效单元格数：%d%n",
                currentSheetName, cellDataList.size());
    }

    // 获取最终解析结果
    public List<ExcelCellData> getCellDataList() {
        return cellDataList;
    }
}

