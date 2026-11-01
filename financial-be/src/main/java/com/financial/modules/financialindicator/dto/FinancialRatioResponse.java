package com.financial.modules.financialindicator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.EvaluationDirection;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Financial ratio definition response")
public class FinancialRatioResponse {

    @Schema(description = "Unique ratio identifier", example = "1")
    private Long id;

    @Schema(description = "Unique semantic code of the financial ratio", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Vietnamese ratio name", example = "Chỉ số thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "English ratio name", example = "Current Ratio")
    private String ratioNameEn;

    @Schema(description = "Ratio category", example = "LIQUIDITY")
    private FinancialRatioCategory category;

    @Schema(description = "Formula mathematical expression", example = "CURRENT_ASSETS / CURRENT_LIABILITIES")
    private String formulaExpression;

    @Schema(description = "Measurement unit", example = "TIMES")
    private FinancialUnit unit;

    @Schema(description = "Financial evaluation direction", example = "HIGHER_IS_BETTER")
    private EvaluationDirection evaluationDirection;

    @Schema(description = "Decimal scale precision for formatting", example = "2")
    private Integer decimalScale;

    @Schema(description = "Whether this ratio is one of the core corporate health KPIs", example = "true")
    private Boolean isCoreKpi;

    @Schema(description = "Detailed ratio description")
    private String description;

    @Schema(description = "Whether the ratio definition is active", example = "true")
    private Boolean isActive;

    @Schema(description = "List of formula input dependencies")
    private List<FinancialRatioInputResponse> inputs;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

