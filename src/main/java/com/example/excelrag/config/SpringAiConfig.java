package com.example.excelrag.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/26 17:13
 * @description: TODO
 */
@Primary
@Configuration
@RequiredArgsConstructor
public class SpringAiConfig {

    private final ApplicationConfig applicationConfig;

/*    @Bean
    public EmbeddingModel sharedLLMConfig() {
        return new OpenAiEmbeddingModel(OpenAiApi.builder()
                .baseUrl(applicationConfig.getBaiLianBaseUrl())
                .apiKey(applicationConfig.getBaiLianApiKey())
                .completionsPath("")
                .embeddingsPath("")
                .build());
    }*/

    //@Bean
    //@Primary  // ✅ 标记为主 Bean
    /*public EmbeddingModel sharedLLMConfig() {
        return new OpenAiEmbeddingModel(OpenAiApi.builder()
                .baseUrl(applicationConfig.getBaiLianBaseUrl())
                .apiKey(applicationConfig.getBaiLianApiKey())
                .embeddingsPath("")
                .build());
    }*/

}
