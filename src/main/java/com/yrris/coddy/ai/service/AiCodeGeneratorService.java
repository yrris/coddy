package com.yrris.coddy.ai.service;

import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    HtmlCodeResult generateHtmlCode(String userMessage);

    MultiFileCodeResult generateMultiFileCode(String userMessage);

    Flux<String> generateHtmlCodeStream(String userMessage);

    Flux<String> generateMultiFileCodeStream(String userMessage);

    Flux<String> generateHtmlCodeStream(long appId, String userMessage);

    Flux<String> generateMultiFileCodeStream(long appId, String userMessage);

    Flux<String> generateReactViteProjectStream(long appId, String userMessage);

    default String generateRawCodeForStream(String userMessage, CodeGenTypeEnum codeGenType) {
        Flux<String> stream = switch (codeGenType) {
            case HTML_SINGLE -> generateHtmlCodeStream(userMessage);
            case HTML_MULTI -> generateMultiFileCodeStream(userMessage);
            case REACT_VITE -> throw new UnsupportedOperationException(
                    "REACT_VITE uses tool-call streaming, not raw code stream");
        };
        return stream.collectList().map(parts -> String.join("", parts)).blockOptional().orElse("");
    }
}
