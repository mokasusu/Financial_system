package com.financial.modules.evaluationcriteria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Evaluation criterion response payload")
public class EvaluationCriteriaResponse {

    @Schema(description = "Unique criterion identifier", example = "1")
    private Long id;

    @Schema(description = "Associated financial ratio definition ID", example = "1")
    private Long ratioDefinitionId;

    @Schema(description = "Associated financial ratio semantic code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Associated financial ratio name", example = "Chỉ số thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "Criterion descriptive name", example = "Current Ratio Standard Threshold")
    private String criterionName;

    @Schema(description = "Comparison operator", example = "GREATER_THAN_OR_EQUAL")
    private ComparisonOperator comparisonOperator;

    @Schema(description = "Single-value threshold", example = "1.5")
    private BigDecimal thresholdValue;

    @Schema(description = "Minimum range threshold (when BETWEEN)", example = "null")
    private BigDecimal minimumValue;

    @Schema(description = "Maximum range threshold (when BETWEEN)", example = "null")
    private BigDecimal maximumValue;

    @Schema(description = "Measurement unit", example = "TIMES")
    private FinancialUnit unit;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Whether the criterion is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

