package com.example.excelrag.controller;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 10:25
 * @description: TODO
 */
import com.example.excelrag.service.ExcelToTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/excel")
public class ExcelTableController {

    private final ExcelToTableService excelToTableService;

    public ExcelTableController(ExcelToTableService excelToTableService) {
        this.excelToTableService = excelToTableService;
    }

    /**
     * 上传Excel自动建表并导入数据
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tableName", required = false) String tableName,
            @RequestParam(value = "sheetIndex", defaultValue = "0") int sheetIndex) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // 如果未指定表名，使用文件名
        String finalTableName = tableName != null ? tableName :
                file.getOriginalFilename().replaceAll("\\.xlsx?$", "");

        try {
            String createdTable = excelToTableService.processExcelAndTrain(file, finalTableName, sheetIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("tableName", createdTable);
            response.put("message", "Excel successfully imported and trained in SuperSQL");
            response.put("nextStep", "Use /api/chat/query to ask questions about this table");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", e.getMessage()));
    }
}