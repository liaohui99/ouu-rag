package com.example.excelrag.controller;

import com.example.excelrag.req.PromptReq;
import com.example.excelrag.service.ChatDemoAssistant;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;


@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    StreamingChatModel streamingChatModel;
    @Autowired
    ChatDemoAssistant chatDemoAssistant;
    @Autowired
    EmbeddingModel embeddingModel;
    @Autowired
    EmbeddingStore embeddingStore; 
    @Autowired
    ChatMemoryProvider chatMemoryProvider;

    /**
     * 流式聊天端点
     * 注意：在流式响应中，工具调用结果由 LLM 根据系统提示词决定是否包含
     * 如果需要确保工具调用结果（特别是图表HTML）被返回，请使用 /chat 端点
     * 
     * @param prompt 用户请求
     * @param response HTTP响应
     * @return 流式响应
     */
    @PostMapping("/stream")
    public Flux<String> stream(@RequestBody @Validated PromptReq prompt, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        logger.info("收到流式聊天请求，memoryId: {}, message: {}", 
                 prompt.getMemoryId(), prompt.getUserMessage());
        ChatMemory chatMemory = chatMemoryProvider.get(prompt.getMemoryId());
        List<ChatMessage> messages = chatMemory.messages();
        // 1. 检索相关向量
        /*Response<Embedding> queryEmbedding = embeddingModel.embed(prompt.getUserMessage());
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding.content())
                .maxResults(100)
                .minScore(0.6)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(request);
        List<EmbeddingMatch<TextSegment>> relevantMatches = searchResult.matches();

        // 2. 构建上下文
        StringBuilder context = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : relevantMatches) {
            if (match.embedded() != null) {
                context.append("- ").append(match.embedded().text()).append("\n");
            }
        }*/

        return chatDemoAssistant.chatFlux(prompt.getMemoryId(), prompt.getUserMessage())
                .doOnComplete(() -> {
                    logger.info("流式聊天完成");
                })
                .doOnError(error -> {
                    logger.error("流式聊天出错", error);
                });
/*
        return Flux.create(sink -> streamingChatModel.chat(userMessage, new StreamingChatResponseHandler() {


            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {

            }

            @Override
            public void onError(Throwable error) {

            }
        }));*/
    }

    /**
     * 非流式聊天端点，用于处理需要工具调用的请求
     * 确保工具调用结果（特别是图表HTML）被正确返回
     * 
     * @param prompt 用户请求
     * @param response HTTP响应
     * @return 完整的响应，包含工具调用结果
     */
    @PostMapping("/chat")
    public String chat(@RequestBody @Validated PromptReq prompt, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        
        logger.info("收到非流式聊天请求，memoryId: {}, message: {}", 
                 prompt.getMemoryId(), prompt.getUserMessage());
        
        try {
            // 使用非流式方法，确保工具调用结果被正确返回
            String result = chatDemoAssistant.chat(null, prompt.getMemoryId(), prompt.getUserMessage());
            
            // 检查响应中是否包含HTML
            if (result != null && (result.contains("<!DOCTYPE html>") || result.contains("<html>"))) {
                logger.info("响应包含HTML内容，长度: {} 字符", result.length());
            } else {
                logger.warn("响应可能不包含工具调用结果");
            }
            
            return result;
        } catch (Exception e) {
            logger.error("处理聊天请求失败", e);
            return "处理请求时出错: " + e.getMessage();
        }
    }
}
