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

@Slf4j
@Component
@RequiredArgsConstructor
public class IncomeStatementValidator implements CategoryValidator {

    private final IndicatorResolverService indicatorResolver;
    private final ObjectMapper objectMapper;

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.INCOME_STATEMENT;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        String resultCode = "GROSS_PROFIT";
        String minuendCode = "NET_REVENUE";
        String subtrahendCode = "COST_OF_GOODS_SOLD";

        if (rule.getConfiguration() != null && !rule.getConfiguration().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rule.getConfiguration());
                if (root.has("result")) resultCode = root.get("result").asText();
                if (root.has("minuend")) minuendCode = root.get("minuend").asText();
                if (root.has("subtrahend")) subtrahendCode = root.get("subtrahend").asText();
            } catch (Exception e) {
                log.warn("Failed to parse income statement rule configuration: {}", e.getMessage());
            }
        }

        BigDecimal resVal = indicatorResolver.resolveValueByCode(statement, resultCode, PeriodReference.CURRENT);
        BigDecimal minVal = indicatorResolver.resolveValueByCode(statement, minuendCode, PeriodReference.CURRENT);
        BigDecimal subVal = indicatorResolver.resolveValueByCode(statement, subtrahendCode, PeriodReference.CURRENT);

        String affected = resultCode + "," + minuendCode + "," + subtrahendCode;

        if (resVal == null || minVal == null || subVal == null) {
            StringBuilder missing = new StringBuilder();
            if (resVal == null) missing.append(resultCode).append(" ");
            if (minVal == null) missing.append(minuendCode).append(" ");
            if (subVal == null) missing.append(subtrahendCode).append(" ");

            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.REVIEW_REQUIRED)
                    .expectedValue(null)
                    .actualValue(resVal)
                    .toleranceValue(rule.getToleranceValue())
                    .affectedIndicators(affected)
                    .explanation("Không thể kiểm tra quan hệ Báo cáo kết quả kinh doanh do thiếu chỉ tiêu: " + missing.toString().trim())
                    .build();
        }

        BigDecimal expected = minVal.subtract(subVal);
        BigDecimal actual = resVal;
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
                ? String.format("Quan hệ Báo cáo kết quả kinh doanh khớp. %s (%s) = %s (%s) - %s (%s). Chênh lệch: %s.",
                resultCode, actual, minuendCode, minVal, subtrahendCode, subVal, difference)
                : String.format("Quan hệ Báo cáo kết quả kinh doanh không khớp: %s (%s) != %s - %s (kỳ vọng: %s). Chênh lệch: %s (vượt dung sai %s).",
                resultCode, actual, minuendCode, subtrahendCode, expected, difference, tolerance);

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
                .affectedIndicators(affected)
                .explanation(explanation)
                .build();
    }
}

