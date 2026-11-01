package com.financial.modules.validation.validator;

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
public class AccountingEquationValidator implements CategoryValidator {

    private final IndicatorResolverService indicatorResolver;

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.ACCOUNTING_EQUATION;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        BigDecimal totalAssets = indicatorResolver.resolveValueByCode(statement, "TOTAL_ASSETS", PeriodReference.CURRENT);
        BigDecimal totalLiabilities = indicatorResolver.resolveValueByCode(statement, "TOTAL_LIABILITIES", PeriodReference.CURRENT);
        BigDecimal equity = indicatorResolver.resolveValueByCode(statement, "EQUITY", PeriodReference.CURRENT);

        String affected = "TOTAL_ASSETS,TOTAL_LIABILITIES,EQUITY";

        if (totalAssets == null || totalLiabilities == null || equity == null) {
            StringBuilder missing = new StringBuilder();
            if (totalAssets == null) missing.append("Tổng tài sản (TOTAL_ASSETS) ");
            if (totalLiabilities == null) missing.append("Nợ phải trả (TOTAL_LIABILITIES) ");
            if (equity == null) missing.append("Vốn chủ sở hữu (EQUITY) ");

            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.REVIEW_REQUIRED)
                    .expectedValue(null)
                    .actualValue(totalAssets)
                    .differenceValue(null)
                    .toleranceValue(rule.getToleranceValue())
                    .affectedIndicators(affected)
                    .explanation("Không thể kiểm tra phương trình kế toán do thiếu chỉ tiêu: " + missing.toString().trim())
                    .build();
        }

        BigDecimal expected = totalLiabilities.add(equity);
        BigDecimal actual = totalAssets;
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
                ? String.format("Phương trình kế toán cân đối. Tổng tài sản (%s) = Nợ phải trả (%s) + Vốn chủ sở hữu (%s). Chênh lệch: %s (trong ngưỡng dung sai: %s).",
                actual, totalLiabilities, equity, difference, tolerance)
                : String.format("Phương trình kế toán không cân đối: Tổng tài sản (%s) != Nợ phải trả + Vốn chủ sở hữu (%s). Chênh lệch: %s (vượt ngưỡng dung sai: %s).",
                actual, expected, difference, tolerance);

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

