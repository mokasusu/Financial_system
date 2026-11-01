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
@Schema(description = "Identified financial risk detail item")
public class DashboardIdentifiedRiskDTO {

    @Schema(description = "Risk ID", example = "101")
    private Long riskId;

    @Schema(description = "Risk category", example = "LIQUIDITY")
    private String category;

    @Schema(description = "Risk title", example = "Rủi ro thanh khoản ngắn hạn")
    private String title;

    @Schema(description = "Risk detailed description")
    private String description;

    @Schema(description = "Authoritative severity", example = "HIGH")
    private String severity;

    @Schema(description = "Risk status", example = "DETECTED")
    private String status;

    @Schema(description = "Supporting evidence")
    private String evidence;

    @Schema(description = "Related ratio code if any", example = "CURRENT_RATIO")
    private String relatedRatioCode;

    @Schema(description = "Actual ratio value", example = "0.8500")
    private BigDecimal actualValue;

    @Schema(description = "Configured threshold value", example = "1.5000")
    private BigDecimal thresholdValue;

    @Schema(description = "Actionable mitigation recommendation")
    private String recommendation;
}

