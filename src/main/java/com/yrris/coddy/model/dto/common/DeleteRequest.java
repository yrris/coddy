package com.yrris.coddy.model.dto.common;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DeleteRequest {

    @NotNull(message = "Id is required")
    @Positive(message = "Id must be positive")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
