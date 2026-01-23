package com.example.excelrag.controller;

import com.example.excelrag.model.dto.DocumentChunk;
import com.example.excelrag.model.dto.RAGRequest;
import com.example.excelrag.model.dto.RAGResponse;
import com.example.excelrag.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RAGController {

    @Autowired
    private RAGService ragService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            String result = ragService.processExcelFile(file);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("vectorCount", ragService.getVectorStoreSize());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文件处理失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/query")
    public ResponseEntity<RAGResponse> query(@RequestBody RAGRequest request) {
        try {
            RAGResponse response = ragService.query(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RAGResponse errorResponse = new RAGResponse();
            errorResponse.setAnswer("查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("vectorCount", ragService.getVectorStoreSize());
        status.put("status", "running");
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearVectorStore() {
        String result = ragService.clearVectorStore();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}
