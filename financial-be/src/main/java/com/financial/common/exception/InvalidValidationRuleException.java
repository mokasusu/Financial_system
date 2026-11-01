package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidValidationRuleException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_VALIDATION_RULE";
    private final String errorCode;

    public InvalidValidationRuleException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

