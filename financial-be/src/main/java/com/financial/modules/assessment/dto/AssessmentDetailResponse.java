package com.financial.modules.assessment.dto;

import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
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
@Schema(description = "Individual financial ratio evaluation details against configured criterion")
public class AssessmentDetailResponse {

    @Schema(description = "Assessment detail ID", example = "101")
    private Long id;

    @Schema(description = "Ratio code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Ratio name", example = "Hệ số khả năng thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "Financial category", example = "LIQUIDITY")
    private FinancialRatioCategory category;

    @Schema(description = "Calculated ratio value from confirmed statement data", example = "1.8500")
    private BigDecimal calculatedValue;

    @Schema(description = "Ratio measurement unit", example = "TIMES")
    private FinancialUnit unit;

    @Schema(description = "Display name of measurement unit", example = "Lần")
    private String unitDisplayName;

    @Schema(description = "Associated criterion ID", example = "1")
    private Long criterionId;

    @Schema(description = "Criterion name snapshot", example = "Current Ratio Standard Threshold")
    private String criterionName;

    @Schema(description = "Comparison operator used for evaluation", example = "GREATER_THAN_OR_EQUAL")
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Symbol of comparison operator", example = ">=")
    private String comparisonOperatorSymbol;

    @Schema(description = "Configured threshold value", example = "1.5000")
    private BigDecimal thresholdValue;

    @Schema(description = "Configured minimum value for range evaluation", example = "null")
    private BigDecimal minimumValue;

    @Schema(description = "Configured maximum value for range evaluation", example = "null")
    private BigDecimal maximumValue;

    @Schema(description = "Evaluation outcome status (PASS, FAIL, INSUFFICIENT_DATA, NOT_CONFIGURED, NOT_APPLICABLE)", example = "PASS")
    private RatioEvaluationStatus evaluationStatus;

    @Schema(description = "Detailed evaluation explanation and outcome rationale", example = "Chỉ số Hệ số khả năng thanh toán hiện hành đạt chuẩn (1.8500 >= 1.5000 Lần)")
    private String evaluationExplanation;
}

