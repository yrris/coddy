package com.yrris.coddy.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AppAddRequest {

    @NotBlank(message = "Initial prompt is required")
    @Size(max = 4000, message = "Initial prompt is too long")
    private String initPrompt;

    private String codeGenType;

    public String getInitPrompt() {
        return initPrompt;
    }

    public void setInitPrompt(String initPrompt) {
        this.initPrompt = initPrompt;
    }

    public String getCodeGenType() {
        return codeGenType;
    }

    public void setCodeGenType(String codeGenType) {
        this.codeGenType = codeGenType;
    }
}
