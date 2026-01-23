package com.example.excelrag.service;

import com.example.excelrag.model.dto.DocumentChunk;
import com.example.excelrag.model.dto.RAGRequest;
import com.example.excelrag.model.dto.RAGResponse;
import com.example.excelrag.model.excel.ExcelCellData;
import com.example.excelrag.model.excel.TextChunk;
import com.example.excelrag.model.excel.VectorChunk;
import com.example.excelrag.parser.ExcelParserUtil;
import com.example.excelrag.utils.excel.ExcelTextSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private ExcelTextSplitter excelTextSplitter;

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${rag.retrieval.top-k:5}")
    private int defaultTopK;

    public String processExcelFile(MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("excel-upload");
        Path tempFile = tempDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        File excelFile = tempFile.toFile();
        List<ExcelCellData> cellDataList = ExcelParserUtil.parseExcel(excelFile);

        logger.info("解析Excel文件成功，共{}个单元格", cellDataList.size());

        List<TextChunk> textChunks = excelTextSplitter.splitByRow(cellDataList);
        logger.info("生成文本块{}个", textChunks.size());

        List<VectorChunk> vectorChunks = embeddingService.embedTextChunks(textChunks);
        vectorStoreService.storeAll(vectorChunks);
        logger.info("向量化并存储完成，当前向量库共{}个向量", vectorStoreService.size());

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);

        return "Excel文件处理成功，共生成" + vectorChunks.size() + "个向量块";
    }

    public RAGResponse query(RAGRequest request) {
        Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();
        int topK = request.getTopK() != null ? request.getTopK() : defaultTopK;

        List<VectorStoreService.VectorChunkWithScore> relevantChunks = vectorStoreService.search(queryEmbedding.vector(), topK);

        List<DocumentChunk> contexts = relevantChunks.stream()
                .map(item -> DocumentChunk.builder()
                        .content(item.getChunk().getContent())
                        .score(item.getScore())
                        .metadata(item.getChunk().getMetadata())
                        .build())
                .collect(Collectors.toList());

        String contextText = contexts.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        String prompt = buildPrompt(request.getQuery(), contextText);

        String answer = generateAnswer(prompt);

        return RAGResponse.builder()
                .answer(answer)
                .contexts(contexts)
                .build();
    }

    private String buildPrompt(String query, String context) {
        return "基于以下Excel数据信息回答问题：\n\n" +
                "【上下文信息】\n" +
                context + "\n\n" +
                "【问题】\n" +
                query + "\n\n" +
                "请根据上下文信息准确回答问题。如果上下文中没有相关信息，请说明。";
    }

    private String generateAnswer(String prompt) {
        return "基于Excel数据的回答：\n" + prompt;
    }

    public int getVectorStoreSize() {
        return vectorStoreService.size();
    }

    public String clearVectorStore() {
        vectorStoreService.clear();
        return "向量库已清空";
    }
}
