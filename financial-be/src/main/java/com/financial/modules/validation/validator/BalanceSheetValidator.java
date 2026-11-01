package com.financial.modules.validation.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.calculationengine.service.IndicatorResolverService;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ToleranceType;
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
public class BalanceSheetValidator implements CategoryValidator {

    private final IndicatorResolverService indicatorResolver;
    private final ObjectMapper objectMapper;

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.BALANCE_SHEET_SUBTOTAL;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        String parentCode = "TOTAL_ASSETS";
        List<String> componentCodes = List.of("CURRENT_ASSETS", "NON_CURRENT_ASSETS");

        // Parse structured JSON config if available
        if (rule.getConfiguration() != null && !rule.getConfiguration().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rule.getConfiguration());
                if (root.has("parent")) {
                    parentCode = root.get("parent").asText();
                }
                if (root.has("components") && root.get("components").isArray()) {
                    List<String> parsed = new ArrayList<>();
                    for (JsonNode c : root.get("components")) {
                        parsed.add(c.asText());
                    }
                    if (!parsed.isEmpty()) {
                        componentCodes = parsed;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse balance sheet subtotal rule configuration: {}", e.getMessage());
            }
        }

        BigDecimal parentValue = indicatorResolver.resolveValueByCode(statement, parentCode, PeriodReference.CURRENT);
        BigDecimal componentSum = BigDecimal.ZERO;
        List<String> missingComponents = new ArrayList<>();
        List<String> foundComponents = new ArrayList<>();

        for (String comp : componentCodes) {
            BigDecimal val = indicatorResolver.resolveValueByCode(statement, comp, PeriodReference.CURRENT);
            if (val != null) {
                componentSum = componentSum.add(val);
                foundComponents.add(comp + " (" + val + ")");
            } else {
                missingComponents.add(comp);
            }
        }

        String allAffected = parentCode + "," + String.join(",", componentCodes);

        if (parentValue == null || !missingComponents.isEmpty()) {
            String explanation = String.format("Không thể kiểm tra tổng phụ Bảng cân đối do thiếu chỉ tiêu: %s",
                    (parentValue == null ? parentCode + " " : "") + String.join(", ", missingComponents));

            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.REVIEW_REQUIRED)
                    .expectedValue(null)
                    .actualValue(parentValue)
                    .toleranceValue(rule.getToleranceValue())
                    .affectedIndicators(allAffected)
                    .explanation(explanation)
                    .build();
        }

        BigDecimal expected = componentSum;
        BigDecimal actual = parentValue;
        BigDecimal difference = actual.subtract(expected).abs();

        BigDecimal tolerance = rule.getToleranceValue() != null ? rule.getToleranceValue() : BigDecimal.ZERO;
        boolean isWithinTolerance;

        if (rule.getToleranceType() == ToleranceType.PERCENTAGE) {
            if (expected.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal percentDiff = difference.divide(expected.abs(), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(100));
                isWithinTolerance = percentDiff.compareTo(tolerance) <= 0;
            } else {
                isWithinTolerance = difference.compareTo(BigDecimal.ZERO) == 0;
            }
        } else {
            isWithinTolerance = difference.compareTo(tolerance) <= 0;
        }

        ValidationStatus status = isWithinTolerance ? ValidationStatus.PASS : ValidationStatus.FAIL;
        String explanation = isWithinTolerance
                ? String.format("Tổng phụ Bảng cân đối kế toán khớp. %s (%s) = tổng các bộ phận (%s). Chênh lệch: %s.",
                parentCode, actual, expected, difference)
                : String.format("Tổng phụ Bảng cân đối không khớp. %s (%s) != tổng các bộ phận (%s). Chênh lệch: %s (vượt dung sai %s).",
                parentCode, actual, expected, difference, tolerance);

        return ValidationResult.builder()
                .validationRule(rule)
                .ruleCodeSnapshot(rule.getRuleCode())
                .ruleNameSnapshot(rule.getRuleName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .status(status)
                .expectedValue(expected)
                .actualValue(actual)
                .differenceValue(difference)
                .toleranceValue(tolerance)
                .affectedIndicators(allAffected)
                .explanation(explanation)
                .build();
    }
}

