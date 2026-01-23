package com.example.excelrag.model.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Excel行级文本块实体
 * 一行所有单元格数据聚合后的结构化文本，用于向量化
 */
public class TextChunk {
    /** 行级聚合文本内容（结构化格式，提升向量化准确性） */
    private String content;
    /** 元数据（工作表名、行号、文件类型，用于ES存储/检索溯源） */
    private Map<String, Object> metadata;
}
