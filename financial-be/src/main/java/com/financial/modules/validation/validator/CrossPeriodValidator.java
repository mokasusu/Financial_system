package com.financial.modules.validation.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.calculationengine.service.IndicatorResolverService;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossPeriodValidator implements CategoryValidator {

    private final IndicatorResolverService indicatorResolver;
    private final ObjectMapper objectMapper;

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.CROSS_PERIOD;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        String checkType = "NEGATIVE_CHECK"; // or "FLUCTUATION_CHECK"
        List<String> targetIndicators = List.of("TOTAL_ASSETS", "CURRENT_ASSETS", "INVENTORY", "CASH_AND_EQUIVALENTS", "NET_REVENUE");
        BigDecimal maxChangePercent = rule.getToleranceValue() != null && rule.getToleranceValue().compareTo(BigDecimal.ZERO) > 0
                ? rule.getToleranceValue()
                : new BigDecimal("100.0"); // 100% default fluctuation warning threshold

        if (rule.getConfiguration() != null && !rule.getConfiguration().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rule.getConfiguration());
                if (root.has("checkType")) {
                    checkType = root.get("checkType").asText();
                }
                if (root.has("indicators") && root.get("indicators").isArray()) {
                    List<String> parsed = new ArrayList<>();
                    for (JsonNode in : root.get("indicators")) {
                        parsed.add(in.asText());
                    }
                    if (!parsed.isEmpty()) targetIndicators = parsed;
                }
                if (root.has("maxChangePercent")) {
                    maxChangePercent = new BigDecimal(root.get("maxChangePercent").asText());
                }
            } catch (Exception e) {
                log.warn("Failed to parse cross period rule configuration: {}", e.getMessage());
            }
        }

        if ("NEGATIVE_CHECK".equalsIgnoreCase(checkType)) {
            return validateNegativeValues(statement, rule, targetIndicators);
        } else {
            return validateFluctuations(statement, rule, targetIndicators, maxChangePercent);
        }
    }

    private ValidationResult validateNegativeValues(FinancialStatement statement, ValidationRule rule, List<String> indicators) {
        List<String> negativeFound = new ArrayList<>();

        for (String code : indicators) {
            BigDecimal val = indicatorResolver.resolveValueByCode(statement, code, PeriodReference.CURRENT);
            if (val != null && val.compareTo(BigDecimal.ZERO) < 0) {
                negativeFound.add(code + " (" + val + ")");
            }
        }

        String affected = String.join(",", indicators);

        if (!negativeFound.isEmpty()) {
            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.WARNING)
                    .affectedIndicators(String.join(",", negativeFound))
                    .explanation("Phát hiện chỉ tiêu mang giá trị âm bất thường: " + String.join(", ", negativeFound))
                    .build();
        }

        return ValidationResult.builder()
                .validationRule(rule)
                .ruleCodeSnapshot(rule.getRuleCode())
                .ruleNameSnapshot(rule.getRuleName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .status(ValidationStatus.PASS)
                .affectedIndicators(affected)
                .explanation("Không phát hiện giá trị âm bất thường trong các chỉ tiêu tài sản và doanh thu trọng yếu.")
                .build();
    }

    private ValidationResult validateFluctuations(FinancialStatement statement, ValidationRule rule, List<String> indicators, BigDecimal maxChangePercent) {
        List<String> largeChanges = new ArrayList<>();
        List<String> missingPrevious = new ArrayList<>();

        for (String code : indicators) {
            BigDecimal cur = indicatorResolver.resolveValueByCode(statement, code, PeriodReference.CURRENT);
            BigDecimal prev = indicatorResolver.resolveValueByCode(statement, code, PeriodReference.BEGINNING);

            if (cur != null) {
                if (prev == null) {
                    missingPrevious.add(code);
                } else if (prev.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = cur.subtract(prev).abs();
                    BigDecimal pct = diff.divide(prev, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(100));
                    if (pct.compareTo(maxChangePercent) > 0) {
                        largeChanges.add(String.format("%s (biến động %s%% > ngưỡng %s%%)", code, pct.setScale(2, java.math.RoundingMode.HALF_UP), maxChangePercent));
                    }
                }
            }
        }

        String affected = String.join(",", indicators);

        if (!largeChanges.isEmpty()) {
            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.WARNING)
                    .toleranceValue(maxChangePercent)
                    .affectedIndicators(affected)
                    .explanation("Cảnh báo biến động mạnh liên kỳ: " + String.join("; ", largeChanges))
                    .build();
        }

        if (!missingPrevious.isEmpty()) {
            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.WARNING)
                    .affectedIndicators(String.join(",", missingPrevious))
                    .explanation("Cảnh báo: Thiếu số liệu đầu kỳ để so sánh liên kỳ cho chỉ tiêu: " + String.join(", ", missingPrevious))
                    .build();
        }

        return ValidationResult.builder()
                .validationRule(rule)
                .ruleCodeSnapshot(rule.getRuleCode())
                .ruleNameSnapshot(rule.getRuleName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .status(ValidationStatus.PASS)
                .toleranceValue(maxChangePercent)
                .affectedIndicators(affected)
                .explanation("Biến động liên kỳ của các chỉ tiêu nằm trong phạm vi ngưỡng cho phép.")
                .build();
    }
}

