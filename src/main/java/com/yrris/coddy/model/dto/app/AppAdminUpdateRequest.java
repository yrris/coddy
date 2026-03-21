package com.yrris.coddy.model.dto.app;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AppAdminUpdateRequest {

    @NotNull(message = "Id is required")
    @Positive(message = "Id must be positive")
    private Long id;

    @Size(max = 128, message = "App name is too long")
    private String appName;

    @Size(max = 1024, message = "Cover URL is too long")
    private String cover;

    private Integer priority;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
