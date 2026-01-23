# Excel RAG Application

基于SpringBoot和LangChain4j的Excel文件RAG检索系统

## 项目结构

```
ouu-rag/
├── src/main/java/com/example/excelrag/
│   ├── ExcelRagApplication.java          # 应用启动类
│   ├── config/
│   │   └── LangChain4jConfig.java        # LangChain4j配置
│   ├── controller/
│   │   └── RAGController.java            # REST API控制器
│   ├── model/
│   │   ├── dto/
│   │   │   ├── DocumentChunk.java        # 文档块DTO
│   │   │   ├── RAGRequest.java           # RAG请求DTO
│   │   │   └── RAGResponse.java          # RAG响应DTO
│   │   └── excel/
│   │       ├── ExcelCellData.java        # Excel单元格数据模型
│   │       ├── TextChunk.java            # 文本块模型
│   │       └── VectorChunk.java          # 向量块模型
│   ├── parser/
│   │   ├── ExcelAnalysisListener.java    # Excel解析监听器
│   │   └── ExcelParserUtil.java          # Excel解析工具类
│   └── service/
│       ├── EmbeddingService.java         # 向量化服务
│       ├── RAGService.java               # RAG核心服务
│       ├── TextChunkService.java         # 文本分块服务
│       └── VectorStoreService.java       # 向量存储服务
├── src/main/resources/
│   └── application.yml                  # 应用配置文件
└── pom.xml                               # Maven依赖配置
```

## 技术栈

- SpringBoot 3.2.0
- LangChain4j 0.29.1
- EasyExcel 3.3.2
- Java 17

## API接口

### 1. 上传Excel文件
```
POST /api/rag/upload
Content-Type: multipart/form-data
参数: file (Excel文件)
```

### 2. 查询检索
```
POST /api/rag/query
Content-Type: application/json
Body: {
  "query": "查询问题",
  "topK": 5
}
```

### 3. 获取状态
```
GET /api/rag/status
```

### 4. 清空向量库
```
DELETE /api/rag/clear
```

## 运行项目

1. 使用Maven编译项目
2. 运行ExcelRagApplication
3. 访问 http://localhost:8080

## 功能特性

- 支持Excel文件解析（.xls/.xlsx）
- 自动文本分块和向量化
- 余弦相似度检索
- 完整的元数据保留
- RESTful API接口
