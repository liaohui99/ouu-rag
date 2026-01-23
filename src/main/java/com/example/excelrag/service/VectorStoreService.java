package com.example.excelrag.service;

import com.example.excelrag.model.excel.VectorChunk;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    public void setEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    public void store(VectorChunk vectorChunk) {
        Embedding embedding = Embedding.from(vectorChunk.getVector());
        TextSegment textSegment = vectorChunk.toTextSegment();
        embeddingStore.add(embedding, textSegment);
    }

    public void storeAll(List<VectorChunk> chunks) {
        List<Embedding> embeddings = new ArrayList<>();
        List<TextSegment> textSegments = new ArrayList<>();

        for (VectorChunk chunk : chunks) {
            embeddings.add(Embedding.from(chunk.getVector()));
            textSegments.add(chunk.toTextSegment());
        }

        embeddingStore.addAll(embeddings, textSegments);
    }

    public void clear() {
        if (embeddingStore instanceof InMemoryEmbeddingStore) {
            embeddingStore = new InMemoryEmbeddingStore<>();
        } else {
            System.err.println("当前EmbeddingStore不支持清空操作");
        }
    }

    public int size() {
        return embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[0]))
                .maxResults(Integer.MAX_VALUE)
                .build()).matches().size();
    }

    public List<VectorChunkWithScore> search(float[] queryVector, int topK) {
        Embedding queryEmbedding = Embedding.from(queryVector);
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);

        return result.matches().stream()
                .map(match -> new VectorChunkWithScore(
                        VectorChunk.fromTextSegment(match.embedded(), match.embedding().vector()),
                        match.score()
                ))
                .collect(Collectors.toList());
    }

    public static class VectorChunkWithScore {
        private final VectorChunk chunk;
        private final double score;

        public VectorChunkWithScore(VectorChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public VectorChunk getChunk() {
            return chunk;
        }

        public double getScore() {
            return score;
        }
    }
}
