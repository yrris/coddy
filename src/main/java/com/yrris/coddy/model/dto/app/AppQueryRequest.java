package com.yrris.coddy.model.dto.app;

import com.yrris.coddy.model.dto.common.PageQueryRequest;

public class AppQueryRequest extends PageQueryRequest {

    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Integer priority;

    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

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

    public String getDeployKey() {
        return deployKey;
    }

    public void setDeployKey(String deployKey) {
        this.deployKey = deployKey;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
