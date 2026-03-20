package com.yrris.coddy.ai.facade;

import com.yrris.coddy.ai.model.CodeGenerationOutput;
import com.yrris.coddy.ai.model.HtmlCodeResult;
import com.yrris.coddy.ai.model.MultiFileCodeResult;
import com.yrris.coddy.ai.parser.CodeParserExecutor;
import com.yrris.coddy.ai.saver.CodeFileSaverExecutor;
import com.yrris.coddy.ai.service.AiCodeGeneratorService;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Facade for AI code generation orchestration.
 * It combines generation, parsing, persistence, and response packaging.
 */
@Service
public class AiCodeGeneratorFacade {

    private final AiCodeGeneratorService aiCodeGeneratorService;

    private final CodeParserExecutor codeParserExecutor;

    private final CodeFileSaverExecutor codeFileSaverExecutor;

    public AiCodeGeneratorFacade(
            AiCodeGeneratorService aiCodeGeneratorService,
            CodeParserExecutor codeParserExecutor,
            CodeFileSaverExecutor codeFileSaverExecutor
    ) {
        this.aiCodeGeneratorService = aiCodeGeneratorService;
        this.codeParserExecutor = codeParserExecutor;
        this.codeFileSaverExecutor = codeFileSaverExecutor;
    }

    public CodeGenerationOutput generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML_SINGLE -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield saveStructuredResult(result, CodeGenTypeEnum.HTML_SINGLE);
            }
            case HTML_MULTI -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield saveStructuredResult(result, CodeGenTypeEnum.HTML_MULTI);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported code generation type");
        };
    }

    /**
     * Deprecated
     * @param userMessage
     * @param codeGenType
     * @param chunkConsumer
     * @return
     */
    public CodeGenerationOutput generateAndSaveCodeStream(
            String userMessage,
            CodeGenTypeEnum codeGenType,
            Consumer<String> chunkConsumer
    ) {
        Flux<String> codeStream = switch (codeGenType) {
            case HTML_SINGLE -> aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
            case HTML_MULTI -> aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported code generation type");
        };

        StringBuilder rawResponseBuilder = new StringBuilder();
        CountDownLatch streamLatch = new CountDownLatch(1);
        final Throwable[] streamError = new Throwable[1];

        codeStream
                .doOnNext(chunk -> {
                    // Collect full stream content for final parsing and file saving.
                    rawResponseBuilder.append(chunk);
                    if (chunkConsumer != null) {
                        chunkConsumer.accept(chunk);
                    }
                })
                .doOnError(error -> {
                    streamError[0] = error;
                    streamLatch.countDown();
                })
                .doOnComplete(streamLatch::countDown)
                .subscribe();

        waitForStreamCompletion(streamLatch);
        if (streamError[0] != null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI stream generation failed");
        }

        String rawResponse = rawResponseBuilder.toString();
        return parseAndSave(rawResponse, codeGenType);
    }


    public void generateAndSaveCodeStreamAsync(
            String userMessage,
            CodeGenTypeEnum codeGenType,
            Consumer<String> onChunk,
            Consumer<CodeGenerationOutput> onComplete,
            Consumer<Throwable> onError
    ) {
        Flux<String> codeStream;
        try {
            codeStream = switch (codeGenType) {
                case HTML_SINGLE -> aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                case HTML_MULTI -> aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported code generation type");
            };
        } catch (Exception e) {
            if (onError != null) onError.accept(e);
            return;
        }

        StringBuilder rawResponseBuilder = new StringBuilder();
        // Project Reactor subscribe
        codeStream.subscribe(
                // doOnNext
                chunk -> {
                    rawResponseBuilder.append(chunk);
                    if (onChunk != null) {
                        onChunk.accept(chunk);
                    }
                },
                // doOnError
                error -> {
                    if (onError != null) {
                        onError.accept(error);
                    }
                },
                // doOnComplete
                () -> {
                    try {
                        String rawResponse = rawResponseBuilder.toString();
                        // after streaming, parse and save files
                        CodeGenerationOutput output = parseAndSave(rawResponse, codeGenType);
                        if (onComplete != null) {
                            onComplete.accept(output);
                        }
                    } catch (Exception e) {
                        if (onError != null) {
                            onError.accept(e);
                        }
                    }
                }
        );
    }

    private CodeGenerationOutput parseAndSave(String rawResponse, CodeGenTypeEnum codeGenType) {
        Object parsedResult = codeParserExecutor.executeParser(rawResponse, codeGenType);
        File savedDir = codeFileSaverExecutor.executeSaver(parsedResult, codeGenType);

        CodeGenerationOutput output = new CodeGenerationOutput();
        output.setCodeGenType(codeGenType);
        output.setOutputDir(savedDir.getAbsolutePath());
        output.setFiles(buildFileMap(parsedResult, codeGenType));
        output.setRawResponse(rawResponse);
        return output;
    }

    private CodeGenerationOutput saveStructuredResult(Object structuredResult, CodeGenTypeEnum codeGenType) {
        File savedDir = codeFileSaverExecutor.executeSaver(structuredResult, codeGenType);
        Map<String, String> files = buildFileMap(structuredResult, codeGenType);

        CodeGenerationOutput output = new CodeGenerationOutput();
        output.setCodeGenType(codeGenType);
        output.setOutputDir(savedDir.getAbsolutePath());
        output.setFiles(files);
        output.setRawResponse(buildRawResponse(files, codeGenType));
        return output;
    }

    private Map<String, String> buildFileMap(Object parsedResult, CodeGenTypeEnum codeGenType) {
        Map<String, String> fileMap = new LinkedHashMap<>();
        switch (codeGenType) {
            case HTML_SINGLE -> {
                HtmlCodeResult result = (HtmlCodeResult) parsedResult;
                fileMap.put("index.html", result.getHtmlCode());
            }
            case HTML_MULTI -> {
                MultiFileCodeResult result = (MultiFileCodeResult) parsedResult;
                fileMap.put("index.html", result.getHtmlCode());
                fileMap.put("style.css", result.getCssCode());
                fileMap.put("script.js", result.getJsCode());
            }
            default -> throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unsupported code generation type");
        }
        return fileMap;
    }

    private String buildRawResponse(Map<String, String> files, CodeGenTypeEnum codeGenType) {
        if (codeGenType == CodeGenTypeEnum.HTML_SINGLE) {
            return files.getOrDefault("index.html", "");
        }
        return "```html\n"
                + files.getOrDefault("index.html", "")
                + "\n```\n```css\n"
                + files.getOrDefault("style.css", "")
                + "\n```\n```javascript\n"
                + files.getOrDefault("script.js", "")
                + "\n```";
    }

    private void waitForStreamCompletion(CountDownLatch streamLatch) {
        try {
            boolean completed = streamLatch.await(180, TimeUnit.SECONDS);
            if (!completed) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI stream timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "AI stream interrupted");
        }
    }
}
