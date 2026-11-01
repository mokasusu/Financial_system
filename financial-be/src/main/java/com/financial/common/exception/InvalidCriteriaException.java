package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidCriteriaException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_CRITERIA_CONFIGURATION";
    private final String errorCode;

    public InvalidCriteriaException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public InvalidCriteriaException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : ERROR_CODE;
    }
}

