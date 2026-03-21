package com.yrris.coddy.model.dto.app;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AppDeployRequest {

    @NotNull(message = "App id is required")
    @Positive(message = "App id must be positive")
    private Long appId;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
