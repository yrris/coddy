package com.yrris.coddy.ai.service;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Production AI service implementation backed by LangChain4j.
 */
@Service
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "langchain4j")
public class LangChain4jAiCodeGeneratorService implements AiCodeGeneratorService {

    private final LangChain4jCodeGeneratorAgent codeGeneratorAgent;

    public LangChain4jAiCodeGeneratorService(LangChain4jCodeGeneratorAgent codeGeneratorAgent) {
        this.codeGeneratorAgent = codeGeneratorAgent;
    }

    @Override
    public HtmlCodeResult generateHtmlCode(String userMessage) {
        return codeGeneratorAgent.generateHtmlCode(userMessage);
    }

    @Override
    public MultiFileCodeResult generateMultiFileCode(String userMessage) {
        return codeGeneratorAgent.generateMultiFileCode(userMessage);
    }

    @Override
    public Flux<String> generateHtmlCodeStream(String userMessage) {
        return codeGeneratorAgent.generateHtmlCodeStream(userMessage);
    }

    @Override
    public Flux<String> generateMultiFileCodeStream(String userMessage) {
        return codeGeneratorAgent.generateMultiFileCodeStream(userMessage);
    }
}
