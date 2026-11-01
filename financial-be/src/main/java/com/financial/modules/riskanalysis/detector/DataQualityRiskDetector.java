package com.financial.modules.riskanalysis.detector;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.enums.ValidationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataQualityRiskDetector implements RiskDetector {

    private final RiskAnalysisMapper mapper;

    @Override
    public RiskCategory getCategory() {
        return RiskCategory.DATA_QUALITY;
    }

    @Override
    public List<IdentifiedRisk> detect(RiskDetectionContext context, List<RiskRule> rules) {
        List<IdentifiedRisk> results = new ArrayList<>();
        List<RiskRule> dqRules = new ArrayList<>();

        if (rules != null) {
            for (RiskRule r : rules) {
                if (r.getRiskCategory() == RiskCategory.DATA_QUALITY && r.getIsActive()) {
                    dqRules.add(r);
                }
            }
        }

        // Rule: Only create a risk when an applicable risk policy is configured
        if (dqRules.isEmpty()) {
            log.debug("No active data quality risk rules configured.");
            return results;
        }

        ValidationExecution val = context.getValidationExecution();
        if (val == null) {
            // Validation not executed -> INSUFFICIENT_DATA
            for (RiskRule rule : dqRules) {
                IdentifiedRisk risk = IdentifiedRisk.builder()
                        .riskRule(rule)
                        .ruleCodeSnapshot(rule.getRuleCode())
                        .riskTitle(rule.getRuleName())
                        .riskDescription("Báo cáo tài chính chưa thực hiện kiểm tra tính hợp lệ dữ liệu kế toán (Validation).")
                        .riskCategory(RiskCategory.DATA_QUALITY)
                        .severity(rule.getSeverity())
                        .riskStatus(RiskStatus.INSUFFICIENT_DATA)
                        .sourceReference("NO_VALIDATION_EXECUTION")
                        .detectedAt(LocalDateTime.now())
                        .build();
                results.add(risk);
            }
            return results;
        }

        Set<String> previouslyActiveCodes = new HashSet<>();
        if (context.getPreviousRisks() != null) {
            for (IdentifiedRisk prev : context.getPreviousRisks()) {
                if ((prev.getRiskStatus() == RiskStatus.DETECTED || prev.getRiskStatus() == RiskStatus.MONITORING)
                        && prev.getRuleCodeSnapshot() != null) {
                    previouslyActiveCodes.add(prev.getRuleCodeSnapshot());
                }
            }
        }

        for (RiskRule rule : dqRules) {
            BigDecimal actualFailures = BigDecimal.valueOf(val.getFailedRules() != null ? val.getFailedRules() : 0);
            boolean isTriggered = false;

            if (val.getExecutionStatus() == ValidationStatus.FAIL || actualFailures.compareTo(BigDecimal.ZERO) > 0) {
                // If rule evaluates failure count
                if (rule.getComparisonOperator() == ComparisonOperator.GREATER_THAN) {
                    BigDecimal threshold = rule.getThresholdValue() != null ? rule.getThresholdValue() : BigDecimal.ZERO;
                    isTriggered = actualFailures.compareTo(threshold) > 0;
                } else {
                    isTriggered = true;
                }
            } else if (rule.getThresholdValue() != null && val.getWarningRules() != null && val.getWarningRules() > 0) {
                // If rule specifically targets warning threshold
                BigDecimal actualWarnings = BigDecimal.valueOf(val.getWarningRules());
                if (rule.getComparisonOperator() == ComparisonOperator.GREATER_THAN) {
                    isTriggered = actualWarnings.compareTo(rule.getThresholdValue()) > 0;
                }
            }

            if (isTriggered) {
                boolean wasPrev = previouslyActiveCodes.contains(rule.getRuleCode());
                RiskStatus status = wasPrev ? RiskStatus.MONITORING : RiskStatus.DETECTED;

                String explanation = (rule.getExplanationTemplate() != null && !rule.getExplanationTemplate().isBlank())
                        ? rule.getExplanationTemplate().replace("{actualValue}", actualFailures.toPlainString())
                        : String.format("Phát hiện %d lỗi kế toán hoặc bất cân đối số liệu theo quy tắc kiểm tra tính hợp lệ.", actualFailures.intValue());

                String recommendation = (rule.getRecommendationTemplate() != null && !rule.getRecommendationTemplate().isBlank())
                        ? rule.getRecommendationTemplate()
                        : "Cần kiểm tra và đối soát lại các khoản mục BCTC trước khi sử dụng kết quả đánh giá.";

                IdentifiedRisk risk = IdentifiedRisk.builder()
                        .riskRule(rule)
                        .ruleCodeSnapshot(rule.getRuleCode())
                        .riskTitle(rule.getRuleName())
                        .riskDescription(explanation)
                        .riskCategory(RiskCategory.DATA_QUALITY)
                        .severity(rule.getSeverity())
                        .riskStatus(status)
                        .actualValue(actualFailures)
                        .thresholdValue(rule.getThresholdValue())
                        .potentialImpact(rule.getPotentialImpactTemplate())
                        .recommendation(recommendation)
                        .sourceReference("VALIDATION_EXECUTION_" + val.getId())
                        .detectedAt(LocalDateTime.now())
                        .build();

                results.add(risk);
            } else if (previouslyActiveCodes.contains(rule.getRuleCode())) {
                // Was active previously, but now no failures exist -> mark RESOLVED
                IdentifiedRisk resolved = IdentifiedRisk.builder()
                        .riskRule(rule)
                        .ruleCodeSnapshot(rule.getRuleCode())
                        .riskTitle(rule.getRuleName())
                        .riskDescription(String.format("Rủi ro dữ liệu '%s' đã được giải tỏa. Số liệu BCTC hiện tại hoàn toàn cân đối.", rule.getRuleName()))
                        .riskCategory(RiskCategory.DATA_QUALITY)
                        .severity(rule.getSeverity())
                        .riskStatus(RiskStatus.RESOLVED)
                        .actualValue(actualFailures)
                        .thresholdValue(rule.getThresholdValue())
                        .potentialImpact("Số liệu kế toán đạt chuẩn.")
                        .recommendation("Tiếp tục kiểm soát định kỳ dữ liệu BCTC.")
                        .sourceReference("VALIDATION_EXECUTION_" + val.getId())
                        .detectedAt(LocalDateTime.now())
                        .build();

                results.add(resolved);
            }
        }

        return results;
    }
}

