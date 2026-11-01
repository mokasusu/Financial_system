package com.financial.common.exception;

import lombok.Getter;

@Getter
public class InvalidFileException extends RuntimeException {

    public static final String ERROR_CODE = "INVALID_FILE";
    private final String errorCode;

    public InvalidFileException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

