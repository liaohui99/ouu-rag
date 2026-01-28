package com.example.excelrag.tools;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.http.codec.ServerSentEvent;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/28 14:21
 * @description: TODO
 */
public class ToolsStreamingChatResponseHandler implements StreamingChatResponseHandler {
    @Override
    public void onPartialResponse(String partialResponse) {
        /*// 只发送文本 token
        sink.next(ServerSentEvent.builder(partialResponse)
                .event("text")
                .build());*/
    }
    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {

    }

    @Override
    public void onError(Throwable error) {

    }
}
