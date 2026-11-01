package com.financial.modules.financialindicator.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Measurement unit for financial indicators and ratios")
public enum FinancialUnit {
    @Schema(description = "Vietnamese Dong")
    VND,

    @Schema(description = "Percentage (%)")
    PERCENT,

    @Schema(description = "Number of times / multiplier")
    TIMES
}

