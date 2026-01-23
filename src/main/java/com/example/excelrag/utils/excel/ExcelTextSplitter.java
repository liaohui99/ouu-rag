package com.example.excelrag.utils.excel;

import com.example.excelrag.model.excel.ExcelCellData;
import com.example.excelrag.model.excel.TextChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExcelTextSplitter {

    public List<TextChunk> splitByRow(List<ExcelCellData> cellDataList) {
        Map<String, Map<String, Map<Integer, StringBuilder>>> fileSheetRowMap = new HashMap<>(16);
        Map<String, Map<String, Map<Integer, List<String>>>> fileSheetRowColumnMap = new HashMap<>(16);
        List<TextChunk> textChunks = new ArrayList<>();

        cellDataList.forEach(cellData -> {
            String fileName = cellData.getFileName();
            String sheetName = cellData.getSheetName();
            Integer rowNum = cellData.getRowNum();
            fileSheetRowMap.computeIfAbsent(fileName, k -> new HashMap<>(16));
            Map<String, Map<Integer, StringBuilder>> sheetRowMap = fileSheetRowMap.get(fileName);
            sheetRowMap.computeIfAbsent(sheetName, k -> new HashMap<>(32));
            StringBuilder rowText = sheetRowMap.get(sheetName)
                    .computeIfAbsent(rowNum, k -> new StringBuilder());
            
            fileSheetRowColumnMap.computeIfAbsent(fileName, k -> new HashMap<>(16));
            Map<String, Map<Integer, List<String>>> sheetRowColumnMap = fileSheetRowColumnMap.get(fileName);
            sheetRowColumnMap.computeIfAbsent(sheetName, k -> new HashMap<>(32));
            List<String> columnNames = sheetRowColumnMap.get(sheetName)
                    .computeIfAbsent(rowNum, k -> new ArrayList<>());
            
            String columnName = cellData.getColumnName();
            if (columnName != null && !columnName.isEmpty()) {
                rowText.append(columnName).append(":").append(cellData.getCellValue()).append("; ");
                columnNames.add(columnName);
            } else {
                rowText.append("列").append(cellData.getColNum())
                        .append(":").append(cellData.getCellValue()).append("; ");
            }
        });

        fileSheetRowMap.forEach((fileName, sheetRowMap) -> {
            sheetRowMap.forEach((sheetName, rowMap) -> {
                rowMap.forEach((rowNum, rowText) -> {
                    List<String> currentColumnNames = fileSheetRowColumnMap.get(fileName).get(sheetName).get(rowNum);
                    /*String columnNamesStr = "";
                    if (currentColumnNames != null && !currentColumnNames.isEmpty()) {
                        columnNamesStr = " 列名: " + String.join(", ", currentColumnNames);
                        rowText.append(columnNamesStr);
                    }*/
                    String fullContent = rowText.toString();
                    String chunkText = String.format("文件[%s] 工作表[%s] 行[%d] 内容: %s",
                            fileName, sheetName, rowNum, fullContent);

                    TextChunk chunk = new TextChunk();
                    chunk.setContent(chunkText);
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("fileName", fileName);
                    metadata.put("sheetName", sheetName);
                    metadata.put("rowNum", rowNum.toString());
                    metadata.put("fileType", "excel");
                    metadata.put("dataSource", "excel-row");
                    
                    /*if (currentColumnNames != null && !currentColumnNames.isEmpty()) {
                        metadata.put("columnNames", currentColumnNames);
                    }*/
                    
                    chunk.setMetadata(metadata);
                    textChunks.add(chunk);
                });
            });
        });
        return textChunks;
    }
}
