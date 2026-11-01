package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vietnamese Accounting Standard Circular")
public enum AccountingStandard {
    @Schema(description = "Circular 99/2000/TT-BTC (Historical standard)")
    TT99,

    @Schema(description = "Circular 200/2014/TT-BTC (Applicable for medium and large enterprises)")
    TT200,

    @Schema(description = "Circular 133/2016/TT-BTC (Applicable for small and medium enterprises - SMEs)")
    TT133;

    public static AccountingStandard fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Accounting standard cannot be null or empty");
        }
        String normalized = value.trim().toUpperCase();
        for (AccountingStandard standard : values()) {
            if (standard.name().equals(normalized)) {
                return standard;
            }
        }
        throw new IllegalArgumentException("Unsupported accounting standard: " + value);
    }
}

