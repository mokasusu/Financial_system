package com.financial.common.exception;

import lombok.Getter;

@Getter
public class StatementNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "STATEMENT_NOT_FOUND";
    private final String errorCode;

    public StatementNotFoundException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public StatementNotFoundException(Long id) {
        super("Financial statement not found with ID: " + id);
        this.errorCode = ERROR_CODE;
    }
}

