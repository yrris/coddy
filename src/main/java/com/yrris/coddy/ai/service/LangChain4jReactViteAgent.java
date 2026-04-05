package com.yrris.coddy.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AI service interface for React+Vite project generation.
 * Separate from LangChain4jCodeGeneratorAgent because tool-calling agents
 * require different builder config (.tools() registration).
 * Returns TokenStream to support tool execution callbacks.
 */
public interface LangChain4jReactViteAgent {

    @SystemMessage(fromResource = "prompt/codegen-react-vite-system-prompt.txt")
    TokenStream generateReactProject(@MemoryId long memoryId,@UserMessage String userMessage);
}
