package com.financial.common.exception;

import lombok.Getter;

@Getter
public class UnsupportedFileTypeException extends RuntimeException {

    public static final String ERROR_CODE = "UNSUPPORTED_FILE_TYPE";
    private final String errorCode;

    public UnsupportedFileTypeException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }
}

