package com.example.excelrag.tools;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.example.excelrag.service.EmbeddingChat;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.V;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 13:35
 * @description: TODO
 */
@Component
@RequiredArgsConstructor
public class EmbdSemanticConversion {

    private final ChatModel semanticConversionChatModel;
    private final EmbeddingModel embeddingModel;
    private final ChatMemoryProvider chatMemoryProvider;

    @Tool(name = "semanticConversion", value = "将用户的问题转换为最适合向量搜索的简短表达形式")
    public String semanticConversion(@P("用户原始问题") String question) {
        String prompt = """
                你是一个查询优化专家。请将用户的问题转换为最适合向量搜索的简短表达形式。
                要求：
                1. 提取核心关键词（实体、概念、动作）
                2. 保留关键约束条件（时间、地点、数值）
                3. 使用名词和动词短语，避免完整句式
                4. 去除冗余词、疑问词和语气词
                5. 用逗号或空格分隔关键词
                            
                用户问题："%s"
                            
                优化后的查询词：
                """.formatted(question);
        return semanticConversionChatModel.chat(prompt);
    }

    @Tool(name = "generateHypotheticalAnswer", value = "生成一个假设性的答案来回应用户问题")
    public String generateHypotheticalAnswer(@P("用户原始问题") String userQuestion) {
        String prompt = """
                请基于你的知识，生成一个假设性的答案来回应用户问题。
                这个答案应该包含相关的事实、细节和关键词，格式为简洁的段落。
                            
                用户问题："%s"
                            
                假设性答案：
                """.formatted(userQuestion);
        return semanticConversionChatModel.chat(prompt);
    }


    @Tool(name = "contextualTransform", value = "基于以下对话历史和当前问题，生成优化的搜索向量的查询词")
    public String contextualTransform(@P("用户原始问题") String currentQuery, @ToolMemoryId Object memoryId) {
        ChatMemory chatMemory = chatMemoryProvider.get(memoryId);
        List<ChatMessage> history = chatMemory.messages();
        if (CollUtil.isEmpty(history)) {
            return "暂无历史对话";
        }
        String context = extractRelevantHistory(history);
        String prompt = """
                基于以下对话历史和当前问题，生成优化的搜索向量的查询词：
                        
                历史对话：%s
                当前问题：%s
                        
                优化查询：
                """.formatted(context, currentQuery);
        return semanticConversionChatModel.chat(prompt);
    }

    private String extractRelevantHistory(List<ChatMessage> history) {
        return JSON.toJSONString(history);
    }

    // 使用时：将生成的假设答案而非原问题进行向量化
    public Embedding createSearchEmbedding(String userQuestion) {
        String hypotheticalAnswer = generateHypotheticalAnswer(userQuestion);
        return embeddingModel.embed(hypotheticalAnswer).content();
    }


}
