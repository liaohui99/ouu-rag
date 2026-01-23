package com.example.excelrag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    // @Bean
    // public EmbeddingModel embeddingModel() {
    //     return new AllMiniLmL6V2EmbeddingModel();
    // }

    @Autowired
    ApplicationConfig config;

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(QdrantGrpcClient.newBuilder("192.168.111.11", 6334, false).build());
    }


    //@Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        //return new InMemoryEmbeddingStore<>();
        return QdrantEmbeddingStore.builder()
                .host("192.168.111.11")
                .port(6334)  // gRPC端口
                .collectionName("ouu-rag-test")
                .build();
    }

    //@Bean
    public EmbeddingModel embeddingModel() {
        //return new InMemoryEmbeddingStore<>();
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(config.getBaiLianApiKey())
                .modelName("text-embedding-v4")
                .build();
    }

    //@Bean
    public StreamingChatModel streamingChatModel() {
        //return new InMemoryEmbeddingStore<>();
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(config.getBaiLianApiKey())
                .modelName("text-embedding-v4")
                .build();
    }


}
