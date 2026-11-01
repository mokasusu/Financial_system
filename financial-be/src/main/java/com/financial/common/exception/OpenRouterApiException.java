package com.financial.common.exception;

import lombok.Getter;

@Getter
public class OpenRouterApiException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    public OpenRouterApiException(String message) {
        super(message);
        this.errorCode = "OPENROUTER_API_ERROR";
        this.statusCode = 502;
    }

    public OpenRouterApiException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public OpenRouterApiException(String errorCode, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}

