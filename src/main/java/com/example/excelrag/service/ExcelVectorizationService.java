package com.example.excelrag.service;

import com.example.excelrag.model.excel.ExcelCellData;
import com.example.excelrag.model.excel.TextChunk;
import com.example.excelrag.model.excel.VectorChunk;
import com.example.excelrag.utils.excel.ExcelParserUtil;
import com.example.excelrag.utils.excel.ExcelTextSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Excel向量化一站式调用服务
 * 对外提供统一入口，屏蔽底层四层实现细节
 */
@Service
@RequiredArgsConstructor
public class ExcelVectorizationService {
    private final ExcelTextSplitter excelTextSplitter;
    private final ExcelVectorStoreService excelVectorStoreService;


    public static void main(String[] args) {
        String excelFilePath = "E:\\study\\AI\\ouu-rag\\src\\main\\resources\\file\\成交分析副本.xlsx";
        File excelFile = new File(excelFilePath);

        if (!excelFile.exists()) {
            System.err.println("❌ 文件不存在：" + excelFilePath);
            System.err.println("请检查文件路径是否正确，或创建测试文件");
            return;
        }

        try {
            // 1. 解析Excel：单元格数据
            List<ExcelCellData> cellDataList = ExcelParserUtil.parseExcel(excelFile, 1);
            // 2. 文本分块：按行聚合为TextChunk
            ExcelTextSplitter splitter = new ExcelTextSplitter();
            List<TextChunk> textChunks = splitter.splitByRow(cellDataList);
            ExcelVectorStoreService vectorStoreService = null;

            // 3. 向量化：生成向量数据
            vectorStoreService = new ExcelVectorStoreService();
            //List<VectorChunk> vectorChunks = vectorStoreService.generateVector(textChunks);
            // 4. 写入ES：批量存储向量+文本+元数据
            //vectorStoreService.saveToElasticsearch(vectorChunks);
/*            System.out.println("✅ Excel文件向量化成功，共生成" + vectorChunks.size() + "个向量块");
            File file = new File("E:\\study\\AI\\ouu-rag\\src\\main\\resources\\file\\vectorStoreService.ser");
            if (!file.exists()) {
                // 3. 向量化：生成向量数据
                vectorStoreService = new ExcelVectorStoreService();
                List<VectorChunk> vectorChunks = vectorStoreService.generateVector(textChunks);
                // 4. 写入ES：批量存储向量+文本+元数据
                vectorStoreService.saveToElasticsearch(vectorChunks);
                System.out.println("✅ Excel文件向量化成功，共生成" + vectorChunks.size() + "个向量块");

                //序列化vectorStoreService到“E:\study\AI\ouu-rag\src\main\resources\file”
                System.out.println("序列化vectorStoreService到" + file.getAbsolutePath());
                byte[] serialize = SerializationUtils.serialize(vectorStoreService);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(serialize);
                fos.close();
            } else {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] fileBytes = baos.toByteArray();
                fis.close();
                vectorStoreService = (ExcelVectorStoreService) SerializationUtils.deserialize(fileBytes);
            }*/


            vectorStoreService.chatWithExcel();
 /*           EmbeddingModel embeddingModel = vectorStoreService.embeddingModel;
            EmbeddingStore<TextSegment> embeddingStore = vectorStoreService.embeddingStore;
            // 7. 验证检索效果
            String userQuery = "LangChain4j的核心功能是什么？";
            Response<Embedding> embed = embeddingModel.embed(userQuery);
            EmbeddingSearchRequest builder = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embed.content())
                    .maxResults(Integer.MAX_VALUE)
                    .filter(null)
                    .minScore(0.5)
                    .build();
            EmbeddingSearchResult<TextSegment> search = embeddingStore.search(builder);
            List<EmbeddingMatch<TextSegment>> relevantSegments = search.matches();

            for (int i = 0; i < relevantSegments.size(); i++) {
                System.out.println("\n🔍 测试检索: " + userQuery);
                var result = relevantSegments.get(i);
                System.out.println("\n结果 " + (i + 1) + " (相似度: " +
                        String.format("%.3f", result.score()) + "):");
                System.out.println(result.embedded().text());
            }*/

        } catch (Exception e) {
            throw new RuntimeException("❌ Excel文件向量化失败：" + e.getMessage(), e);
        }
    }

    /**
     * Excel文件向量化一站式入口
     *
     * @param excelFilePath Excel文件绝对路径
     * @param headRowNumber 表头行数（有表头=1，无表头=0）
     */
    public void vectorizeExcel(String excelFilePath, int headRowNumber) {
        try {
            File excelFile = new File(excelFilePath);
            // 1. 解析Excel：单元格数据
            List<ExcelCellData> cellDataList = ExcelParserUtil.parseExcel(excelFile, headRowNumber);
            // 2. 文本分块：按行聚合为TextChunk
            List<TextChunk> textChunks = excelTextSplitter.splitByRow(cellDataList);
            // 3. 向量化：生成向量数据
            List<VectorChunk> vectorChunks = excelVectorStoreService.generateVector(textChunks);
            // 4. 写入ES：批量存储向量+文本+元数据
            excelVectorStoreService.saveToElasticsearch(vectorChunks);
        } catch (Exception e) {
            throw new RuntimeException("❌ Excel文件向量化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 重载方法：默认无表头（headRowNumber=0），简化调用
     */
    public void vectorizeExcel(String excelFilePath) {
        vectorizeExcel(excelFilePath, 0);
    }
}

// ========== 调用示例 ==========
// @Autowired
// private ExcelVectorizationService excelVectorizationService;
//
// // 无表头Excel调用
// excelVectorizationService.vectorizeExcel("D:/数据报表.xlsx");
// // 有表头Excel调用
// excelVectorizationService.vectorizeExcel("D:/用户信息表.xlsx", 1);

