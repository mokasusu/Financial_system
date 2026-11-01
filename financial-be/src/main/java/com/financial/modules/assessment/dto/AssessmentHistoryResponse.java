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
@Schema(description = "Assessment history item in paginated list")
public class AssessmentHistoryResponse {

    @Schema(description = "Assessment ID", example = "1")
    private Long id;

    @Schema(description = "Sequential assessment version", example = "1")
    private Integer assessmentVersion;

    @Schema(description = "Associated calculation execution ID", example = "1")
    private Long calculationExecutionId;

    @Schema(description = "Associated validation execution ID", example = "1")
    private Long validationExecutionId;

    @Schema(description = "Assessment status", example = "COMPLETED")
    private AssessmentStatus assessmentStatus;

    @Schema(description = "Scoring configuration status", example = "NOT_CONFIGURED")
    private AssessmentScoringStatus scoringStatus;

    @Schema(description = "Overall composite score", example = "null")
    private BigDecimal overallScore;

    @Schema(description = "Financial health classification", example = "NOT_CONFIGURED")
    private HealthClassification healthClassification;

    @Schema(description = "Data quality status", example = "VALID")
    private DataQualityStatus dataQualityStatus;

    @Schema(description = "Total financial ratios evaluated", example = "14")
    private Integer totalRatios;

    @Schema(description = "Total passing ratios", example = "4")
    private Integer passedRatios;

    @Schema(description = "Total failing ratios", example = "0")
    private Integer failedRatios;

    @Schema(description = "Total insufficient data ratios", example = "0")
    private Integer insufficientDataRatios;

    @Schema(description = "Total not configured ratios", example = "10")
    private Integer notConfiguredRatios;

    @Schema(description = "Timestamp when assessment was performed")
    private LocalDateTime assessedAt;
}

