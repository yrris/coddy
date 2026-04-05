package com.yrris.coddy.ai.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yrris.coddy.ai.tool.FileWriteTool;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import com.yrris.coddy.model.entity.ChatHistory;
import com.yrris.coddy.repository.ChatHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "langchain4j")
public class AiCodeGeneratorServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(AiCodeGeneratorServiceFactory.class);

    private static final int MAX_MEMORY_MESSAGES = 20;

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final RedisChatMemoryStore redisChatMemoryStore;
    private final ChatHistoryRepository chatHistoryRepository;
    private final FileWriteTool fileWriteTool;

    private final Cache<Long, LangChain4jCodeGeneratorAgent> agentCache;
    private final Cache<Long, LangChain4jReactViteAgent> reactViteAgentCache;

    public AiCodeGeneratorServiceFactory(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            RedisChatMemoryStore redisChatMemoryStore,
            ChatHistoryRepository chatHistoryRepository,
            FileWriteTool fileWriteTool
    ) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.redisChatMemoryStore = redisChatMemoryStore;
        this.chatHistoryRepository = chatHistoryRepository;
        this.fileWriteTool = fileWriteTool;
        this.agentCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
        this.reactViteAgentCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
    }

    public LangChain4jCodeGeneratorAgent getAgent(long appId) {
        return agentCache.get(appId, this::createAgent);
    }

    private LangChain4jCodeGeneratorAgent createAgent(long appId) {
        seedMemoryFromDatabase(appId);

        return AiServices.builder(LangChain4jCodeGeneratorAgent.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(MAX_MEMORY_MESSAGES)
                                .chatMemoryStore(redisChatMemoryStore)
                                .build()
                )
                .build();
    }

    public LangChain4jReactViteAgent getReactViteAgent(long appId) {
        return reactViteAgentCache.get(appId, this::createReactViteAgent);
    }

    private LangChain4jReactViteAgent createReactViteAgent(long appId) {
        seedMemoryFromDatabase(appId);
        return AiServices.builder(LangChain4jReactViteAgent.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(MAX_MEMORY_MESSAGES)
                                .chatMemoryStore(redisChatMemoryStore)
                                .build()
                )
                .tools(fileWriteTool)
                .build();
    }

    private void seedMemoryFromDatabase(long appId) {
        try {
            List<ChatHistory> recentMessages = chatHistoryRepository
                    .findByProjectIdOrderByIdDesc(appId, PageRequest.of(0, MAX_MEMORY_MESSAGES));

            if (recentMessages.isEmpty()) {
                return;
            }

            // Reverse to chronological order (oldest first)
            List<ChatHistory> chronological = new ArrayList<>(recentMessages);
            Collections.reverse(chronological);

            List<ChatMessage> langchainMessages = new ArrayList<>();
            for (ChatHistory history : chronological) {
                if ("USER".equals(history.getSenderType())) {
                    langchainMessages.add(UserMessage.from(history.getContent()));
                } else if ("ASSISTANT".equals(history.getSenderType())) {
                    langchainMessages.add(AiMessage.from(history.getContent()));
                }
            }

            if (!langchainMessages.isEmpty()) {
                redisChatMemoryStore.updateMessages(appId, langchainMessages);
                log.info("Seeded {} messages into Redis memory for appId={}", langchainMessages.size(), appId);
            }
        } catch (Exception e) {
            log.warn("Failed to seed chat memory from database for appId={}: {}", appId, e.getMessage());
        }
    }
}
