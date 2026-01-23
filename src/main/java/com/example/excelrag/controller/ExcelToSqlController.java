package com.example.excelrag.controller;

import com.example.excelrag.service.ExcelToSqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel到SQL转换控制器
 * 提供Excel导入和数据查询的REST接口
 * 支持动态表名和表结构创建
 */
@Slf4j
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelToSqlController {

    private final ExcelToSqlService excelToSqlService;

    /**
     * 导入Excel文件到数据库
     *
     * @param filePath Excel文件路径
     * @param tableName 表名
     * @param headRowNumber 表头行数（有表头=1，无表头=0）
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importExcel(
            @RequestParam String filePath,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "1") int headRowNumber) {
        try {
            log.info("开始导入Excel文件：{} 到表：{}", filePath, tableName);
            long count = excelToSqlService.importExcelToDatabase(filePath, tableName, headRowNumber);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Excel文件导入成功");
            result.put("count", count);
            result.put("filePath", filePath);
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Excel文件导入失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Excel文件导入失败：" + e.getMessage());
            result.put("filePath", filePath);
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 清空指定表中的所有数据
     *
     * @param tableName 表名
     * @return 清空结果
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearTable(@RequestParam String tableName) {
        try {
            excelToSqlService.clearTable(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "表数据清空成功");
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("表数据清空失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "表数据清空失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除指定表
     *
     * @param tableName 表名
     * @return 删除结果
     */
    @DeleteMapping("/drop")
    public ResponseEntity<Map<String, Object>> dropTable(@RequestParam String tableName) {
        try {
            excelToSqlService.dropTable(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "表删除成功");
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("表删除失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "表删除失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取指定表中的记录总数
     *
     * @param tableName 表名
     * @return 记录总数
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTotalCount(@RequestParam String tableName) {
        try {
            long count = excelToSqlService.getTotalCount(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", count);
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取记录总数失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取记录总数失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取指定表的所有数据
     *
     * @param tableName 表名
     * @return 数据列表
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData(@RequestParam String tableName) {
        try {
            List<Map<String, Object>> data = excelToSqlService.getAllData(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("count", data.size());
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取数据失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
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
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchByColumn(
            @RequestParam String tableName,
            @RequestParam String columnName,
            @RequestParam String value) {
        try {
            List<Map<String, Object>> data = excelToSqlService.searchByColumn(tableName, columnName, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("count", data.size());
            result.put("tableName", tableName);
            result.put("columnName", columnName);
            result.put("value", value);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询数据失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "查询数据失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取所有表名
     *
     * @return 表名列表
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getAllTables() {
        try {
            List<String> tables = excelToSqlService.getAllTables();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", tables);
            result.put("count", tables.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取表名列表失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取表名列表失败：" + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 检查结果
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkTableExists(@RequestParam String tableName) {
        try {
            boolean exists = excelToSqlService.tableExists(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("exists", exists);
            result.put("tableName", tableName);
            result.put("message", exists ? "表存在" : "表不存在");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("检查表是否存在失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("exists", false);
            result.put("tableName", tableName);
            result.put("message", "检查表是否存在失败：" + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取指定表的结构信息
     *
     * @param tableName 表名
     * @return 列信息列表
     */
    @GetMapping("/structure")
    public ResponseEntity<Map<String, Object>> getTableStructure(@RequestParam String tableName) {
        try {
            List<Map<String, Object>> structure = excelToSqlService.getTableStructure(tableName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", structure);
            result.put("count", structure.size());
            result.put("tableName", tableName);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取表结构失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取表结构失败：" + e.getMessage());
            result.put("tableName", tableName);
            
            return ResponseEntity.badRequest().body(result);
        }
    }
}
