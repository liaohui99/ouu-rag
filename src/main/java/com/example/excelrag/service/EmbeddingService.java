package com.example.excelrag.service;

import com.example.excelrag.model.excel.TextChunk;
import com.example.excelrag.model.excel.VectorChunk;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    public List<VectorChunk> embedTextChunks(List<TextChunk> textChunks) {
        List<dev.langchain4j.data.segment.TextSegment> textSegments = textChunks.stream()
                .map(chunk -> {
                    if (chunk.getMetadata() == null || chunk.getMetadata().isEmpty()) {
                        return dev.langchain4j.data.segment.TextSegment.from(chunk.getContent());
                    }
                    dev.langchain4j.data.document.Metadata metadata = dev.langchain4j.data.document.Metadata.from(chunk.getMetadata());
                    return dev.langchain4j.data.segment.TextSegment.from(chunk.getContent(), metadata);
                })
                .collect(Collectors.toList());

        dev.langchain4j.model.output.Response<List<Embedding>> response = embeddingModel.embedAll(textSegments);
        List<Embedding> embeddings = response.content();

        return IntStream.range(0, textChunks.size())
                .mapToObj(i -> {
                    TextChunk textChunk = textChunks.get(i);
                    return VectorChunk.builder()
                            .content(textChunk.getContent())
                            .metadata(textChunk.getMetadata())
                            .vector(embeddings.get(i).vector())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public VectorChunk embedTextChunk(TextChunk textChunk) {
        Embedding embedding = embeddingModel.embed(textChunk.getContent()).content();
        return VectorChunk.builder()
                .content(textChunk.getContent())
                .metadata(textChunk.getMetadata())
                .vector(embedding.vector())
                .build();
    }
}
