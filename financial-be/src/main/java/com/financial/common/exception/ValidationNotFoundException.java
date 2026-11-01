package com.financial.common.exception;

import lombok.Getter;

@Getter
public class ValidationNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "VALIDATION_NOT_FOUND";
    private final String errorCode;

    public ValidationNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public ValidationNotFoundException(Long id) {
        super("Validation execution not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

