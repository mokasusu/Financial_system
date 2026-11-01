package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidStatementStatusException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_STATEMENT_STATUS";
    private final String errorCode;

    public InvalidStatementStatusException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

