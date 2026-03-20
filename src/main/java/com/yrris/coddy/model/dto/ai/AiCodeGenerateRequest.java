package com.yrris.coddy.model.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiCodeGenerateRequest {

    @NotBlank(message = "Prompt is required")
    @Size(max = 4000, message = "Prompt is too long")
    private String prompt;

    @NotBlank(message = "Code generation type is required")
    private String codeGenType;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getCodeGenType() {
        return codeGenType;
    }

    public void setCodeGenType(String codeGenType) {
        this.codeGenType = codeGenType;
    }
}
