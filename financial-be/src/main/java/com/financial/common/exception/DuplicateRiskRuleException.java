package com.financial.common.exception;

import lombok.Getter;

@Getter
public class DuplicateRiskRuleException extends RuntimeException {

    public static final String ERROR_CODE = "DUPLICATE_RISK_RULE";
    private final String errorCode;

    public DuplicateRiskRuleException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

