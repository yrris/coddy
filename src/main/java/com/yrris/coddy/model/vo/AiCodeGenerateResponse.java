package com.yrris.coddy.model.vo;

import com.yrris.coddy.ai.model.CodeGenerationOutput;

import java.util.LinkedHashMap;
import java.util.Map;

public class AiCodeGenerateResponse {

    private String codeGenType;

    private String outputDir;

    private Map<String, String> files;

    public static AiCodeGenerateResponse fromOutput(CodeGenerationOutput output) {
        AiCodeGenerateResponse response = new AiCodeGenerateResponse();
        response.setCodeGenType(output.getCodeGenType().getValue());
        response.setOutputDir(output.getOutputDir());
        response.setFiles(new LinkedHashMap<>(output.getFiles()));
        return response;
    }

    public String getCodeGenType() {
        return codeGenType;
    }

    public void setCodeGenType(String codeGenType) {
        this.codeGenType = codeGenType;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
}
