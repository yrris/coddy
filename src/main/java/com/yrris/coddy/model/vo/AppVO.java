package com.yrris.coddy.model.vo;

import java.time.Instant;

public class AppVO {

    private Long id;

    private String appName;

    private String cover;

    private String initPrompt;

    private String codeGenType;

    private String deployKey;

    private Instant deployedTime;

    private Integer priority;

    private Long userId;

    private Instant createTime;

    private Instant updateTime;

    private LoginUserVO user;

    private String previewKey;

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

    public Instant getDeployedTime() {
        return deployedTime;
    }

    public void setDeployedTime(Instant deployedTime) {
        this.deployedTime = deployedTime;
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

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public LoginUserVO getUser() {
        return user;
    }

    public void setUser(LoginUserVO user) {
        this.user = user;
    }

    public String getPreviewKey() {
        return previewKey;
    }

    public void setPreviewKey(String previewKey) {
        this.previewKey = previewKey;
    }
}
