package com.yrris.coddy.common;

import java.time.Instant;

public final class ResultUtils {

    private ResultUtils() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data, Instant.now());
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now());
    }
}
