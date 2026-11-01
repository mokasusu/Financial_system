package com.financial.common.exception;

import lombok.Getter;

@Getter
public class UnconfirmedStatementException extends RuntimeException {

    public static final String ERROR_CODE = "STATEMENT_NOT_CONFIRMED";
    private final String errorCode;

    public UnconfirmedStatementException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public UnconfirmedStatementException(Long statementId, String status) {
        super("Financial statement ID " + statementId + " cannot be calculated because it is not confirmed. Current status: " + status);
        this.errorCode = ERROR_CODE;
    }
}

