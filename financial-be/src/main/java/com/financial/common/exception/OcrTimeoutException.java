package com.financial.common.exception;

import lombok.Getter;

@Getter
public class OcrTimeoutException extends RuntimeException {

    public static final String ERROR_CODE = "OCR_TIMEOUT";
    private final String errorCode;
    private final long timeoutSeconds;

    public OcrTimeoutException(long timeoutSeconds) {
        super(String.format("OCR processing exceeded the configured timeout of %d seconds.", timeoutSeconds));
        this.errorCode = ERROR_CODE;
        this.timeoutSeconds = timeoutSeconds;
    }

    public OcrTimeoutException(long timeoutSeconds, Throwable cause) {
        super(String.format("OCR processing exceeded the configured timeout of %d seconds.", timeoutSeconds), cause);
        this.errorCode = ERROR_CODE;
        this.timeoutSeconds = timeoutSeconds;
    }

    public OcrTimeoutException(String message) {
        super(message);
        this.errorCode = ERROR_CODE;
        this.timeoutSeconds = 600L;
    }

    public OcrTimeoutException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERROR_CODE;
        this.timeoutSeconds = 600L;
    }
}

