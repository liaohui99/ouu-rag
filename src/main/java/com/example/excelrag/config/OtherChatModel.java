package com.example.excelrag.config;

import com.example.excelrag.service.ChartAssistant;
import com.example.excelrag.service.ChartDisplayAssistant;
import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Scanner;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 13:36
 * @description: TODO
 */
@Configuration
@RequiredArgsConstructor
public class OtherChatModel {

    private final ApplicationConfig applicationConfig;


    @Bean
    public ChatModel semanticConversionChatModel() {
        return OpenAiChatModel.builder()
/*                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")*/
                .baseUrl(applicationConfig.getBaiLianBaseUrl())
                .apiKey(applicationConfig.getBaiLianApiKey())
                .modelName(applicationConfig.getBaiLianModelName())
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .build();
    }


    public static void main(String[] args) {

        ChartAssistant assistant = createAssistant();

        // You can ask questions such as "How many customers do we have?" and "What is our top seller?".
        startConversationWith(assistant);
    }

    public static void startConversationWith(ChartAssistant assistant) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChartDisplayAssistant.class);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                log.info("==================================================");
                log.info("User: ");
                String userQuery = scanner.nextLine();
                log.info("==================================================");

                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }

                String agentAnswer = assistant.chat(userQuery);
                log.info("==================================================");
                log.info("Assistant: " + agentAnswer);
            }
        }
    }

    private static ChartAssistant createAssistant() {

        DataSource dataSource = createDataSource();

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey("sk-36f36bf254134932b98225f6c8fbb616")
                .modelName("qvq-plus")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        ContentRetriever contentRetriever = SqlDatabaseContentRetriever.builder()
                .dataSource(dataSource)
                .chatModel(chatModel)
                .build();

        return AiServices.builder(ChartAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    private static DataSource createDataSource() {

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:mysql://101.126.13.194:3306/ouu_dev?serverTimezone=Asia/Shanghai&characterEncoding=utf8&useUnicode=true&useSSL=false&allowMultiQueries=true");
        dataSource.setUser("root");
        dataSource.setPassword("z*UoRY#-Lro~nZNJ6q0D");

        return dataSource;
    }






}
