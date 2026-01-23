package com.example.excelrag.service;

import com.example.excelrag.model.excel.ExcelCellData;
import com.example.excelrag.model.excel.TextChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextChunkService {

    public List<TextChunk> chunkByRow(List<ExcelCellData> cellDataList, String fileName) {
        Map<String, List<ExcelCellData>> rowMap = new HashMap<>();
        
        for (ExcelCellData cellData : cellDataList) {
            String key = cellData.getSheetName() + "_" + cellData.getRowNum();
            rowMap.computeIfAbsent(key, k -> new ArrayList<>()).add(cellData);
        }
        
        List<TextChunk> chunks = new ArrayList<>();
        for (Map.Entry<String, List<ExcelCellData>> entry : rowMap.entrySet()) {
            List<ExcelCellData> rowCells = entry.getValue();
            if (rowCells.isEmpty()) {
                continue;
            }
            
            ExcelCellData firstCell = rowCells.get(0);
            StringBuilder contentBuilder = new StringBuilder();
            
            contentBuilder.append("工作表: ").append(firstCell.getSheetName()).append("\n");
            contentBuilder.append("行号: ").append(firstCell.getRowNum()).append("\n");
            contentBuilder.append("数据: ");
            
            for (ExcelCellData cell : rowCells) {
                contentBuilder.append("[列").append(cell.getColNum()).append("]: ")
                        .append(cell.getCellValue()).append(" ");
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sheetName", firstCell.getSheetName());
            metadata.put("rowNum", firstCell.getRowNum());
            metadata.put("fileName", fileName);
            metadata.put("cellCount", rowCells.size());
            
            TextChunk chunk = new TextChunk();
            chunk.setContent(contentBuilder.toString());
            chunk.setMetadata(metadata);
            chunks.add(chunk);
        }
        
        return chunks;
    }
}
