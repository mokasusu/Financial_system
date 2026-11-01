package com.financial.modules.riskanalysis.detector;

import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRatioRiskDetector implements RiskDetector {

    protected final RiskAnalysisMapper mapper;

    public abstract List<String> getTargetRatioCodes();

    @Override
    public List<IdentifiedRisk> detect(RiskDetectionContext context, List<RiskRule> rules) {
        List<IdentifiedRisk> results = new ArrayList<>();
        List<String> targetCodes = getTargetRatioCodes();
        RiskCategory category = getCategory();

        Map<String, List<RiskRule>> rulesByRatio = new HashMap<>();
        if (rules != null) {
            for (RiskRule rule : rules) {
                if (rule.getRiskCategory() == category && rule.getRatioCode() != null && rule.getIsActive()) {
                    rulesByRatio.computeIfAbsent(rule.getRatioCode().toUpperCase(), k -> new ArrayList<>()).add(rule);
                }
            }
        }

        Set<String> previouslyActiveRuleCodes = new HashSet<>();
        if (context.getPreviousRisks() != null) {
            for (IdentifiedRisk prev : context.getPreviousRisks()) {
                if ((prev.getRiskStatus() == RiskStatus.DETECTED || prev.getRiskStatus() == RiskStatus.MONITORING)
                        && prev.getRuleCodeSnapshot() != null) {
                    previouslyActiveRuleCodes.add(prev.getRuleCodeSnapshot());
                }
            }
        }

        for (String ratioCode : targetCodes) {
            List<RiskRule> ratioRules = rulesByRatio.get(ratioCode.toUpperCase());

            if (ratioRules == null || ratioRules.isEmpty()) {
                // Rule: Return NOT_CONFIGURED when no applicable risk rule exists
                IdentifiedRisk notConfiguredRisk = IdentifiedRisk.builder()
                        .riskTitle("Chưa cấu hình quy tắc rủi ro: " + ratioCode)
                        .riskDescription(String.format("Chưa có quy tắc rủi ro tài chính nào được cấu hình cho chỉ số %s.", ratioCode))
                        .riskCategory(category)
                        .severity(null)
                        .riskStatus(RiskStatus.NOT_CONFIGURED)
                        .relatedRatioCode(ratioCode)
                        .sourceReference("NO_CONFIGURED_RULE")
                        .detectedAt(LocalDateTime.now())
                        .build();
                results.add(notConfiguredRisk);
                continue;
            }

            FinancialCalculationResult calcResult = context.getCalculationResultsByCode().get(ratioCode);

            for (RiskRule rule : ratioRules) {
                if (calcResult == null || calcResult.getResultValue() == null || calcResult.getStatus() != CalculationStatus.SUCCESS) {
                    // Rule: Return INSUFFICIENT_DATA when calculation value is unavailable
                    IdentifiedRisk insufficientRisk = IdentifiedRisk.builder()
                        .riskRule(rule)
                        .ruleCodeSnapshot(rule.getRuleCode())
                        .riskTitle(rule.getRuleName())
                        .riskDescription(String.format("Dữ liệu tính toán cho chỉ số %s không đầy đủ hoặc không khả dụng.", ratioCode))
                        .riskCategory(category)
                        .severity(rule.getSeverity())
                        .riskStatus(RiskStatus.INSUFFICIENT_DATA)
                        .relatedRatioCode(ratioCode)
                        .actualValue(null)
                        .thresholdValue(rule.getThresholdValue())
                        .potentialImpact(interpolateTemplate(rule.getPotentialImpactTemplate(), ratioCode, null, rule))
                        .recommendation(interpolateTemplate(rule.getRecommendationTemplate(), ratioCode, null, rule))
                        .sourceReference("INSUFFICIENT_CALCULATION_DATA")
                        .detectedAt(LocalDateTime.now())
                        .build();
                    results.add(insufficientRisk);
                    continue;
                }

                BigDecimal actual = calcResult.getResultValue();
                boolean isTriggered = evaluateCondition(actual, rule);

                if (isTriggered) {
                    boolean wasPreviouslyActive = previouslyActiveRuleCodes.contains(rule.getRuleCode());
                    RiskStatus status = wasPreviouslyActive ? RiskStatus.MONITORING : RiskStatus.DETECTED;

                    IdentifiedRisk triggeredRisk = IdentifiedRisk.builder()
                            .riskRule(rule)
                            .ruleCodeSnapshot(rule.getRuleCode())
                            .riskTitle(rule.getRuleName())
                            .riskDescription(interpolateTemplate(rule.getExplanationTemplate(), ratioCode, actual, rule))
                            .riskCategory(category)
                            .severity(rule.getSeverity())
                            .riskStatus(status)
                            .relatedRatioCode(ratioCode)
                            .actualValue(actual)
                            .thresholdValue(rule.getThresholdValue())
                            .potentialImpact(interpolateTemplate(rule.getPotentialImpactTemplate(), ratioCode, actual, rule))
                            .recommendation(interpolateTemplate(rule.getRecommendationTemplate(), ratioCode, actual, rule))
                            .sourceReference("CALCULATION_RESULT_" + ratioCode)
                            .detectedAt(LocalDateTime.now())
                            .build();

                    results.add(triggeredRisk);
                } else if (previouslyActiveRuleCodes.contains(rule.getRuleCode())) {
                    // Rule: A risk that previously held but no longer satisfies condition is marked as RESOLVED
                    IdentifiedRisk resolvedRisk = IdentifiedRisk.builder()
                            .riskRule(rule)
                            .ruleCodeSnapshot(rule.getRuleCode())
                            .riskTitle(rule.getRuleName())
                            .riskDescription(String.format("Rủi ro '%s' đã được giải tỏa. Giá trị hiện tại (%s) không còn vi phạm ngưỡng cảnh báo.",
                                    rule.getRuleName(), actual.toPlainString()))
                            .riskCategory(category)
                            .severity(rule.getSeverity())
                            .riskStatus(RiskStatus.RESOLVED)
                            .relatedRatioCode(ratioCode)
                            .actualValue(actual)
                            .thresholdValue(rule.getThresholdValue())
                            .potentialImpact("Rủi ro đã được kiểm soát tốt.")
                            .recommendation("Tiếp tục theo dõi và duy trì hiệu quả tài chính của chỉ số này.")
                            .sourceReference("CALCULATION_RESULT_" + ratioCode)
                            .detectedAt(LocalDateTime.now())
                            .build();

                    results.add(resolvedRisk);
                }
            }
        }

        return results;
    }

    private boolean evaluateCondition(BigDecimal actual, RiskRule rule) {
        ComparisonOperator op = rule.getComparisonOperator();
        BigDecimal threshold = rule.getThresholdValue();

        if (op == null) return false;

        return switch (op) {
            case GREATER_THAN -> threshold != null && actual.compareTo(threshold) > 0;
            case GREATER_THAN_OR_EQUAL -> threshold != null && actual.compareTo(threshold) >= 0;
            case LESS_THAN -> threshold != null && actual.compareTo(threshold) < 0;
            case LESS_THAN_OR_EQUAL -> threshold != null && actual.compareTo(threshold) <= 0;
            case EQUAL -> threshold != null && actual.compareTo(threshold) == 0;
            case BETWEEN -> {
                BigDecimal min = rule.getMinimumValue();
                BigDecimal max = rule.getMaximumValue();
                if (min != null && max != null) {
                    yield actual.compareTo(min) >= 0 && actual.compareTo(max) <= 0;
                } else if (threshold != null) {
                    yield actual.compareTo(threshold) == 0;
                }
                yield false;
            }
        };
    }

    protected String interpolateTemplate(String template, String ratioCode, BigDecimal actual, RiskRule rule) {
        if (template == null) return "";
        String opSymbol = mapper.getOperatorSymbol(rule.getComparisonOperator());
        String thresholdStr = rule.getThresholdValue() != null ? rule.getThresholdValue().toPlainString() : "";
        String actualStr = actual != null ? actual.toPlainString() : "N/A";

        return template
                .replace("{ratioCode}", ratioCode != null ? ratioCode : "")
                .replace("{ratioName}", ratioCode != null ? ratioCode : "")
                .replace("{actualValue}", actualStr)
                .replace("{thresholdValue}", thresholdStr)
                .replace("{comparisonOperator}", opSymbol != null ? opSymbol : "");
    }
}

