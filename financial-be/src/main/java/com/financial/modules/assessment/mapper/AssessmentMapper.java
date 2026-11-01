package com.financial.modules.assessment.mapper;

import com.financial.modules.assessment.dto.*;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.enums.ValidationStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssessmentMapper {

    public AssessmentResponse toResponse(Assessment assessment) {
        if (assessment == null) {
            return null;
        }

        AssessmentValidationSummary valSummary = buildValidationSummary(
                assessment.getValidationExecution(),
                assessment.getDataQualityStatus()
        );

        List<AssessmentDetailResponse> detailResponses = assessment.getDetails() != null
                ? assessment.getDetails().stream().map(this::toDetailResponse).collect(Collectors.toList())
                : Collections.emptyList();

        List<AssessmentCategoryResponse> categoryResponses = assessment.getCategoryScores() != null
                ? assessment.getCategoryScores().stream().map(this::toCategoryResponse).collect(Collectors.toList())
                : Collections.emptyList();

        Long companyId = assessment.getCompany() != null ? assessment.getCompany().getId() : null;
        String companyName = assessment.getCompany() != null ? assessment.getCompany().getCompanyName() : null;

        Long statementId = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getId() : null;
        String fileName = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getFileName() : null;
        Integer fiscalYear = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getFiscalYear() : null;
        String period = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getPeriod() : null;
        String standard = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getAccountingStandard() : null;

        Long calcId = assessment.getFinancialCalculation() != null ? assessment.getFinancialCalculation().getId() : null;
        Integer calcVersion = assessment.getFinancialCalculation() != null ? assessment.getFinancialCalculation().getCalculationVersion() : null;
        Long valId = assessment.getValidationExecution() != null ? assessment.getValidationExecution().getId() : null;

        return AssessmentResponse.builder()
                .id(assessment.getId())
                .companyId(companyId)
                .companyName(companyName)
                .financialStatementId(statementId)
                .statementFileName(fileName)
                .fiscalYear(fiscalYear)
                .period(period)
                .accountingStandard(standard)
                .confirmedDataVersion(assessment.getConfirmedDataVersion())
                .calculationExecutionId(calcId)
                .calculationVersion(calcVersion)
                .validationExecutionId(valId)
                .assessmentVersion(assessment.getAssessmentVersion())
                .assessmentStatus(assessment.getAssessmentStatus())
                .scoringStatus(assessment.getScoringStatus())
                .overallScore(assessment.getOverallScore())
                .healthClassification(assessment.getHealthClassification())
                .healthClassificationDisplayName(assessment.getHealthClassification() != null
                        ? assessment.getHealthClassification().getDisplayName() : null)
                .dataQualityStatus(assessment.getDataQualityStatus())
                .totalRatios(assessment.getTotalRatios())
                .passedRatios(assessment.getPassedRatios())
                .failedRatios(assessment.getFailedRatios())
                .insufficientDataRatios(assessment.getInsufficientDataRatios())
                .notConfiguredRatios(assessment.getNotConfiguredRatios())
                .assessmentNotes(assessment.getAssessmentNotes())
                .assessedAt(assessment.getAssessedAt())
                .validationSummary(valSummary)
                .details(detailResponses)
                .categoryScores(categoryResponses)
                .build();
    }

    public AssessmentSummaryResponse toSummaryResponse(Assessment assessment) {
        if (assessment == null) {
            return null;
        }

        AssessmentValidationSummary valSummary = buildValidationSummary(
                assessment.getValidationExecution(),
                assessment.getDataQualityStatus()
        );

        Long statementId = assessment.getFinancialStatement() != null ? assessment.getFinancialStatement().getId() : null;
        Long companyId = assessment.getCompany() != null ? assessment.getCompany().getId() : null;

        return AssessmentSummaryResponse.builder()
                .assessmentId(assessment.getId())
                .financialStatementId(statementId)
                .companyId(companyId)
                .assessmentVersion(assessment.getAssessmentVersion())
                .assessmentStatus(assessment.getAssessmentStatus())
                .dataQualityStatus(assessment.getDataQualityStatus())
                .scoringStatus(assessment.getScoringStatus())
                .overallScore(assessment.getOverallScore())
                .healthClassification(assessment.getHealthClassification())
                .healthClassificationDisplayName(assessment.getHealthClassification() != null
                        ? assessment.getHealthClassification().getDisplayName() : null)
                .totalEvaluatedRatios(assessment.getTotalRatios())
                .totalPass(assessment.getPassedRatios())
                .totalFail(assessment.getFailedRatios())
                .totalInsufficientData(assessment.getInsufficientDataRatios())
                .totalNotConfigured(assessment.getNotConfiguredRatios())
                .validationSummary(valSummary)
                .assessedAt(assessment.getAssessedAt())
                .build();
    }

    public AssessmentHistoryResponse toHistoryResponse(Assessment assessment) {
        if (assessment == null) {
            return null;
        }

        Long calcId = assessment.getFinancialCalculation() != null ? assessment.getFinancialCalculation().getId() : null;
        Long valId = assessment.getValidationExecution() != null ? assessment.getValidationExecution().getId() : null;

        return AssessmentHistoryResponse.builder()
                .id(assessment.getId())
                .assessmentVersion(assessment.getAssessmentVersion())
                .calculationExecutionId(calcId)
                .validationExecutionId(valId)
                .assessmentStatus(assessment.getAssessmentStatus())
                .scoringStatus(assessment.getScoringStatus())
                .overallScore(assessment.getOverallScore())
                .healthClassification(assessment.getHealthClassification())
                .dataQualityStatus(assessment.getDataQualityStatus())
                .totalRatios(assessment.getTotalRatios())
                .passedRatios(assessment.getPassedRatios())
                .failedRatios(assessment.getFailedRatios())
                .insufficientDataRatios(assessment.getInsufficientDataRatios())
                .notConfiguredRatios(assessment.getNotConfiguredRatios())
                .assessedAt(assessment.getAssessedAt())
                .build();
    }

    public AssessmentDetailResponse toDetailResponse(AssessmentDetail detail) {
        if (detail == null) {
            return null;
        }

        Long criterionId = detail.getCriterion() != null ? detail.getCriterion().getId() : null;
        String criterionName = detail.getCriterion() != null ? detail.getCriterion().getCriterionName() : detail.getCriterionSnapshot();

        return AssessmentDetailResponse.builder()
                .id(detail.getId())
                .ratioCode(detail.getRatioCodeSnapshot())
                .ratioName(detail.getRatioNameSnapshot())
                .category(detail.getCategory())
                .calculatedValue(detail.getCalculatedValue())
                .unit(detail.getUnit())
                .unitDisplayName(getUnitDisplayName(detail.getUnit()))
                .criterionId(criterionId)
                .criterionName(criterionName)
                .comparisonOperator(detail.getComparisonOperatorSnapshot())
                .comparisonOperatorSymbol(getOperatorSymbol(detail.getComparisonOperatorSnapshot()))
                .thresholdValue(detail.getThresholdValueSnapshot())
                .minimumValue(detail.getMinimumValueSnapshot())
                .maximumValue(detail.getMaximumValueSnapshot())
                .evaluationStatus(detail.getEvaluationStatus())
                .evaluationExplanation(detail.getEvaluationExplanation())
                .build();
    }

    public AssessmentCategoryResponse toCategoryResponse(AssessmentCategoryScore categoryScore) {
        if (categoryScore == null) {
            return null;
        }

        return AssessmentCategoryResponse.builder()
                .id(categoryScore.getId())
                .category(categoryScore.getCategory())
                .categoryName(categoryScore.getCategoryName())
                .totalRatios(categoryScore.getTotalRatios())
                .passedRatios(categoryScore.getPassedRatios())
                .failedRatios(categoryScore.getFailedRatios())
                .insufficientDataRatios(categoryScore.getInsufficientDataRatios())
                .notConfiguredRatios(categoryScore.getNotConfiguredRatios())
                .categoryScore(categoryScore.getCategoryScore())
                .scoringStatus(categoryScore.getScoringStatus())
                .build();
    }

    public AssessmentValidationSummary buildValidationSummary(ValidationExecution val, DataQualityStatus qualityStatus) {
        if (val == null) {
            return AssessmentValidationSummary.builder()
                    .dataQualityStatus(qualityStatus != null ? qualityStatus : DataQualityStatus.NOT_VALIDATED)
                    .executionStatus("NOT_VALIDATED")
                    .totalRules(0)
                    .passedRules(0)
                    .warningRules(0)
                    .failedRules(0)
                    .reviewRequiredRules(0)
                    .errorRules(0)
                    .hasMismatchedVersion(false)
                    .validationNotes("Báo cáo tài chính chưa thực hiện kiểm tra tính hợp lệ dữ liệu.")
                    .build();
        }

        boolean hasMismatch = qualityStatus == DataQualityStatus.REVIEW_REQUIRED
                && val.getExecutionStatus() == ValidationStatus.PASS;

        return AssessmentValidationSummary.builder()
                .validationExecutionId(val.getId())
                .executionStatus(val.getExecutionStatus() != null ? val.getExecutionStatus().name() : null)
                .dataQualityStatus(qualityStatus)
                .totalRules(val.getTotalRules())
                .passedRules(val.getPassedRules())
                .warningRules(val.getWarningRules())
                .failedRules(val.getFailedRules())
                .reviewRequiredRules(val.getReviewRequiredRules())
                .errorRules(val.getErrorRules())
                .hasMismatchedVersion(hasMismatch)
                .validationNotes(buildValidationNotes(val, qualityStatus))
                .build();
    }

    private String buildValidationNotes(ValidationExecution val, DataQualityStatus qualityStatus) {
        if (qualityStatus == DataQualityStatus.INVALID) {
            return String.format("Dữ liệu vi phạm %d quy tắc nghiêm trọng. Cần kiểm tra lại trước khi phân tích.",
                    val.getFailedRules() != null ? val.getFailedRules() : 0);
        }
        if (qualityStatus == DataQualityStatus.HAS_WARNINGS) {
            return String.format("Dữ liệu đạt chuẩn với %d cảnh báo.",
                    val.getWarningRules() != null ? val.getWarningRules() : 0);
        }
        if (qualityStatus == DataQualityStatus.REVIEW_REQUIRED) {
            return "Dữ liệu hoặc phiên bản xác nhận cần được rà soát đối chiếu.";
        }
        return "Dữ liệu kế toán đã qua kiểm tra và hoàn toàn cân đối.";
    }

    public String getOperatorSymbol(ComparisonOperator op) {
        if (op == null) return null;
        return switch (op) {
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> ">=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case EQUAL -> "==";
            case BETWEEN -> "giữa";
        };
    }

    public String getCategoryDisplayName(FinancialRatioCategory cat) {
        if (cat == null) return null;
        return switch (cat) {
            case LIQUIDITY -> "Khả năng thanh toán";
            case SOLVENCY -> "Cơ cấu vốn và khả năng thanh nợ";
            case PROFITABILITY -> "Khả năng sinh lời";
            case EFFICIENCY -> "Hiệu quả hoạt động";
        };
    }

    public String getUnitDisplayName(FinancialUnit unit) {
        if (unit == null) return null;
        return switch (unit) {
            case TIMES -> "Lần";
            case PERCENT -> "%";
            case VND -> "VND";
        };
    }
}
