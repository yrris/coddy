package com.yrris.coddy.ai.model;

import com.yrris.coddy.model.enums.CodeGenTypeEnum;

import java.util.Map;

public class CodeGenerationOutput {

    private CodeGenTypeEnum codeGenType;

    private String outputDir;

    private Map<String, String> files;

    private String rawResponse;

    public CodeGenTypeEnum getCodeGenType() {
        return codeGenType;
    }

    public void setCodeGenType(CodeGenTypeEnum codeGenType) {
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

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}
