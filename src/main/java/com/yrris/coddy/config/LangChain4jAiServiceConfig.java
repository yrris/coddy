package com.yrris.coddy.config;

import com.yrris.coddy.ai.service.LangChain4jCodeGeneratorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the LangChain4j declarative AI service bean.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "langchain4j")
public class LangChain4jAiServiceConfig {

    @Bean
    public LangChain4jCodeGeneratorAgent langChain4jCodeGeneratorAgent(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel
    ) {
        return AiServices.builder(LangChain4jCodeGeneratorAgent.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
