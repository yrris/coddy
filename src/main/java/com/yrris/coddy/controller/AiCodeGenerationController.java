package com.yrris.coddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrris.coddy.ai.facade.AiCodeGeneratorFacade;
import com.yrris.coddy.ai.model.CodeGenerationOutput;
import com.yrris.coddy.annotation.AuthCheck;
import com.yrris.coddy.common.ApiResponse;
import com.yrris.coddy.common.ResultUtils;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.dto.ai.AiCodeGenerateRequest;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import com.yrris.coddy.model.vo.AiCodeGenerateResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/ai/codegen")
public class AiCodeGenerationController {

    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;

    private final TaskExecutor taskExecutor;

    private final ObjectMapper objectMapper;

    public AiCodeGenerationController(
            AiCodeGeneratorFacade aiCodeGeneratorFacade,
            @Qualifier("openAiStreamingChatModelTaskExecutor") TaskExecutor taskExecutor,
            ObjectMapper objectMapper
    ) {
        this.aiCodeGeneratorFacade = aiCodeGeneratorFacade;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/generate")
    @AuthCheck
    public ApiResponse<AiCodeGenerateResponse> generateCode(@Valid @RequestBody AiCodeGenerateRequest request) {
        CodeGenTypeEnum codeGenTypeEnum = parseCodeGenType(request.getCodeGenType());
        CodeGenerationOutput output = aiCodeGeneratorFacade.generateAndSaveCode(request.getPrompt(), codeGenTypeEnum);
        return ResultUtils.success(AiCodeGenerateResponse.fromOutput(output));
    }


    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthCheck
    public SseEmitter generateCodeStream(@Valid @RequestBody AiCodeGenerateRequest request) {
        CodeGenTypeEnum codeGenTypeEnum = parseCodeGenType(request.getCodeGenType());
        // 2 minutes
        SseEmitter emitter = new SseEmitter(120_000L);

        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> emitter.complete());

        // taskExecutor in case Facade block the thread
        taskExecutor.execute(() -> {
            aiCodeGeneratorFacade.generateAndSaveCodeStreamAsync(
                    request.getPrompt(),
                    codeGenTypeEnum,
                    // onChunk
                     chunk -> sendEvent(emitter, "chunk", chunk),
//                    chunk -> {
//                        System.out.println("receive: " + chunk);
//                        sendEvent(emitter, "chunk", chunk);
//                    },

                    // onComplete
                    output -> {
                        try {
                            sendEvent(emitter, "result", objectMapper.writeValueAsString(AiCodeGenerateResponse.fromOutput(output)));
                            sendEvent(emitter, "done", "done");
                        } catch (Exception e) {
                            sendEvent(emitter, "error", "AI result serialization failed: " + e.getMessage());
                        } finally {
                            // complete Emitter after whole stream process finished
                            emitter.complete();
                        }
                    },

                    // onError
                    error -> {
                        String errorMsg = error.getMessage() != null ? error.getMessage() : "Internal Error during AI generation";
                        sendEvent(emitter, "error", errorMsg);
                        // completeWithError will wake GlobalExceptionHandler
                        emitter.complete(); // error event
                    }
            );
        });

        return emitter;
    }


    private CodeGenTypeEnum parseCodeGenType(String codeGenType) {
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.fromValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported code generation type");
        }
        return codeGenTypeEnum;
    }

    private void sendEvent(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
