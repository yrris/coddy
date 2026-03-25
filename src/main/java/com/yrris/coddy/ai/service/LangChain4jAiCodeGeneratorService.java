package com.yrris.coddy.ai.service;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
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

    private final AiCodeGeneratorServiceFactory factory;

    public LangChain4jAiCodeGeneratorService(AiCodeGeneratorServiceFactory factory) {
        this.factory = factory;
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
}
