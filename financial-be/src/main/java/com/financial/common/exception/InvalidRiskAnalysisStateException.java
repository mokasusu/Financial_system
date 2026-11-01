package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidRiskAnalysisStateException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_RISK_ANALYSIS_STATE";
    private final String errorCode;

    public InvalidRiskAnalysisStateException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

