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
@Schema(description = "Historical financial trend data point")
public class TrendDataPointDTO {

    @Schema(description = "Reporting period", example = "2024")
    private String reportingPeriod;

    @Schema(description = "Financial statement ID", example = "10")
    private Long financialStatementId;

    @Schema(description = "Calculation ID", example = "50")
    private Long calculationId;

    @Schema(description = "Ratio/KPI code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Ratio descriptive name", example = "Khả năng thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "Analytical category", example = "LIQUIDITY")
    private String category;

    @Schema(description = "Calculated numeric value", example = "1.8500")
    private BigDecimal value;

    @Schema(description = "Unit of measurement", example = "TIMES")
    private String unit;

    @Schema(description = "Calculation status", example = "SUCCESS")
    private String calculationStatus;
}

