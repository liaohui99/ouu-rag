package com.example.excelrag.config;

import com.example.excelrag.service.ChartDisplayAssistant;
import com.example.excelrag.service.ChatDemoAssistant;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
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
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * LLM配置类
 * 配置LangChain4j相关的Bean
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SpringLLMConfig {

    private final ApplicationConfig applicationConfig;
    //private final ChromaApiProperties chromaApiProperties;
    //private final ChromaVectorStoreProperties chromaVectorStoreProperties;


    @Autowired
    private ConfigurableListableBeanFactory beanFactory;
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
                .host("192.168.111.11")
                .port(6334)  // gRPC端口
                .collectionName("ouu-rag-test")
                .client(new QdrantClient(QdrantGrpcClient.newBuilder("192.168.111.11", 6334, false).build()))
                .build();
    }

  /*  @Bean
    public VectorStore chromaVectorStore(
            EmbeddingModel embeddingModel) {

        return ChromaVectorStore.builder(embeddingModel, new ChromaApi(chromaApiProperties.getHost() + ":" + chromaApiProperties.getPort()))
                .collectionName(chromaVectorStoreProperties.getCollectionName())
                .initializeSchema(true)  // ✅ 自动创建集合
                .build();
    }*/

/*    @Bean
    public EmbeddingStore embeddingStore() {
        //randomUUID();
        //createCollectionIfNotExists();
        // 自动创建集合（推荐）
        return ChromaEmbeddingStore.builder()
                .apiVersion(V2)
                .baseUrl(chromaApiProperties.getHost() + ":" + chromaApiProperties.getPort())
                .collectionName(chromaVectorStoreProperties.getCollectionName())
                .logRequests(true)
                .logResponses(true)
                .build();
    }*/

/*    private void createCollectionIfNotExists() {
        Client client = new Client(chromaApiProperties.getHost() + ":" + chromaApiProperties.getPort());
        // ✅ 使用 createCollection 并设置 getOrCreate = true
        try {
            EmbeddingFunction embeddingFunction = new DefaultEmbeddingFunction();
            client.createCollection(
                    chromaVectorStoreProperties.getCollectionName(),  // 集合名称,           // 集合名称
                    Map.of(),                // metadata（空映射）
                    true,                    // ✅ getOrCreate = true（不存在则创建）
                    embeddingFunction        // 嵌入函数（必需）
            );
            System.out.println("✅ 集合 '" + chromaVectorStoreProperties.getCollectionName() + "' 已就绪");
        } catch (Exception e) {
            System.err.println("❌ 操作失败: " + e.getMessage());
        }
    }*/


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
        return MessageWindowChatMemory.withMaxMessages(50);
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
    public ChartDisplayAssistant chartDemoAssistant(ChatModel semanticConversionChatModel,
                                                     ChatMemoryProvider chatMemoryProvider,
                                                     ToolProvider mcpToolProvider

    ) {
        return AiServices.builder(ChartDisplayAssistant.class)
                .chatModel(semanticConversionChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                //.contentRetriever(embeddingStoreContentRetriever)
                .toolProvider(mcpToolProvider)
                .build();
    }




    //@Bean
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

//    @Bean
//    @Lazy
    public ChatDemoAssistant chatDemoAssistant(ChatMemoryProvider chatMemoryProvider,
                                               //EmbeddingStoreContentRetriever embeddingStoreContentRetriever,
                                               ToolProvider toolProvider,
                                               List<ToolSpecification> scanForToolMethods
    ) {  // 注入具体的嵌入模型

        return AiServices.builder(ChatDemoAssistant.class)
                .chatModel(baiLianChatModel())
                .streamingChatModel(baiLianStreamingChatModel())
                .chatMemoryProvider(chatMemoryProvider)
                //.contentRetriever(embeddingStoreContentRetriever)
                .tools(scanForToolMethods())
                .toolProvider(toolProvider)
                .build();
    }

    public List<ToolSpecification> scanForToolMethods() {
        List<ToolSpecification> toolSpecifications = new ArrayList<>();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            try {
                String beanClassName = beanFactory.getBeanDefinition(beanName).getBeanClassName();
                if (beanClassName == null) {
                    continue;
                }
                Class<?> beanClass = Class.forName(beanClassName);
                for (Method beanMethod : beanClass.getDeclaredMethods()) {
                    if (beanMethod.isAnnotationPresent(Tool.class)) {
                        //toolBeanNames.add(beanName);
                        try {
                            toolSpecifications.add(ToolSpecifications.toolSpecificationFrom(beanMethod));
                        } catch (Exception e) {
                            log.warn("Cannot convert %s.%s method annotated with @Tool into ToolSpecification"
                                    .formatted(beanClass.getName(), beanMethod.getName()), e);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO
            }
        }

        return toolSpecifications;
    }


}
