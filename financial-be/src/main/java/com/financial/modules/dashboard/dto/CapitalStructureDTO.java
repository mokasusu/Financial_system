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
@Schema(description = "Capital structure distribution indicators")
public class CapitalStructureDTO {

    @Schema(description = "Total Assets (Tổng tài sản)", example = "10000000000")
    private BigDecimal totalAssets;

    @Schema(description = "Total Liabilities (Nợ phải trả)", example = "4000000000")
    private BigDecimal totalLiabilities;

    @Schema(description = "Total Equity (Vốn chủ sở hữu)", example = "6000000000")
    private BigDecimal equity;

    @Schema(description = "Currency unit", example = "VND")
    private String unit;

    @Schema(description = "Debt ratio (Hệ số nợ) if calculated", example = "0.4000")
    private BigDecimal debtRatio;

    @Schema(description = "Equity ratio (Hệ số vốn chủ sở hữu) if calculated", example = "0.6000")
    private BigDecimal equityRatio;
}

