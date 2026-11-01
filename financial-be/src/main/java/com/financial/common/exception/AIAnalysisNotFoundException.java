package com.financial.common.exception;

import lombok.Getter;

@Getter
public class AIAnalysisNotFoundException extends RuntimeException {

    private final String errorCode;

    public AIAnalysisNotFoundException(String message) {
        super(message);
        this.errorCode = "AI_ANALYSIS_NOT_FOUND";
    }

    public AIAnalysisNotFoundException(Long id) {
        super("AI analysis not found with ID: " + id);
        this.errorCode = "AI_ANALYSIS_NOT_FOUND";
    }
}

