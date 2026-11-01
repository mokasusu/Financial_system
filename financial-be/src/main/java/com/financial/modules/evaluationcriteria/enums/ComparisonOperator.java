package com.financial.modules.evaluationcriteria.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comparison operator for evaluating financial metrics against thresholds")
public enum ComparisonOperator {
    @Schema(description = "Strictly greater than threshold (>)")
    GREATER_THAN,

    @Schema(description = "Greater than or equal to threshold (>=)")
    GREATER_THAN_OR_EQUAL,

    @Schema(description = "Strictly less than threshold (<)")
    LESS_THAN,

    @Schema(description = "Less than or equal to threshold (<=)")
    LESS_THAN_OR_EQUAL,

    @Schema(description = "Equal to threshold (==)")
    EQUAL,

    @Schema(description = "Value falls within inclusive range [minimumValue, maximumValue]")
    BETWEEN
}

