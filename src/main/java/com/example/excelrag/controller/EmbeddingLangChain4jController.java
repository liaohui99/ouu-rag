package com.example.excelrag.controller;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/23 15:50
 * @description: TODO
 */
@RestController
@RequestMapping("/embedding")
public class EmbeddingLangChain4jController {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private QdrantClient qdrantClient;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    // 文本向量化
    // http://localhost:9010/embedding/embed
    @GetMapping("/embedding/embed")
    public String embed(@RequestParam("embedTest") String text) {
        String msg = "咏鹅" +
                "鹅，鹅，鹅，" +
                "曲项向天歌。" +
                "白毛浮绿水，" +
                "红掌拨清波。";
        if (text != null && !text.isEmpty()){
            msg = text;
        }
        Response<Embedding> embeddingResponse = embeddingModel.embed(msg);
        return embeddingResponse.content().toString();
    }

    // 新建向量数据库实例和创建索引：test-qdrant
    // http://localhost:9010/embedding/createCollection
    @GetMapping("/embedding/createCollection")
    public void createCollection() {
        Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                .setDistance(Collections.Distance.Cosine)
                .setSize(1024)
                .build();
        qdrantClient.createCollectionAsync("test-qdrant", vectorParams);
    }

    // 向量数据库新增文本数据
    // http://localhost:9010/embedding/add
    @GetMapping("/embedding/add")
    public String add(@RequestParam("embedTest") String text) {
        String msg = "咏鹅" +
                "鹅，鹅，鹅，" +
                "曲项向天歌。" +
                "白毛浮绿水，" +
                "红掌拨清波。";
        if (text != null && !text.isEmpty()){
            msg = text;
        }
        TextSegment textSegment = TextSegment.from(msg);
        textSegment.metadata().put("author", "骆宾王");
        Embedding embedding = embeddingModel.embed(textSegment).content();
        String result = embeddingStore.add(embedding, textSegment);
        return result;
    }

    // 向量数据库查询
    // http://localhost:9010/embedding/query1
    @GetMapping("/embedding/query1")
    public String query1(@RequestParam("embedTest") String text) {
        String msg = "咏鹅是什么";
        if (text != null && !text.isEmpty()){
            msg = text; // 重新赋值
        }
        Embedding embedding = embeddingModel.embed(msg).content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .maxResults(1)
                .filter(metadataKey("author").isEqualTo("骆宾王1"))
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(embeddingSearchRequest);
        List<EmbeddingMatch<TextSegment>> matchList = searchResult.matches();
        String result = "";
        if (matchList != null && !matchList.isEmpty()) {
            result = matchList.get(0).embedded().text();
        }
        return result;
    }

    // http://localhost:9010/embedding/query2
    @GetMapping("/embedding/query2")
    public String query2(@RequestParam("embedTest") String text) {
        String msg = "咏鹅";
        if (text != null && !text.isEmpty()){
            msg = text; // 重新赋值
        }
        Embedding embedding = embeddingModel.embed(msg).content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .filter(metadataKey("author").isEqualTo("zzyy"))
                .maxResults(1)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(embeddingSearchRequest);
        List<EmbeddingMatch<TextSegment>> matchList = searchResult.matches();
        String result = "";
        if (matchList != null && !matchList.isEmpty()) {
            result = matchList.get(0).embedded().text();
        }
        return result;
    }
}

