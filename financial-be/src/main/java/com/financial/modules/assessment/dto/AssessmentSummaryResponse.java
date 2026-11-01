package com.financial.modules.assessment.dto;

import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.HealthClassification;
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
@Schema(description = "Overall financial assessment summary response")
public class AssessmentSummaryResponse {

    @Schema(description = "Assessment ID", example = "1")
    private Long assessmentId;

    @Schema(description = "Financial statement ID", example = "100")
    private Long financialStatementId;

    @Schema(description = "Company ID", example = "10")
    private Long companyId;

    @Schema(description = "Assessment sequential version", example = "1")
    private Integer assessmentVersion;

    @Schema(description = "Assessment execution status", example = "COMPLETED")
    private AssessmentStatus assessmentStatus;

    @Schema(description = "Underlying data quality status from validation engine", example = "VALID")
    private DataQualityStatus dataQualityStatus;

    @Schema(description = "Scoring configuration status", example = "NOT_CONFIGURED")
    private AssessmentScoringStatus scoringStatus;

    @Schema(description = "Overall composite financial score (null if scoring not configured)", example = "null")
    private BigDecimal overallScore;

    @Schema(description = "Financial health classification level", example = "NOT_CONFIGURED")
    private HealthClassification healthClassification;

    @Schema(description = "Display name of health classification", example = "Chưa cấu hình")
    private String healthClassificationDisplayName;

    @Schema(description = "Total evaluated financial ratios across all categories", example = "14")
    private Integer totalEvaluatedRatios;

    @Schema(description = "Total ratios passing threshold", example = "4")
    private Integer totalPass;

    @Schema(description = "Total ratios failing threshold", example = "0")
    private Integer totalFail;

    @Schema(description = "Total ratios with insufficient calculation data", example = "0")
    private Integer totalInsufficientData;

    @Schema(description = "Total ratios without active evaluation criteria configured", example = "10")
    private Integer totalNotConfigured;

    @Schema(description = "Summary of validation execution results")
    private AssessmentValidationSummary validationSummary;

    @Schema(description = "Timestamp when assessment was performed")
    private LocalDateTime assessedAt;
}

