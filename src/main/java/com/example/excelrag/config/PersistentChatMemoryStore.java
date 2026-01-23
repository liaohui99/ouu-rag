package com.example.excelrag.config;

import cn.hutool.core.collection.CollUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("persistentChatMemoryStore")
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {
    private final Map<Object, List<ChatMessage>> memoryStore = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: 实现通过内存ID从持久化存储中获取所有消息。
        List<ChatMessage> chatMessages = memoryStore.get(memoryId);
        if (CollUtil.isEmpty(chatMessages)){
            LinkedList<ChatMessage> msgs = new LinkedList<>();
            memoryStore.put(memoryId, msgs);
            return msgs;
        }
        return chatMessages;
        // 可以使用ChatMessageDeserializer.messageFromJson(String)和
        // ChatMessageDeserializer.messagesFromJson(String)辅助方法
        // 轻松地从JSON反序列化聊天消息。
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: 实现通过内存ID更新持久化存储中的所有消息。
        // 在存储前清理消息，确保AiMessage的text不为null
        List<ChatMessage> sanitizedMessages = messages.stream()
                .map(msg -> {
                    if (msg instanceof AiMessage) {
                        AiMessage aiMsg = (AiMessage) msg;
                        if (aiMsg.text() == null) {
                            log.warn("AiMessage text is null, replace with empty string");
                            log.warn("AiMessage: {}", aiMsg);
                            // 重新构建AiMessage，用空字符串替代null
                            return AiMessage.builder()
                                    .text("") // 关键：强制设置空字符串
                                    .toolExecutionRequests(aiMsg.toolExecutionRequests())
                                    .attributes(aiMsg.attributes())
                                    .build();
                        }
                    }
                    return msg;
                })
                .collect(Collectors.toList());
        memoryStore.put(memoryId, sanitizedMessages);
        // 可以使用ChatMessageSerializer.messageToJson(ChatMessage)和
        // ChatMessageSerializer.messagesToJson(List<ChatMessage>)辅助方法
        // 轻松地将聊天消息序列化为JSON。

    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: 实现通过内存ID删除持久化存储中的所有消息。
        memoryStore.remove(memoryId);
    }

}
