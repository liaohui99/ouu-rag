package com.example.excelrag.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2025/12/2 11:28
 * @description: AI对话请求参数类
 */
@Data
public class PromptReq {

    @NotNull(message = "memoryId不能为空")
    Long memoryId;

    @NotNull(message = "userMessage不能为空")
    String userMessage;

    // 显式添加getter和setter以确保Lombok问题时代码仍可编译
    public Long getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Long memoryId) {
        this.memoryId = memoryId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
}
