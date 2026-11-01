package com.financial.modules.calculationengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
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
@Schema(description = "Calculated financial ratio result detail")
public class CalculationResultResponse {

    @Schema(description = "Result ID", example = "1")
    private Long id;

    @Schema(description = "Ratio code", example = "CURRENT_RATIO")
    private String ratioCode;

    @Schema(description = "Ratio name", example = "Chỉ số thanh toán hiện hành")
    private String ratioName;

    @Schema(description = "Ratio category", example = "LIQUIDITY")
    private FinancialRatioCategory category;

    @Schema(description = "Calculated numeric value (null if calculation failed or insufficient data)", example = "1.8500")
    private BigDecimal resultValue;

    @Schema(description = "Measurement unit", example = "TIMES")
    private FinancialUnit unit;

    @Schema(description = "Unit display name", example = "Lần")
    private String unitDisplayName;

    @Schema(description = "Whether this ratio is a core KPI", example = "true")
    private Boolean isCoreKpi;

    @Schema(description = "Calculation execution status", example = "SUCCESS")
    private CalculationStatus status;

    @Schema(description = "Status display name", example = "Thành công")
    private String statusDisplayName;

    @Schema(description = "Formula expression snapshot used for calculation", example = "CURRENT_ASSETS / CURRENT_LIABILITIES")
    private String formulaSnapshot;

    @Schema(description = "JSON snapshot of input values used during calculation")
    private String inputSnapshot;

    @Schema(description = "Explanatory calculation note or assumption", example = "Using Net Revenue as a proxy for net credit sales")
    private String calculationNote;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}

