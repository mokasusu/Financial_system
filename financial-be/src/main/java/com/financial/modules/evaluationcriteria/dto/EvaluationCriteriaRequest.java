package com.financial.modules.evaluationcriteria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Evaluation criterion creation request")
public class EvaluationCriteriaRequest {

    @NotBlank(message = "Ratio code is required")
    @Schema(description = "Target financial ratio code", example = "CURRENT_RATIO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ratioCode;

    @NotBlank(message = "Criterion name is required")
    @Schema(description = "Descriptive name for the criterion", example = "Current Ratio Standard Threshold", requiredMode = Schema.RequiredMode.REQUIRED)
    private String criterionName;

    @NotNull(message = "Comparison operator is required")
    @Schema(description = "Comparison operator (GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, BETWEEN)", example = "GREATER_THAN_OR_EQUAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Threshold value for single-value comparison operators", example = "1.5")
    private BigDecimal thresholdValue;

    @Schema(description = "Minimum threshold boundary for BETWEEN operator", example = "1.0")
    private BigDecimal minimumValue;

    @Schema(description = "Maximum threshold boundary for BETWEEN operator", example = "3.0")
    private BigDecimal maximumValue;

    @NotNull(message = "Financial unit is required")
    @Schema(description = "Measurement unit of threshold", example = "TIMES", requiredMode = Schema.RequiredMode.REQUIRED)
    private FinancialUnit unit;

    @Schema(description = "Detailed description or guidance for this criterion", example = "Current assets should be at least 1.5x current liabilities")
    private String description;

    @Builder.Default
    @Schema(description = "Whether the criterion is immediately active", example = "true")
    private Boolean isActive = true;
}

