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
@Schema(description = "Core financial KPI scoreboard item")
public class DashboardKPIResponse {

    @Schema(description = "KPI code", example = "CURRENT_RATIO")
    private String code;

    @Schema(description = "KPI descriptive name", example = "Khả năng thanh toán hiện hành")
    private String name;

    @Schema(description = "Actual calculated numeric value", example = "1.8500")
    private BigDecimal value;

    @Schema(description = "Unit of measurement", example = "TIMES")
    private String unit;

    @Schema(description = "Calculation engine status", example = "SUCCESS")
    private String calculationStatus;

    @Schema(description = "Assessment evaluation status against criteria", example = "PASS")
    private String evaluationStatus;

    @Schema(description = "Configured threshold value from Evaluation Criteria", example = "1.5000")
    private BigDecimal configuredThreshold;

    @Schema(description = "Comparison operator", example = ">=")
    private String comparisonOperator;

    @Schema(description = "Detailed explanation or missing-data reason", example = "Chỉ số đạt tiêu chuẩn an toàn thanh khoản")
    private String explanation;
}

