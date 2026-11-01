package com.financial.common.exception;

import lombok.Getter;

@Getter
public class UpstreamDataUnavailableException extends RuntimeException {

    private final String errorCode;
    private final String missingComponent;

    public UpstreamDataUnavailableException(String missingComponent, String message) {
        super(message);
        this.errorCode = "UPSTREAM_DATA_UNAVAILABLE";
        this.missingComponent = missingComponent;
    }

    public UpstreamDataUnavailableException(String message) {
        super(message);
        this.errorCode = "UPSTREAM_DATA_UNAVAILABLE";
        this.missingComponent = "UNKNOWN";
    }
}

