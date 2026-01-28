package com.example.excelrag.service;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2025/12/1 14:25
 * @description: 日历助手
 */
@ConditionalOnMissingBean(ChatDemoAssistant.class)
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "baiLianChatModel",
        streamingChatModel = "baiLianStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        //contentRetriever = "embeddingStoreContentRetriever"
        tools = {"embdSemanticConversion", "sqlTools","mcpToolProvider","dataVisualizationTool"}
)
public interface ChatDemoAssistant {

    /**
     * 普通输出，不是流式的
     */
    //@SystemMessage("你是一名医生，请用专业的医疗经验进行回答,今天是{{current_date}}")
    // 系统提示词  https://blog.csdn.net/2301_80454352/article/details/148211247
    @SystemMessage(fromResource = "prompt.txt")
    //系统提示词
    String chat(@V("data") String data, @MemoryId long memoryId, @UserMessage String userMessage);

    /**
     * 流式输出，是流式的
     * 注意：流式输出返回的要为 Flux<T> 类型的数据类型
     *
     * @return
     */
    @SystemMessage(fromResource = "prompt.txt")
    //系统提示词
    Flux<String> chatFlux(/*@V("data") Object data,*/ @MemoryId long memoryId, @UserMessage String userMessage);
}
