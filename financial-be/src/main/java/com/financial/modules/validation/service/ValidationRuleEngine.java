package com.financial.modules.validation.service;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.repository.ValidationRuleRepository;
import com.financial.modules.validation.validator.CategoryValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ValidationRuleEngine {

    private final ValidationRuleRepository ruleRepository;
    private final Map<ValidationCategory, CategoryValidator> validatorMap = new EnumMap<>(ValidationCategory.class);

    public ValidationRuleEngine(ValidationRuleRepository ruleRepository, List<CategoryValidator> validators) {
        this.ruleRepository = ruleRepository;
        if (validators != null) {
            for (CategoryValidator v : validators) {
                validatorMap.put(v.getCategory(), v);
            }
        }
    }

    /**
     * Resolves the accounting standard and executes all applicable active validation rules.
     */
    public List<ValidationResult> executeValidation(FinancialStatement statement) {
        AccountingStandard standard = resolveAccountingStandard(statement);

        if (standard == null) {
            log.warn("Cannot identify a supported accounting standard for statement ID {}. Statement standard: '{}'",
                    statement.getId(), statement.getAccountingStandard());
            // Return empty or review-required indicator rather than applying arbitrary rules
            return List.of(ValidationResult.builder()
                    .ruleCodeSnapshot("STD_UNKNOWN")
                    .ruleNameSnapshot("Kiểm tra chuẩn mực kế toán")
                    .category(ValidationCategory.CONFIRMED_DATA)
                    .severity(com.financial.modules.validation.enums.ValidationSeverity.HIGH)
                    .status(ValidationStatus.NOT_APPLICABLE)
                    .explanation("Chuẩn mực kế toán của báo cáo tài chính ('" + statement.getAccountingStandard() + "') không được hỗ trợ hoặc không xác định. Không thể áp dụng bộ quy tắc kiểm tra kế toán.")
                    .build());
        }

        List<ValidationRule> applicableRules = ruleRepository.findApplicableRules(standard);
        log.info("Loaded {} active validation rules applicable for standard {} for statement ID {}",
                applicableRules.size(), standard, statement.getId());

        List<ValidationResult> results = new ArrayList<>();

        for (ValidationRule rule : applicableRules) {
            if (!Boolean.TRUE.equals(rule.getIsActive())) {
                continue;
            }

            CategoryValidator validator = validatorMap.get(rule.getCategory());
            if (validator != null) {
                try {
                    ValidationResult res = validator.validate(statement, rule);
                    results.add(res);
                } catch (Exception e) {
                    log.error("Error executing validation rule '{}': {}", rule.getRuleCode(), e.getMessage(), e);
                    results.add(ValidationResult.builder()
                            .validationRule(rule)
                            .ruleCodeSnapshot(rule.getRuleCode())
                            .ruleNameSnapshot(rule.getRuleName())
                            .category(rule.getCategory())
                            .severity(rule.getSeverity())
                            .status(ValidationStatus.ERROR)
                            .explanation("Lỗi thực thi quy tắc kiểm tra: " + e.getMessage())
                            .build());
                }
            } else {
                log.warn("No category validator found for category {}", rule.getCategory());
                results.add(ValidationResult.builder()
                        .validationRule(rule)
                        .ruleCodeSnapshot(rule.getRuleCode())
                        .ruleNameSnapshot(rule.getRuleName())
                        .category(rule.getCategory())
                        .severity(rule.getSeverity())
                        .status(ValidationStatus.NOT_APPLICABLE)
                        .explanation("Chưa hỗ trợ bộ xử lý kiểm tra cho danh mục: " + rule.getCategory())
                        .build());
            }
        }

        return results;
    }

    public AccountingStandard resolveAccountingStandard(FinancialStatement statement) {
        if (statement == null || statement.getAccountingStandard() == null || statement.getAccountingStandard().isBlank()) {
            return AccountingStandard.TT200; // Standard fallback if completely empty
        }
        String stdStr = statement.getAccountingStandard().trim().toUpperCase();
        for (AccountingStandard std : AccountingStandard.values()) {
            if (std.name().equals(stdStr)) {
                return std;
            }
        }
        return null;
    }
}

