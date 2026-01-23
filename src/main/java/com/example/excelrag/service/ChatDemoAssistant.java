package com.example.excelrag.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2025/12/1 14:25
 * @description: 日历助手
 */
@AiService(
        wiringMode = AiServiceWiringMode.AUTOMATIC,
        chatModel = "baiLianChatModel",
        streamingChatModel="baiLianStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
       contentRetriever = "embeddingStoreContentRetriever"
)
public interface ChatDemoAssistant {

    /**
     * 普通输出，不是流式的
     */
    //@SystemMessage("你是一名医生，请用专业的医疗经验进行回答,今天是{{current_date}}")
    // 系统提示词  https://blog.csdn.net/2301_80454352/article/details/148211247
    @SystemMessage(fromResource = "prompt.txt")     //系统提示词
    String chat(@V("data") String data,@MemoryId long memoryId, @UserMessage String userMessage);

    /**
     * 流式输出，是流式的
     * 注意：流式输出返回的要为 Flux<T> 类型的数据类型
     * @return
     */
    @SystemMessage(fromResource = "prompt.txt")     //系统提示词
    Flux<String> chatFlux(/*@V("data") Object data,*/ @MemoryId long memoryId, @UserMessage String userMessage);
}
