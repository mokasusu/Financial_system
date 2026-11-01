package com.financial.modules.riskanalysis.dto;

import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risk analysis history item in paginated list")
public class RiskAnalysisHistoryResponse {

    @Schema(description = "Risk analysis ID", example = "1")
    private Long id;

    @Schema(description = "Analysis sequential version", example = "1")
    private Integer analysisVersion;

    @Schema(description = "Associated assessment ID", example = "501")
    private Long assessmentId;

    @Schema(description = "Associated calculation execution ID", example = "200")
    private Long calculationExecutionId;

    @Schema(description = "Associated validation execution ID", example = "300")
    private Long validationExecutionId;

    @Schema(description = "Execution status", example = "COMPLETED")
    private RiskAnalysisStatus executionStatus;

    @Schema(description = "Total active risks", example = "3")
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

    @Schema(description = "Timestamp when analysis was performed")
    private LocalDateTime analyzedAt;
}

