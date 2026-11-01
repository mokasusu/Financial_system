package com.financial.common.exception;

import lombok.Getter;

@Getter
public class ValidationRuleNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "VALIDATION_RULE_NOT_FOUND";
    private final String errorCode;

    public ValidationRuleNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public ValidationRuleNotFoundException(Long id) {
        super("Validation rule not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

