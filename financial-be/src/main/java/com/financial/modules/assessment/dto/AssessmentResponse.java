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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full financial health assessment response")
public class AssessmentResponse {

    @Schema(description = "Assessment ID", example = "1")
    private Long id;

    @Schema(description = "Company ID", example = "10")
    private Long companyId;

    @Schema(description = "Company business name", example = "Công ty Cổ phần Công nghệ Tài chính Việt Nam")
    private String companyName;

    @Schema(description = "Financial statement ID", example = "100")
    private Long financialStatementId;

    @Schema(description = "Financial statement file name", example = "BCTC_2024_KiemToan.xlsx")
    private String statementFileName;

    @Schema(description = "Fiscal year of financial statement", example = "2024")
    private Integer fiscalYear;

    @Schema(description = "Reporting period", example = "YEAR")
    private String period;

    @Schema(description = "Accounting standard applied (TT200, TT133, TT99)", example = "TT200")
    private String accountingStandard;

    @Schema(description = "Confirmed financial data version reference", example = "CONFIRMED_20261101_100")
    private String confirmedDataVersion;

    @Schema(description = "Associated calculation execution ID", example = "1")
    private Long calculationExecutionId;

    @Schema(description = "Associated calculation version", example = "1")
    private Integer calculationVersion;

    @Schema(description = "Associated validation execution ID (if executed)", example = "1")
    private Long validationExecutionId;

    @Schema(description = "Sequential assessment version for this statement", example = "1")
    private Integer assessmentVersion;

    @Schema(description = "Assessment execution status", example = "COMPLETED")
    private AssessmentStatus assessmentStatus;

    @Schema(description = "Scoring configuration status", example = "NOT_CONFIGURED")
    private AssessmentScoringStatus scoringStatus;

    @Schema(description = "Overall composite score (null if scoring not configured)", example = "null")
    private BigDecimal overallScore;

    @Schema(description = "Financial health classification level", example = "NOT_CONFIGURED")
    private HealthClassification healthClassification;

    @Schema(description = "Display name of health classification", example = "Chưa cấu hình")
    private String healthClassificationDisplayName;

    @Schema(description = "Data quality status from validation engine", example = "VALID")
    private DataQualityStatus dataQualityStatus;

    @Schema(description = "Total evaluated financial ratios across all categories", example = "14")
    private Integer totalRatios;

    @Schema(description = "Total ratios passing threshold", example = "4")
    private Integer passedRatios;

    @Schema(description = "Total ratios failing threshold", example = "0")
    private Integer failedRatios;

    @Schema(description = "Total ratios with missing or insufficient data", example = "0")
    private Integer insufficientDataRatios;

    @Schema(description = "Total ratios without active configured criteria", example = "10")
    private Integer notConfiguredRatios;

    @Schema(description = "Optional assessment notes", example = "Đánh giá tài chính tự động hoàn tất")
    private String assessmentNotes;

    @Schema(description = "Timestamp when assessment was performed")
    private LocalDateTime assessedAt;

    @Schema(description = "Validation execution summary")
    private AssessmentValidationSummary validationSummary;

    @Schema(description = "List of category-level aggregated summaries")
    @Builder.Default
    private List<AssessmentCategoryResponse> categoryScores = new ArrayList<>();

    @Schema(description = "List of individual financial ratio evaluation details")
    @Builder.Default
    private List<AssessmentDetailResponse> details = new ArrayList<>();
}

