package com.financial.modules.assessment.dto;

import com.financial.modules.assessment.enums.DataQualityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Validation engine execution summary associated with the financial assessment")
public class AssessmentValidationSummary {

    @Schema(description = "Validation execution ID reference", example = "1")
    private Long validationExecutionId;

    @Schema(description = "Validation execution status (PASS, WARNING, FAIL, REVIEW_REQUIRED, NOT_APPLICABLE)", example = "PASS")
    private String executionStatus;

    @Schema(description = "Overall data quality status", example = "VALID")
    private DataQualityStatus dataQualityStatus;

    @Schema(description = "Total validation rules evaluated", example = "10")
    private Integer totalRules;

    @Schema(description = "Number of passed validation rules", example = "9")
    private Integer passedRules;

    @Schema(description = "Number of warning validation rules", example = "1")
    private Integer warningRules;

    @Schema(description = "Number of failed validation rules", example = "0")
    private Integer failedRules;

    @Schema(description = "Number of rules requiring review", example = "0")
    private Integer reviewRequiredRules;

    @Schema(description = "Number of rules encountering errors", example = "0")
    private Integer errorRules;

    @Schema(description = "Whether a version mismatch was detected between calculation and validation data", example = "false")
    private Boolean hasMismatchedVersion;

    @Schema(description = "Validation quality notes", example = "Tất cả các phương trình và tổng phụ cân đối đạt chuẩn.")
    private String validationNotes;
}

