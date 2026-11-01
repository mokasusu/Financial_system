package com.financial.common.exception;

import lombok.Getter;

@Getter
public class RiskRuleNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "RISK_RULE_NOT_FOUND";
    private final String errorCode;

    public RiskRuleNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public RiskRuleNotFoundException(Long id) {
        super("Risk rule not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

