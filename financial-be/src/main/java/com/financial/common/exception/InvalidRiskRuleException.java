package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidRiskRuleException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_RISK_RULE";
    private final String errorCode;

    public InvalidRiskRuleException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

