package com.yrris.coddy.exception;

public enum ErrorCode {

    SUCCESS(0, "OK"),
    BAD_REQUEST(40000, "Bad request"),
    PARAMS_ERROR(40001, "Invalid parameters"),
    UNAUTHORIZED(40100, "Unauthorized"),
    FORBIDDEN(40300, "Forbidden"),
    NOT_FOUND(40400, "Not found"),
    CONFLICT(40900, "Conflict"),
    TOO_MANY_REQUESTS(42900, "Too many requests"),
    INTERNAL_ERROR(50000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
