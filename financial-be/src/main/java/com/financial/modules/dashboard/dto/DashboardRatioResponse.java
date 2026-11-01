package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Financial ratio detail item")
public class DashboardRatioResponse {

    @Schema(description = "Ratio code", example = "QUICK_RATIO")
    private String code;

    @Schema(description = "Ratio descriptive name", example = "Khả năng thanh toán nhanh")
    private String name;

    @Schema(description = "Category code", example = "LIQUIDITY")
    private String category;

    @Schema(description = "Localized category display name", example = "Thanh khoản")
    private String categoryDisplayName;

    @Schema(description = "Actual calculated numeric value", example = "1.2000")
    private BigDecimal value;

    @Schema(description = "Unit of measurement", example = "TIMES")
    private String unit;

    @Schema(description = "Calculation engine status", example = "SUCCESS")
    private String calculationStatus;

    @Schema(description = "Assessment evaluation status", example = "PASS")
    private String evaluationStatus;

    @Schema(description = "Configured threshold value", example = "1.0000")
    private BigDecimal threshold;

    @Schema(description = "Comparison operator", example = ">=")
    private String comparisonOperator;

    @Schema(description = "Explanation when data is missing or calculation failed")
    private String missingDataExplanation;
}

