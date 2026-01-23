package com.example.excelrag.model.excel;

import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorChunk {
    private String content;
    private Map<String, Object> metadata;
    private float[] vector;

    public TextSegment toTextSegment() {
        if (metadata == null || metadata.isEmpty()) {
            return TextSegment.from(content);
        }
        dev.langchain4j.data.document.Metadata langchainMetadata = dev.langchain4j.data.document.Metadata.from(metadata);
        return TextSegment.from(content, langchainMetadata);
    }

    public static VectorChunk fromTextSegment(TextSegment textSegment, float[] vector) {
        Map<String, Object> metadata;
        if (textSegment.metadata() != null) {
            metadata = new java.util.HashMap<>();
            final Map<String, Object> finalMetadata = metadata;
            textSegment.metadata().toMap().forEach((key, value) -> finalMetadata.put(key, value));
        } else {
            metadata = null;
        }
        return VectorChunk.builder()
                .content(textSegment.text())
                .metadata(metadata)
                .vector(vector)
                .build();
    }
}
