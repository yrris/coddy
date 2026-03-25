package com.yrris.coddy.ai.service;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

/**
 * LangChain4j declarative AI service interface.
 *
 * The structured methods are used for stable file saving,
 * while stream methods are used for SSE output.
 */
public interface LangChain4jCodeGeneratorAgent {

    @SystemMessage(fromResource = "prompt/codegen-html-single-structured-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(@MemoryId long memoryId, String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-html-multi-structured-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(@MemoryId long memoryId, String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-html-single-stream-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(@MemoryId long memoryId, String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-html-multi-stream-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(@MemoryId long memoryId, String userMessage);
}
