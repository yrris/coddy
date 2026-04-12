package com.yrris.coddy.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class ChatModelConfig {

    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer maxCompletionTokens;
    private Double temperature;
    private boolean logRequests;
    private boolean logResponses;
    private boolean strictJsonSchema;
    private String responseFormat;

    @Bean
    @Scope("prototype")
    public ChatModel chatModelPrototype() {
        var builder = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxCompletionTokens(maxCompletionTokens)
                .temperature(temperature)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .strictJsonSchema(strictJsonSchema);
        if ("json_object".equals(responseFormat)) {
            builder.responseFormat("json_object");
        }
        return builder.build();
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getMaxCompletionTokens() { return maxCompletionTokens; }
    public void setMaxCompletionTokens(Integer maxCompletionTokens) { this.maxCompletionTokens = maxCompletionTokens; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public boolean isLogRequests() { return logRequests; }
    public void setLogRequests(boolean logRequests) { this.logRequests = logRequests; }
    public boolean isLogResponses() { return logResponses; }
    public void setLogResponses(boolean logResponses) { this.logResponses = logResponses; }
    public boolean isStrictJsonSchema() { return strictJsonSchema; }
    public void setStrictJsonSchema(boolean strictJsonSchema) { this.strictJsonSchema = strictJsonSchema; }
    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }
}
