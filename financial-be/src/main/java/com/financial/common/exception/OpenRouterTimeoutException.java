package com.financial.common.exception;

import lombok.Getter;

@Getter
public class OpenRouterTimeoutException extends RuntimeException {

    private final String errorCode;
    private final long timeoutSeconds;

    public OpenRouterTimeoutException(long timeoutSeconds) {
        super(String.format("OpenRouter AI processing exceeded the configured timeout of %d seconds.", timeoutSeconds));
        this.errorCode = "OPENROUTER_TIMEOUT";
        this.timeoutSeconds = timeoutSeconds;
    }

    public OpenRouterTimeoutException(long timeoutSeconds, Throwable cause) {
        super(String.format("OpenRouter AI processing exceeded the configured timeout of %d seconds.", timeoutSeconds), cause);
        this.errorCode = "OPENROUTER_TIMEOUT";
        this.timeoutSeconds = timeoutSeconds;
    }

    public OpenRouterTimeoutException(String message) {
        super(message);
        this.errorCode = "OPENROUTER_TIMEOUT";
        this.timeoutSeconds = 120L;
    }
}

