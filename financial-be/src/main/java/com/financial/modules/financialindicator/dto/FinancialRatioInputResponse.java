package com.financial.modules.financialindicator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.financialindicator.enums.PeriodReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Financial ratio formula input dependency response")
public class FinancialRatioInputResponse {

    @Schema(description = "Input dependency identifier", example = "1")
    private Long id;

    @Schema(description = "Associated ratio definition ID", example = "1")
    private Long ratioDefinitionId;

    @Schema(description = "Associated ratio code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Target indicator ID", example = "1")
    private Long indicatorId;

    @Schema(description = "Target indicator semantic code", example = "CURRENT_ASSETS")
    private String indicatorCode;

    @Schema(description = "Target indicator name", example = "Tài sản ngắn hạn")
    private String indicatorName;

    @Schema(description = "Variable alias used in formula expression", example = "CURRENT_ASSETS")
    private String inputAlias;

    @Schema(description = "Ordering sequence of the input variable", example = "1")
    private Integer inputOrder;

    @Schema(description = "Whether this input variable is mandatory", example = "true")
    private Boolean required;

    @Schema(description = "Reporting period reference (CURRENT, BEGINNING, ENDING, AVERAGE)", example = "CURRENT")
    private PeriodReference periodReference;
}

