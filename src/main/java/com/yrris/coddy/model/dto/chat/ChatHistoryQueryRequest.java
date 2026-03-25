package com.yrris.coddy.model.dto.chat;

import com.yrris.coddy.model.dto.common.PageQueryRequest;

public class ChatHistoryQueryRequest extends PageQueryRequest {

    private Long projectId;

    private String senderType;

    private String content;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
