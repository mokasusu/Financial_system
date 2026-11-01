package com.financial.common.exception;

import lombok.Getter;

@Getter
public class ConcurrentAIAnalysisException extends RuntimeException {

    private final String errorCode;
    private final Long assessmentId;

    public ConcurrentAIAnalysisException(Long assessmentId, String message) {
        super(message);
        this.errorCode = "CONCURRENT_AI_ANALYSIS_IN_PROGRESS";
        this.assessmentId = assessmentId;
    }
}

