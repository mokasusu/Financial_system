package com.financial.common.exception;

import lombok.Getter;

@Getter
public class OcrApiException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;

    public OcrApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 502;
    }

    public OcrApiException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public OcrApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = 502;
    }
}

