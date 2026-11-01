package com.financial.common.exception;

import lombok.Getter;

@Getter
public class DuplicateValidationRuleException extends RuntimeException {

    public static final String ERROR_CODE = "DUPLICATE_VALIDATION_RULE";
    private final String errorCode;

    public DuplicateValidationRuleException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

