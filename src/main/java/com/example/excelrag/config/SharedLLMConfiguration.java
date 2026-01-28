package com.example.excelrag.config;

import dev.langchain4j.model.openai.OpenAiChatModelName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 11:18
 * @description: TODO
 */
@Configuration
@RequiredArgsConstructor
public class SharedLLMConfiguration {


    private final ApplicationConfig applicationConfig;

    /**
     * 创建 Spring AI 的 ChatModel（供 SuperSQL 使用）
     */
    /*@Bean
    public ChatModel springAiChatModel() {
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(applicationConfig.getBaiLianBaseUrl())
                        .apiKey(applicationConfig.getBaiLianApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder().model(applicationConfig.getBaiLianModelName()).build())
                .build();
    }*/


}
