package com.financial.common.exception;

import lombok.Getter;

@Getter
public class RiskAnalysisNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "RISK_ANALYSIS_NOT_FOUND";
    private final String errorCode;

    public RiskAnalysisNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public RiskAnalysisNotFoundException(Long id) {
        super("Financial risk analysis not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

