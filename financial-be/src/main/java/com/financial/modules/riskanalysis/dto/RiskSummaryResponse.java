package com.financial.modules.riskanalysis.dto;

import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risk summary aggregated by category and severity for dashboards and reporting")
public class RiskSummaryResponse {

    @Schema(description = "Risk analysis ID", example = "1")
    private Long riskAnalysisId;

    @Schema(description = "Financial statement ID", example = "100")
    private Long financialStatementId;

    @Schema(description = "Company ID", example = "10")
    private Long companyId;

    @Schema(description = "Analysis sequential version", example = "1")
    private Integer analysisVersion;

    @Schema(description = "Execution status", example = "COMPLETED")
    private RiskAnalysisStatus executionStatus;

    @Schema(description = "Total active risks identified (DETECTED + MONITORING)", example = "3")
    private Integer totalRisks;

    @Schema(description = "Count of LOW severity risks", example = "1")
    private Integer lowRisks;

    @Schema(description = "Count of MEDIUM severity risks", example = "1")
    private Integer mediumRisks;

    @Schema(description = "Count of HIGH severity risks", example = "1")
    private Integer highRisks;

    @Schema(description = "Count of CRITICAL severity risks", example = "0")
    private Integer criticalRisks;

    @Schema(description = "Count of resolved risks", example = "0")
    private Integer resolvedRisks;

    @Schema(description = "Count of indicators with no configured risk rule", example = "10")
    private Integer notConfiguredCount;

    @Schema(description = "Count of indicators with insufficient data", example = "0")
    private Integer insufficientDataCount;

    @Schema(description = "Breakdown of risk counts by category")
    private Map<RiskCategory, Integer> categoryCounts;

    @Schema(description = "Breakdown of risk counts by severity")
    private Map<RiskSeverity, Integer> severityCounts;

    @Schema(description = "Timestamp when analysis was performed")
    private LocalDateTime analyzedAt;
}

