package com.yrris.coddy.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.ai.model.StreamMessage;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Production AI service implementation backed by LangChain4j.
 * Uses AiCodeGeneratorServiceFactory for per-app memory-aware agents.
 */
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "langchain4j")
public class LangChain4jAiCodeGeneratorService implements AiCodeGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jAiCodeGeneratorService.class);

    private final AiCodeGeneratorServiceFactory factory;
    private final ObjectMapper objectMapper;

    public LangChain4jAiCodeGeneratorService(AiCodeGeneratorServiceFactory factory, ObjectMapper objectMapper) {
        this.factory = factory;
        this.objectMapper = objectMapper;
    }

    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        return factory.getAgent(0L).generateHtmlCode(0L, userMessage);
    }

    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        return factory.getAgent(0L).generateMultiFileCode(0L, userMessage);
    }

    @Override
    public Flux<String> generateHtmlCodeStream(String userMessage) {
        return factory.getAgent(0L).generateHtmlCodeStream(0L, userMessage);
    }

    @Override
    public Flux<String> generateMultiFileCodeStream(String userMessage) {
        return factory.getAgent(0L).generateMultiFileCodeStream(0L, userMessage);
    }

    @Override
    public Flux<String> generateHtmlCodeStream(long appId, String userMessage) {
        return factory.getAgent(appId).generateHtmlCodeStream(appId, userMessage);
    }

    @Override
    public Flux<String> generateMultiFileCodeStream(long appId, String userMessage) {
        return factory.getAgent(appId).generateMultiFileCodeStream(appId, userMessage);
    }

    @Override
    public Flux<String> generateReactViteProjectStream(long appId, String userMessage) {
        return Flux.create(sink -> {
            LangChain4jReactViteAgent agent = factory.getReactViteAgent(appId);
            TokenStream tokenStream = agent.generateReactProject(appId, userMessage);

            tokenStream
                    .onPartialResponse(token -> {
                        if (token != null && !token.isEmpty()) {
                            sink.next(toJson(StreamMessage.aiResponse(token)));
                        }
                    })
                    .onToolExecuted(toolExecution -> {
                        sink.next(toJson(StreamMessage.toolExecuted(
                                toolExecution.request().name(),
                                toolExecution.result()
                        )));
                    })
                    .onCompleteResponse(response -> {
                        sink.next(toJson(StreamMessage.complete()));
                        sink.complete();
                    })
                    .onError(error -> {
                        log.error("React+Vite generation error for appId={}: {}", appId, error.getMessage());
                        sink.next(toJson(StreamMessage.error(error.getMessage())));
                        sink.complete();
                    })
                    .start();
        });
    }

    private String toJson(StreamMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize StreamMessage: {}", e.getMessage());
            return "{\"type\":\"ERROR\",\"data\":\"Serialization error\"}";
        }
    }
}
