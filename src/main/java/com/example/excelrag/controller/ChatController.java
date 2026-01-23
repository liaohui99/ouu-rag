package com.example.excelrag.controller;

import com.example.excelrag.req.PromptReq;
import com.example.excelrag.service.ChatDemoAssistant;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;


@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    @Autowired
    StreamingChatModel streamingChatModel;
    @Autowired
    ChatDemoAssistant chatDemoAssistant;
    @Autowired
    EmbeddingModel embeddingModel;
    @Autowired
    EmbeddingStore embeddingStore;

    @PostMapping("/stream")
    public Flux<String> stream(@RequestBody @Validated PromptReq prompt, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");


        // 1. 检索相关向量
/*        Response<Embedding> queryEmbedding = embeddingModel.embed(prompt.getUserMessage());
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
        }*/

        return chatDemoAssistant.chatFlux(prompt.getMemoryId(), prompt.getUserMessage());
/*
        return Flux.create(sink -> streamingChatModel.chat(userMessage, new StreamingChatResponseHandler() {


            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {

            }

            @Override
            public void onError(Throwable error) {

            }
        }));*/
    }
}
