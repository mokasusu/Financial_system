package com.financial.modules.aianalysis.service;

import com.financial.modules.aianalysis.dto.internal.AIAnalysisInputPayload;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.enums.ValidationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AIAnalysisPayloadBuilder {

    private static final List<String> CORE_14_RATIOS = List.of(
            "CURRENT_RATIO",
            "QUICK_RATIO",
            "CASH_RATIO",
            "DEBT_TO_EQUITY",
            "DEBT_RATIO",
            "EQUITY_RATIO",
            "ROA",
            "ROE",
            "NET_PROFIT_MARGIN",
            "GROSS_PROFIT_MARGIN",
            "ASSET_TURNOVER",
            "INVENTORY_TURNOVER",
            "RECEIVABLES_TURNOVER",
            "PAYABLES_TURNOVER"
    );

    public AIAnalysisInputPayload buildPayload(
            Assessment assessment,
            FinancialStatement statement,
            Company company,
            FinancialCalculation calculation,
            ValidationExecution validation,
            RiskAnalysis riskAnalysis
    ) {
        log.info("Constructing structured AI input payload for assessment ID {}", assessment.getId());

        // 1. Company Info
        AIAnalysisInputPayload.CompanyInfo companyInfo = AIAnalysisInputPayload.CompanyInfo.builder()
                .companyName(company.getCompanyName())
                .industry(company.getMainBusinessLine())
                .accountingStandard(statement.getAccountingStandard())
                .period(statement.getPeriod())
                .currencyUnit(statement.getUnit() != null ? statement.getUnit() : "VND")
                .build();

        // 2. Financial Ratios Map
        Map<String, FinancialCalculationResult> calcMap = Collections.emptyMap();
        if (calculation != null && calculation.getResults() != null) {
            calcMap = calculation.getResults().stream()
                    .filter(r -> r.getRatioCode() != null)
                    .collect(Collectors.toMap(
                            r -> r.getRatioCode().toUpperCase(),
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
        }

        Map<String, AssessmentDetail> detailMap = Collections.emptyMap();
        if (assessment.getDetails() != null) {
            detailMap = assessment.getDetails().stream()
                    .filter(d -> d.getRatioCodeSnapshot() != null)
                    .collect(Collectors.toMap(
                            d -> d.getRatioCodeSnapshot().toUpperCase(),
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
        }

        List<AIAnalysisInputPayload.RatioInfo> ratioInfos = new ArrayList<>();
        for (String code : CORE_14_RATIOS) {
            FinancialCalculationResult calc = calcMap.get(code);
            AssessmentDetail detail = detailMap.get(code);

            String ratioName = calc != null && calc.getRatioName() != null ? calc.getRatioName() : code;
            if (calc == null && detail != null && detail.getRatioNameSnapshot() != null) {
                ratioName = detail.getRatioNameSnapshot();
            }

            AIAnalysisInputPayload.RatioInfo ratioInfo;
            if (calc != null && calc.getStatus() == CalculationStatus.SUCCESS && calc.getResultValue() != null) {
                ratioInfo = AIAnalysisInputPayload.RatioInfo.builder()
                        .ratioCode(code)
                        .ratioName(ratioName)
                        .actualValue(calc.getResultValue())
                        .unit(calc.getUnit() != null ? calc.getUnit().name() : null)
                        .calculationStatus("SUCCESS")
                        .evaluationStatus(detail != null && detail.getEvaluationStatus() != null ? detail.getEvaluationStatus().name() : null)
                        .missingDataExplanation(null)
                        .build();
            } else {
                ratioInfo = AIAnalysisInputPayload.RatioInfo.builder()
                        .ratioCode(code)
                        .ratioName(ratioName)
                        .actualValue(null)
                        .unit(null)
                        .calculationStatus(calc != null && calc.getStatus() != null ? calc.getStatus().name() : "INSUFFICIENT_DATA")
                        .evaluationStatus(detail != null && detail.getEvaluationStatus() != null ? detail.getEvaluationStatus().name() : null)
                        .missingDataExplanation("Chưa có đủ số liệu BCTC đã xác nhận để tính toán chỉ số này.")
                        .build();
            }
            ratioInfos.add(ratioInfo);
        }

        // 3. Assessment Info
        List<AIAnalysisInputPayload.CriterionInfo> criteriaInfos = new ArrayList<>();
        if (assessment.getDetails() != null) {
            for (AssessmentDetail d : assessment.getDetails()) {
                criteriaInfos.add(AIAnalysisInputPayload.CriterionInfo.builder()
                        .criterionName(d.getCriterionSnapshot() != null ? d.getCriterionSnapshot() : d.getRatioNameSnapshot())
                        .ratioCode(d.getRatioCodeSnapshot())
                        .comparisonOperator(d.getComparisonOperatorSnapshot() != null ? d.getComparisonOperatorSnapshot().name() : null)
                        .thresholdValue(d.getThresholdValueSnapshot())
                        .evaluationStatus(d.getEvaluationStatus() != null ? d.getEvaluationStatus().name() : null)
                        .explanation(d.getEvaluationExplanation())
                        .build());
            }
        }

        AIAnalysisInputPayload.AssessmentInfo assessmentInfo = AIAnalysisInputPayload.AssessmentInfo.builder()
                .assessmentVersion(assessment.getAssessmentVersion())
                .confirmedDataVersion(assessment.getConfirmedDataVersion())
                .assessmentStatus(assessment.getAssessmentStatus() != null ? assessment.getAssessmentStatus().name() : null)
                .scoringStatus(assessment.getScoringStatus() != null ? assessment.getScoringStatus().name() : null)
                .overallScore(assessment.getOverallScore())
                .healthClassification(assessment.getHealthClassification() != null ? assessment.getHealthClassification().name() : null)
                .dataQualityStatus(assessment.getDataQualityStatus() != null ? assessment.getDataQualityStatus().name() : null)
                .criteriaEvaluations(criteriaInfos)
                .build();

        // 4. Validation Info
        AIAnalysisInputPayload.ValidationInfo validationInfo = null;
        if (validation != null) {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            if (validation.getResults() != null) {
                for (ValidationResult vr : validation.getResults()) {
                    if (vr.getStatus() == ValidationStatus.FAIL || vr.getStatus() == ValidationStatus.ERROR) {
                        errors.add(String.format("[%s] %s: %s", vr.getRuleCodeSnapshot(), vr.getRuleNameSnapshot(), vr.getExplanation()));
                    } else if (vr.getStatus() == ValidationStatus.WARNING) {
                        warnings.add(String.format("[%s] %s: %s", vr.getRuleCodeSnapshot(), vr.getRuleNameSnapshot(), vr.getExplanation()));
                    }
                }
            }

            validationInfo = AIAnalysisInputPayload.ValidationInfo.builder()
                    .executionStatus(validation.getExecutionStatus() != null ? validation.getExecutionStatus().name() : null)
                    .accountingStandard(validation.getAccountingStandard() != null ? validation.getAccountingStandard().name() : null)
                    .totalRules(validation.getTotalRules())
                    .passedRules(validation.getPassedRules())
                    .failedRules(validation.getFailedRules())
                    .warningRules(validation.getWarningRules())
                    .errors(errors)
                    .warnings(warnings)
                    .build();
        }

        // 5. Risk Info
        List<AIAnalysisInputPayload.RiskInfo> riskInfos = new ArrayList<>();
        if (riskAnalysis != null && riskAnalysis.getIdentifiedRisks() != null) {
            for (IdentifiedRisk r : riskAnalysis.getIdentifiedRisks()) {
                riskInfos.add(AIAnalysisInputPayload.RiskInfo.builder()
                        .riskId(r.getId())
                        .category(r.getRiskCategory() != null ? r.getRiskCategory().name() : null)
                        .riskTitle(r.getRiskTitle())
                        .description(r.getRiskDescription())
                        .severity(r.getSeverity() != null ? r.getSeverity().name() : null)
                        .status(r.getRiskStatus() != null ? r.getRiskStatus().name() : null)
                        .relatedRatioCode(r.getRelatedRatioCode())
                        .actualValue(r.getActualValue())
                        .thresholdValue(r.getThresholdValue())
                        .existingRecommendation(r.getRecommendation())
                        .build());
            }
        }

        return AIAnalysisInputPayload.builder()
                .company(companyInfo)
                .financialRatios(ratioInfos)
                .assessment(assessmentInfo)
                .validation(validationInfo)
                .detectedRisks(riskInfos)
                .build();
    }
}
