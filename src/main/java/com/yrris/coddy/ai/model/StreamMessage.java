package com.yrris.coddy.ai.model;

import java.util.Map;

/**
 * Typed SSE message for tool-call streaming mode (REACT_VITE).
 * Serialized as JSON and sent directly through SSE.
 */
public class StreamMessage {

    private String type;
    private Object data;

    public StreamMessage() {
    }

    public StreamMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public static StreamMessage aiResponse(String text) {
        return new StreamMessage("AI_RESPONSE", text);
    }

    public static StreamMessage toolExecuted(String toolName, String result) {
        return new StreamMessage("TOOL_EXECUTED", Map.of(
                "toolName", toolName,
                "result", result
        ));
    }

    public static StreamMessage complete() {
        return new StreamMessage("COMPLETE", "");
    }

    public static StreamMessage error(String message) {
        return new StreamMessage("ERROR", message);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
