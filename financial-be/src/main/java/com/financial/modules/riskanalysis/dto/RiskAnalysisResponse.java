package com.financial.modules.riskanalysis.dto;

import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete financial risk analysis response")
public class RiskAnalysisResponse {

    @Schema(description = "Risk analysis ID", example = "1")
    private Long id;

    @Schema(description = "Company ID", example = "10")
    private Long companyId;

    @Schema(description = "Company name", example = "Công ty Cổ phần Công nghệ Tài chính Việt Nam")
    private String companyName;

    @Schema(description = "Financial statement ID", example = "100")
    private Long financialStatementId;

    @Schema(description = "Financial statement file name", example = "BCTC_2024.xlsx")
    private String statementFileName;

    @Schema(description = "Fiscal year", example = "2024")
    private Integer fiscalYear;

    @Schema(description = "Reporting period", example = "YEAR")
    private String period;

    @Schema(description = "Accounting standard applied", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Confirmed data version reference", example = "CONFIRMED_20261101_100")
    private String confirmedDataVersion;

    @Schema(description = "Associated assessment ID", example = "501")
    private Long assessmentId;

    @Schema(description = "Associated calculation execution ID", example = "200")
    private Long calculationExecutionId;

    @Schema(description = "Associated validation execution ID", example = "300")
    private Long validationExecutionId;

    @Schema(description = "Analysis sequential version", example = "1")
    private Integer analysisVersion;

    @Schema(description = "Execution status", example = "COMPLETED")
    private RiskAnalysisStatus executionStatus;

    @Schema(description = "Total active identified risks (DETECTED + MONITORING)", example = "3")
    private Integer totalRisks;

    @Schema(description = "Count of LOW severity risks", example = "1")
    private Integer lowRisks;

    @Schema(description = "Count of MEDIUM severity risks", example = "1")
    private Integer mediumRisks;

    @Schema(description = "Count of HIGH severity risks", example = "1")
    private Integer highRisks;

    @Schema(description = "Count of CRITICAL severity risks", example = "0")
    private Integer criticalRisks;

    @Schema(description = "Count of resolved risks from previous evaluations", example = "0")
    private Integer resolvedRisks;

    @Schema(description = "Count of indicators with no configured risk rule", example = "10")
    private Integer notConfiguredCount;

    @Schema(description = "Count of indicators with insufficient calculation data", example = "0")
    private Integer insufficientDataCount;

    @Schema(description = "Optional audit notes", example = "Phân tích rủi ro định kỳ hoàn tất")
    private String analysisNotes;

    @Schema(description = "Timestamp when analysis was performed")
    private LocalDateTime analyzedAt;

    @Schema(description = "List of all identified risks")
    @Builder.Default
    private List<IdentifiedRiskResponse> identifiedRisks = new ArrayList<>();
}

