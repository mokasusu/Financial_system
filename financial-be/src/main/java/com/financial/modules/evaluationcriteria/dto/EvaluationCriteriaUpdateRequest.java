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
@Schema(description = "Evaluation criterion update request")
public class EvaluationCriteriaUpdateRequest {

    @NotBlank(message = "Criterion name is required")
    @Schema(description = "Descriptive name for the criterion", example = "Updated Current Ratio Threshold", requiredMode = Schema.RequiredMode.REQUIRED)
    private String criterionName;

    @NotNull(message = "Comparison operator is required")
    @Schema(description = "Comparison operator", example = "GREATER_THAN_OR_EQUAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Threshold value for single-value comparison operators", example = "1.8")
    private BigDecimal thresholdValue;

    @Schema(description = "Minimum threshold boundary for BETWEEN operator", example = "1.2")
    private BigDecimal minimumValue;

    @Schema(description = "Maximum threshold boundary for BETWEEN operator", example = "3.5")
    private BigDecimal maximumValue;

    @NotNull(message = "Financial unit is required")
    @Schema(description = "Measurement unit", example = "TIMES", requiredMode = Schema.RequiredMode.REQUIRED)
    private FinancialUnit unit;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}

