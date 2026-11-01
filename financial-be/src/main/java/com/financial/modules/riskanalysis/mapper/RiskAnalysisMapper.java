package com.financial.modules.riskanalysis.mapper;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.dto.*;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RiskAnalysisMapper {

    public RiskRule toRuleEntity(RiskRuleRequest request) {
        if (request == null) return null;
        return RiskRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .riskCategory(request.getRiskCategory())
                .ratioCode(request.getRatioCode())
                .comparisonOperator(request.getComparisonOperator())
                .thresholdValue(request.getThresholdValue())
                .minimumValue(request.getMinimumValue())
                .maximumValue(request.getMaximumValue())
                .severity(request.getSeverity())
                .explanationTemplate(request.getExplanationTemplate())
                .recommendationTemplate(request.getRecommendationTemplate())
                .potentialImpactTemplate(request.getPotentialImpactTemplate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public void updateRuleEntity(RiskRule rule, RiskRuleRequest request) {
        if (rule == null || request == null) return;
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setRiskCategory(request.getRiskCategory());
        rule.setRatioCode(request.getRatioCode());
        rule.setComparisonOperator(request.getComparisonOperator());
        rule.setThresholdValue(request.getThresholdValue());
        rule.setMinimumValue(request.getMinimumValue());
        rule.setMaximumValue(request.getMaximumValue());
        rule.setSeverity(request.getSeverity());
        rule.setExplanationTemplate(request.getExplanationTemplate());
        rule.setRecommendationTemplate(request.getRecommendationTemplate());
        rule.setPotentialImpactTemplate(request.getPotentialImpactTemplate());
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
    }

    public RiskRuleResponse toRuleResponse(RiskRule rule) {
        if (rule == null) return null;
        return RiskRuleResponse.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .riskCategory(rule.getRiskCategory())
                .riskCategoryDisplayName(rule.getRiskCategory() != null ? rule.getRiskCategory().getDisplayName() : null)
                .ratioCode(rule.getRatioCode())
                .comparisonOperator(rule.getComparisonOperator())
                .comparisonOperatorSymbol(getOperatorSymbol(rule.getComparisonOperator()))
                .thresholdValue(rule.getThresholdValue())
                .minimumValue(rule.getMinimumValue())
                .maximumValue(rule.getMaximumValue())
                .severity(rule.getSeverity())
                .severityDisplayName(rule.getSeverity() != null ? rule.getSeverity().getDisplayName() : null)
                .explanationTemplate(rule.getExplanationTemplate())
                .recommendationTemplate(rule.getRecommendationTemplate())
                .potentialImpactTemplate(rule.getPotentialImpactTemplate())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    public IdentifiedRiskResponse toRiskResponse(IdentifiedRisk risk) {
        if (risk == null) return null;
        return IdentifiedRiskResponse.builder()
                .id(risk.getId())
                .ruleCode(risk.getRuleCodeSnapshot())
                .riskTitle(risk.getRiskTitle())
                .riskDescription(risk.getRiskDescription())
                .riskCategory(risk.getRiskCategory())
                .riskCategoryDisplayName(risk.getRiskCategory() != null ? risk.getRiskCategory().getDisplayName() : null)
                .severity(risk.getSeverity())
                .severityDisplayName(risk.getSeverity() != null ? risk.getSeverity().getDisplayName() : null)
                .riskStatus(risk.getRiskStatus())
                .riskStatusDisplayName(risk.getRiskStatus() != null ? risk.getRiskStatus().getDisplayName() : null)
                .relatedRatioCode(risk.getRelatedRatioCode())
                .actualValue(risk.getActualValue())
                .thresholdValue(risk.getThresholdValue())
                .potentialImpact(risk.getPotentialImpact())
                .recommendation(risk.getRecommendation())
                .sourceReference(risk.getSourceReference())
                .detectedAt(risk.getDetectedAt())
                .build();
    }

    public RiskAnalysisResponse toAnalysisResponse(RiskAnalysis analysis) {
        if (analysis == null) return null;

        Long companyId = analysis.getCompany() != null ? analysis.getCompany().getId() : null;
        String companyName = analysis.getCompany() != null ? analysis.getCompany().getCompanyName() : null;

        Long statementId = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getId() : null;
        String fileName = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getFileName() : null;
        Integer fiscalYear = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getFiscalYear() : null;
        String period = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getPeriod() : null;
        String standard = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getAccountingStandard() : null;

        Long assessmentId = analysis.getAssessment() != null ? analysis.getAssessment().getId() : null;
        Long calcId = analysis.getFinancialCalculation() != null ? analysis.getFinancialCalculation().getId() : null;
        Long valId = analysis.getValidationExecution() != null ? analysis.getValidationExecution().getId() : null;

        List<IdentifiedRiskResponse> riskResponses = analysis.getIdentifiedRisks() != null
                ? analysis.getIdentifiedRisks().stream().map(this::toRiskResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return RiskAnalysisResponse.builder()
                .id(analysis.getId())
                .companyId(companyId)
                .companyName(companyName)
                .financialStatementId(statementId)
                .statementFileName(fileName)
                .fiscalYear(fiscalYear)
                .period(period)
                .accountingStandard(standard)
                .confirmedDataVersion(analysis.getConfirmedDataVersion())
                .assessmentId(assessmentId)
                .calculationExecutionId(calcId)
                .validationExecutionId(valId)
                .analysisVersion(analysis.getAnalysisVersion())
                .executionStatus(analysis.getExecutionStatus())
                .totalRisks(analysis.getTotalRisks())
                .lowRisks(analysis.getLowRisks())
                .mediumRisks(analysis.getMediumRisks())
                .highRisks(analysis.getHighRisks())
                .criticalRisks(analysis.getCriticalRisks())
                .resolvedRisks(analysis.getResolvedRisks())
                .notConfiguredCount(analysis.getNotConfiguredCount())
                .insufficientDataCount(analysis.getInsufficientDataCount())
                .analysisNotes(analysis.getAnalysisNotes())
                .analyzedAt(analysis.getAnalyzedAt())
                .identifiedRisks(riskResponses)
                .build();
    }

    public RiskAnalysisHistoryResponse toHistoryResponse(RiskAnalysis analysis) {
        if (analysis == null) return null;

        Long assessmentId = analysis.getAssessment() != null ? analysis.getAssessment().getId() : null;
        Long calcId = analysis.getFinancialCalculation() != null ? analysis.getFinancialCalculation().getId() : null;
        Long valId = analysis.getValidationExecution() != null ? analysis.getValidationExecution().getId() : null;

        return RiskAnalysisHistoryResponse.builder()
                .id(analysis.getId())
                .analysisVersion(analysis.getAnalysisVersion())
                .assessmentId(assessmentId)
                .calculationExecutionId(calcId)
                .validationExecutionId(valId)
                .executionStatus(analysis.getExecutionStatus())
                .totalRisks(analysis.getTotalRisks())
                .lowRisks(analysis.getLowRisks())
                .mediumRisks(analysis.getMediumRisks())
                .highRisks(analysis.getHighRisks())
                .criticalRisks(analysis.getCriticalRisks())
                .resolvedRisks(analysis.getResolvedRisks())
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }

    public RiskSummaryResponse toSummaryResponse(RiskAnalysis analysis) {
        if (analysis == null) return null;

        Long statementId = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getId() : null;
        Long companyId = analysis.getCompany() != null ? analysis.getCompany().getId() : null;

        Map<RiskCategory, Integer> categoryCounts = new EnumMap<>(RiskCategory.class);
        for (RiskCategory cat : RiskCategory.values()) {
            categoryCounts.put(cat, 0);
        }

        Map<RiskSeverity, Integer> severityCounts = new EnumMap<>(RiskSeverity.class);
        for (RiskSeverity sev : RiskSeverity.values()) {
            severityCounts.put(sev, 0);
        }

        if (analysis.getIdentifiedRisks() != null) {
            for (IdentifiedRisk r : analysis.getIdentifiedRisks()) {
                if (r.getRiskStatus() == RiskStatus.DETECTED || r.getRiskStatus() == RiskStatus.MONITORING) {
                    if (r.getRiskCategory() != null) {
                        categoryCounts.put(r.getRiskCategory(), categoryCounts.get(r.getRiskCategory()) + 1);
                    }
                    if (r.getSeverity() != null) {
                        severityCounts.put(r.getSeverity(), severityCounts.get(r.getSeverity()) + 1);
                    }
                }
            }
        }

        return RiskSummaryResponse.builder()
                .riskAnalysisId(analysis.getId())
                .financialStatementId(statementId)
                .companyId(companyId)
                .analysisVersion(analysis.getAnalysisVersion())
                .executionStatus(analysis.getExecutionStatus())
                .totalRisks(analysis.getTotalRisks())
                .lowRisks(analysis.getLowRisks())
                .mediumRisks(analysis.getMediumRisks())
                .highRisks(analysis.getHighRisks())
                .criticalRisks(analysis.getCriticalRisks())
                .resolvedRisks(analysis.getResolvedRisks())
                .notConfiguredCount(analysis.getNotConfiguredCount())
                .insufficientDataCount(analysis.getInsufficientDataCount())
                .categoryCounts(categoryCounts)
                .severityCounts(severityCounts)
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
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
}

