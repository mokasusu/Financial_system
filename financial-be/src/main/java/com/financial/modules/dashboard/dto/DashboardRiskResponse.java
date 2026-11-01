package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Financial risk analysis overview and list of detected risks")
public class DashboardRiskResponse {

    @Schema(description = "Risk analysis ID", example = "301")
    private Long riskAnalysisId;

    @Schema(description = "Risk execution status", example = "COMPLETED")
    private String executionStatus;

    @Schema(description = "Risk severity breakdown summary")
    private DashboardRiskSummaryDTO summary;

    @Schema(description = "List of identified risks")
    private List<DashboardIdentifiedRiskDTO> identifiedRisks;
}

