package com.example.excelrag.service;

import com.example.excelrag.model.excel.TextChunk;
import com.example.excelrag.model.excel.VectorChunk;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class ExcelVectorStoreService implements Serializable {
    EmbeddingModel embeddingModel;
    EmbeddingStore<TextSegment> embeddingStore;

    public ExcelVectorStoreService() {
        //this.embeddingStore = new InMemoryEmbeddingStore<>();
        //this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("BAI_LIAN_API_KEY"))
                .modelName("text-embedding-v4")
                .build();
        this.embeddingStore = QdrantEmbeddingStore.builder()
                .host("192.168.111.10")
                .port(6334)  // gRPC端口
                .collectionName("ouu-rag-test")
                .client(new QdrantClient(QdrantGrpcClient.newBuilder("192.168.111.11", 6334, false).build()))
                .build();
    }

    public ExcelVectorStoreService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public List<VectorChunk> generateVector(List<TextChunk> textChunks) {
        List<TextSegment> textSegments = textChunks.stream()
                .map(chunk -> {
                    if (chunk.getMetadata() == null || chunk.getMetadata().isEmpty()) {
                        return TextSegment.from(chunk.getContent());
                    }

                    // 转换不支持的数据类型
                    Map<String, Object> convertedMetadata = new HashMap<>();
                    chunk.getMetadata().forEach((key, value) -> {
                        if (value instanceof List) {
                            // 将 List 转换为逗号分隔的字符串
                            convertedMetadata.put(key, String.join(", ",
                                    ((List<?>) value).stream()
                                            .map(Object::toString)
                                            .toArray(String[]::new)));
                        } else if (value instanceof Object[]) {
                            // 将数组转换为逗号分隔的字符串
                            convertedMetadata.put(key, String.join(", ",
                                    Arrays.stream((Object[]) value)
                                            .map(Object::toString)
                                            .toArray(String[]::new)));
                        } else {
                            convertedMetadata.put(key, value);
                        }
                    });

                    Metadata metadata = Metadata.from(convertedMetadata);
                    return TextSegment.from(chunk.getContent(), metadata);
                })
                .collect(Collectors.toList());

        //Response<List<Embedding>> response = embeddingModel.embedAll(textSegments);
        // 将文本段按每批 10 个进行分组处理
        int batchSize = 10;
        List<Embedding> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < textSegments.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, textSegments.size());
            List<TextSegment> batch = textSegments.subList(i, endIndex);

            Response<List<Embedding>> response = embeddingModel.embedAll(batch);
            allEmbeddings.addAll(response.content());
        }
        List<Embedding> embeddings = allEmbeddings;

        return IntStream.range(0, textChunks.size())
                .mapToObj(i -> {
                    TextChunk textChunk = textChunks.get(i);
                    Embedding embedding = embeddings.get(i);
                    return VectorChunk.builder()
                            .content(textChunk.getContent())
                            .vector(embedding.vector())
                            .metadata(textChunk.getMetadata())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void saveToElasticsearch(List<VectorChunk> vectorChunks) {
        if (embeddingStore == null) {
            System.out.println("⚠️ EmbeddingStore未初始化，跳过存储");
            return;
        }
        List<Embedding> embeddings = vectorChunks.stream()
                .map(chunk -> Embedding.from(chunk.getVector()))
                .collect(Collectors.toList());
        List<TextSegment> textSegments = vectorChunks.stream()
                .map(VectorChunk::toTextSegment)
                .collect(Collectors.toList());
        embeddingStore.addAll(embeddings, textSegments);
        System.out.printf("✅ Excel向量数据写入完成 | 共写入分片数：%d%n", vectorChunks.size());
    }

    /**
     * 与Excel向量对话
     */
    public void chatWithExcel() {

        /*OpenAiChatModel simpleChatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.longcat.chat/openai")
                .apiKey(System.getenv("LONGCAT_API_KEY"))
                .modelName("LongCat-Flash-Chat")
                .logRequests(true)
                .logResponses(true)
                .build();*/

        OpenAiChatModel simpleChatModel = OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(System.getenv("BAI_LIAN_API_KEY"))
                .modelName("qwen-plus")
                .logRequests(true)
                .logResponses(true)
                .build();



        while (true) {
            //控制台输入
            Scanner scanner = new Scanner(System.in);
            System.out.print("用户：");
            String userQuery = scanner.nextLine();


            // 1. 检索相关向量
            Response<Embedding> queryEmbedding = embeddingModel.embed(userQuery);
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
            }

            // 3. 调用大模型生成回答
            String prompt = String.format(
                    "你是一个Excel数据分析助手。基于以下表格数据回答问题,不允许捏造数据：\n\n" +
                            "数据内容：\n%s\n\n" +
                            "用户问题：%s\n\n" +
                            "请直接给出答案，并引用相关数据支持你的结论。",
                    context, userQuery);


            String generate = simpleChatModel.chat(prompt);
            log.info("\n LLM输出：{}", generate);

        }


    }
}
