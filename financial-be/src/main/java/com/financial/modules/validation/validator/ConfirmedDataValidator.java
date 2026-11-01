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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmedDataValidator implements CategoryValidator {

    private final IndicatorResolverService indicatorResolver;
    private final ObjectMapper objectMapper;

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.CONFIRMED_DATA;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        List<String> requiredIndicators = List.of(
                "TOTAL_ASSETS",
                "CURRENT_ASSETS",
                "TOTAL_LIABILITIES",
                "EQUITY",
                "NET_REVENUE",
                "NET_PROFIT"
        );

        if (rule.getConfiguration() != null && !rule.getConfiguration().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(rule.getConfiguration());
                if (root.has("requiredIndicators") && root.get("requiredIndicators").isArray()) {
                    List<String> parsed = new ArrayList<>();
                    for (JsonNode in : root.get("requiredIndicators")) {
                        parsed.add(in.asText());
                    }
                    if (!parsed.isEmpty()) requiredIndicators = parsed;
                }
            } catch (Exception e) {
                log.warn("Failed to parse confirmed data rule configuration: {}", e.getMessage());
            }
        }

        List<String> missingList = new ArrayList<>();
        List<String> presentList = new ArrayList<>();

        for (String code : requiredIndicators) {
            BigDecimal val = indicatorResolver.resolveValueByCode(statement, code, PeriodReference.CURRENT);
            if (val == null) {
                missingList.add(code);
            } else {
                presentList.add(code);
            }
        }

        String affected = String.join(",", requiredIndicators);

        if (!missingList.isEmpty()) {
            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.FAIL)
                    .affectedIndicators(String.join(",", missingList))
                    .explanation("Dữ liệu tài chính đã xác nhận còn thiếu các chỉ tiêu bắt buộc: " + String.join(", ", missingList))
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
                .explanation("Dữ liệu đã xác nhận đầy đủ tất cả các chỉ tiêu tài chính bắt buộc.")
                .build();
    }
}

