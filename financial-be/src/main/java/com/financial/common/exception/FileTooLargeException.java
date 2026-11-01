package com.financial.common.exception;

import lombok.Getter;

@Getter
public class FileTooLargeException extends RuntimeException {

    public static final String ERROR_CODE = "FILE_TOO_LARGE";
    private final String errorCode;

    public FileTooLargeException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
    }

    public FileTooLargeException(long maxMb) {
        super(String.format("File size exceeds maximum allowed limit of %d MB", maxMb));
        this.errorCode = ERROR_CODE;
    }
}

