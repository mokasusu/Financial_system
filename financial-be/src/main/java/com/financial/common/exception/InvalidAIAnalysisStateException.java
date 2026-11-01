package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidAIAnalysisStateException extends RuntimeException {

    private final String errorCode;

    public InvalidAIAnalysisStateException(String message) {
        super(message);
        this.errorCode = "INVALID_AI_ANALYSIS_STATE";
    }

    public InvalidAIAnalysisStateException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

