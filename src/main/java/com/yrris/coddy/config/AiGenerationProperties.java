package com.yrris.coddy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiGenerationProperties {

    private String provider = "mock";

    private String baseUrl = "https://api.openai.com/v1";

    private String apiKey;

    private String model = "gpt-4o-mini";

    private int maxTokens = 4096;

    private double temperature = 0.2;

    private int streamChunkSize = 64;

    private long streamDelayMs = 20L;

    private String outputRootDir = "tmp/code_output";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getStreamChunkSize() {
        return streamChunkSize;
    }

    public void setStreamChunkSize(int streamChunkSize) {
        this.streamChunkSize = streamChunkSize;
    }

    public long getStreamDelayMs() {
        return streamDelayMs;
    }

    public void setStreamDelayMs(long streamDelayMs) {
        this.streamDelayMs = streamDelayMs;
    }

    public String getOutputRootDir() {
        return outputRootDir;
    }

    public void setOutputRootDir(String outputRootDir) {
        this.outputRootDir = outputRootDir;
    }
}
