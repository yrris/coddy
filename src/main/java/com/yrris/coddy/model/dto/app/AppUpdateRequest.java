package com.yrris.coddy.model.dto.app;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AppUpdateRequest {

    @NotNull(message = "Id is required")
    @Positive(message = "Id must be positive")
    private Long id;

    @Size(max = 128, message = "App name is too long")
    private String appName;

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
}
