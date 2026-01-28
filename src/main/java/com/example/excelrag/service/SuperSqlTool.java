package com.example.excelrag.service;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 10:19
 * @description: TODO
 */

@Component
public class SuperSqlTool {
  //  private final SpringSqlEngine sqlEngine;


    /**
     * 将自然语言问题转换为SQL并执行
     *
     * @param question 用户自然语言查询，例如"查询销售额超过100万的客户"
     * @return 包含生成的SQL和查询结果
     */
    //@Tool("将自然语言转换为SQL查询并执行，支持复杂条件、聚合函数和多表关联")
    public Map<String, Object> executeNaturalLanguageQuery(String question) {
        // 生成SQL
/*        String sql = sqlEngine.generateSql(question);

        // 执行SQL并返回结果
        Object result = sqlEngine.executeSql(sql);*/

                String sql = "sqlEngine.generateSql(question)";

        // 执行SQL并返回结果
        Object result = "sqlEngine.executeSql(sql)";

        return Map.of(
                "generatedSql", sql,
                "result", result,
                "success", true
        );
    }

    /**
     * 仅生成SQL不执行（用于审查）
     */
    //@Tool("根据自然语言生成SQL语句，不执行查询")
    public String generateSqlOnly(String question) {
        return "sqlEngine.generateSql(question)";
    }



    //@Tool("执行SQL查询并处理错误")
    public Map<String, Object> safeExecuteQuery(String question) {
        try {
            String sql = "sqlEngine.generateSql(question)";
            // 安全校验：禁止危险操作
            if (sql.contains("DROP") || sql.contains("DELETE")) {
                return Map.of("error", "危险操作被拒绝", "sql", sql);
            }
            return Map.of("result", "sqlEngine.executeSql(sql)", "sql", sql);
        } catch (Exception e) {
            return Map.of("error", e.getMessage(), "suggestion", "请更清晰地描述您的查询需求");
        }
    }
}
