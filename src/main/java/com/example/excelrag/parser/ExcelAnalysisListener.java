package com.example.excelrag.parser;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.excelrag.model.excel.ExcelCellData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelAnalysisListener extends AnalysisEventListener<Map<Integer, String>> {

    private final List<ExcelCellData> cellDataList;
    private final String fileName;
    private String currentSheetName;
    private int currentRowNum;
    private Map<Integer, String> currentHeadMap;

    public ExcelAnalysisListener(String fileName) {
        this.cellDataList = new ArrayList<>();
        this.fileName = fileName;
        this.currentRowNum = 0;
        this.currentHeadMap = new java.util.HashMap<>();
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.currentSheetName = context.readSheetHolder().getSheetName();
        this.currentHeadMap = new java.util.HashMap<>(headMap);
    }

    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        this.currentRowNum = context.readRowHolder().getRowIndex() + 1;
        this.currentSheetName = context.readSheetHolder().getSheetName();
        
        for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
            String cellValue = entry.getValue();
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                ExcelCellData cellData = new ExcelCellData();
                cellData.setFileName(fileName);
                cellData.setSheetName(currentSheetName);
                cellData.setRowNum(currentRowNum);
                cellData.setColNum(entry.getKey() + 1);
                String columnName = currentHeadMap.get(entry.getKey());
                cellData.setColumnName(columnName != null ? columnName : "");
                cellData.setCellValue(cellValue.trim());
                cellDataList.add(cellData);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    public List<ExcelCellData> getCellDataList() {
        return cellDataList;
    }
}
