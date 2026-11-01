package com.financial.modules.assessment.dto;

import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
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
@Schema(description = "Category-level aggregated assessment evaluation summary")
public class AssessmentCategoryResponse {

    @Schema(description = "Category score record ID", example = "1")
    private Long id;

    @Schema(description = "Category code", example = "LIQUIDITY")
    private FinancialRatioCategory category;

    @Schema(description = "Category display name", example = "Khả năng thanh toán")
    private String categoryName;

    @Schema(description = "Total financial ratios defined in this category", example = "3")
    private Integer totalRatios;

    @Schema(description = "Number of ratios passing configured threshold", example = "1")
    private Integer passedRatios;

    @Schema(description = "Number of ratios failing configured threshold", example = "0")
    private Integer failedRatios;

    @Schema(description = "Number of ratios with missing or insufficient data", example = "0")
    private Integer insufficientDataRatios;

    @Schema(description = "Number of ratios without configured active criteria", example = "2")
    private Integer notConfiguredRatios;

    @Schema(description = "Weighted score for this category if scoring is configured", example = "null")
    private BigDecimal categoryScore;

    @Schema(description = "Scoring configuration status for this category", example = "NOT_CONFIGURED")
    private AssessmentScoringStatus scoringStatus;
}

