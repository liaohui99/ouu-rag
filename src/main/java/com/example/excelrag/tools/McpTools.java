package com.example.excelrag.tools;


import com.example.excelrag.service.ChatDemoAssistant;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 14:56
 * @description: TODO
 */
@Slf4j
@Configuration
public class McpTools {



    @Bean
    public McpClient chart() {
        return new DefaultMcpClient.Builder()
                .transport(StdioMcpTransport.builder()
                        .command(List.of("cmd", "/c", "npx", "-y", "@antv/mcp-server-chart"))
                        .logEvents(true)
                        .build())
                .build();
    }

    //@Bean
    public McpClient chartDisplay() {
        // 使用新的 SSE 传输实现
        var transport = StreamableHttpMcpTransport.builder()
                .url("https://mcp.api-inference.modelscope.net/b5b70b46c5fe4a/mcp")
                .logRequests(true)
                .logResponses(true)
                .build();
        List<ToolSpecification> toolSpecifications = new DefaultMcpClient.Builder()
                .transport(transport)
                .build().listTools();
        toolSpecifications.forEach(toolSpecification -> log.info("McpClient tools : {}", toolSpecification.name()));
        return new DefaultMcpClient.Builder()
                .transport(transport)
                .build();
    }





    @Bean(name = "mcpToolProvider")
    public ToolProvider mcpToolProvider(List<McpClient> mcpClients) {
        if (mcpClients.isEmpty()){
            return McpToolProvider.builder()
                    .mcpClients(Collections.emptyList())
                    .build();
        }
        return McpToolProvider.builder()
                .mcpClients(mcpClients)
                .build();
    }


    // 或者，如果您需要将工具以集合形式注入
   // @Bean
    public List<ToolSpecification> allMcpToolSpecifications(List<McpClient> mcpClients) {
        List<ToolSpecification> allTools = new ArrayList<>();
        // 添加MCP工具
        allTools.addAll(mcpClients.stream().flatMap(client -> client.listTools().stream()).toList());
        return allTools;
    }

    // 或者，如果您需要将工具以集合形式注入
   // @Bean
    public List<ToolSpecification> allMcpToolSpecifications2(List<McpClient> mcpClients) {
        List<ToolSpecification> allTools = new ArrayList<>();
        // 添加MCP工具
        allTools.addAll(mcpClients.stream().flatMap(client -> client.listTools().stream()).toList());
        return allTools;
    }


}
