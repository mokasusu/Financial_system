package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risk severity distribution summary")
public class DashboardRiskSummaryDTO {

    @Schema(description = "Total detected risks", example = "3")
    private Integer totalRisks;

    @Schema(description = "Low severity risks", example = "1")
    private Integer lowRisks;

    @Schema(description = "Medium severity risks", example = "1")
    private Integer mediumRisks;

    @Schema(description = "High severity risks", example = "1")
    private Integer highRisks;

    @Schema(description = "Critical severity risks", example = "0")
    private Integer criticalRisks;

    @Schema(description = "Resolved risks", example = "0")
    private Integer resolvedRisks;

    @Schema(description = "Not configured risks", example = "0")
    private Integer notConfiguredCount;

    @Schema(description = "Insufficient data count", example = "0")
    private Integer insufficientDataCount;
}

