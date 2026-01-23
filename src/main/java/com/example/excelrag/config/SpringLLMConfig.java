package com.example.excelrag.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * LLM配置类
 * 配置LangChain4j相关的Bean
 */
@Configuration
@RequiredArgsConstructor
public class SpringLLMConfig {

    private final ApplicationConfig applicationConfig;


    /**
     * 创建流式对话模型Bean
     *
     * @return StreamingChatModel
     */
    @Bean
    public StreamingChatModel baiLianStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                /*.baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")*/
                .baseUrl(applicationConfig.getBaiLianBaseUrl())
                .apiKey(applicationConfig.getBaiLianApiKey())
                .modelName(applicationConfig.getBaiLianModelName())
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("BAI_LIAN_API_KEY"))
                .modelName("text-embedding-v4")
                .maxRetries(1000)
                .build();
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return QdrantEmbeddingStore.builder()
                .host("192.168.111.10")
                .port(6334)  // gRPC端口
                .collectionName("ouu-rag-test")
                .client(new QdrantClient(QdrantGrpcClient.newBuilder("192.168.111.11", 6334, false).build()))
                .build();
    }


    /**
     * 创建普通对话模型Bean
     *
     * @return ChatModel
     */
    @Bean
    public ChatModel baiLianChatModel() {
        return OpenAiChatModel.builder()
/*                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")*/
                .baseUrl(applicationConfig.getBaiLianBaseUrl())
                .apiKey(applicationConfig.getBaiLianApiKey())
                .modelName(applicationConfig.getBaiLianApiKey())
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .build();
    }


    // 注释掉 windowChatMemory Bean，因为在 @AiService 注解中同时指定了 chatMemory 和 chatMemoryProvider 时，chatMemoryProvider 会被忽略
    @Bean
    public ChatMemory windowChatMemory() {
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 创建MySQL会话消息存储Bean
     * 使用MysqlChatMemoryStore替代InMemoryChatMemoryStore，实现会话持久化
     *
     * @param mysqlChatMemoryStore MySQL会话消息存储
     * @return ChatMemoryStore
     */
//    @Bean
//    public ChatMemoryStore chatMemoryStore(MysqlChatMemoryStore mysqlChatMemoryStore) {
//        return mysqlChatMemoryStore;
//    }


    /**
     * 创建会话内存提供者Bean
     *
     * @param persistentChatMemoryStore 会话消息存储
     * @return ChatMemoryProvider
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore persistentChatMemoryStore) {
        return userId -> MessageWindowChatMemory.builder()
                .id(userId)
                .maxMessages(200)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();
    }



    @Bean
    public EmbeddingStoreContentRetriever embeddingStoreContentRetriever(
            EmbeddingStore embeddingStore,
            EmbeddingModel embeddingModel) {  // 注入具体的嵌入模型
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)  // 明确指定使用哪个模型
                .minScore(0.7)
                .maxResults(300)
                .build();
    }

}
