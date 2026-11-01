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
@Schema(description = "Financial ratio actual value compared with configured evaluation threshold")
public class RatioComparisonDTO {

    @Schema(description = "Ratio code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Ratio descriptive name", example = "Khả năng thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "Category code", example = "LIQUIDITY")
    private String category;

    @Schema(description = "Actual calculated numeric value", example = "1.8500")
    private BigDecimal actualValue;

    @Schema(description = "Unit of measurement", example = "TIMES")
    private String unit;

    @Schema(description = "Configured threshold value from Evaluation Criteria", example = "1.5000")
    private BigDecimal configuredThreshold;

    @Schema(description = "Comparison operator", example = ">=")
    private String comparisonOperator;

    @Schema(description = "Evaluation status", example = "PASS")
    private String evaluationStatus;

    @Schema(description = "Industry benchmark value (null if real benchmark dataset is not configured)", example = "null")
    private BigDecimal industryBenchmark;

    @Schema(description = "Benchmark availability status", example = "NOT_CONFIGURED")
    private String benchmarkStatus;
}

