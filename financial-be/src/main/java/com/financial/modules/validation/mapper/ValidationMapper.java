package com.financial.modules.validation.mapper;

import com.financial.modules.validation.dto.*;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class ValidationMapper {

    public ValidationResponse toResponse(ValidationExecution execution) {
        if (execution == null) return null;

        List<ValidationResultResponse> resultDtos = execution.getResults() != null
                ? execution.getResults().stream().map(this::toResultResponse).toList()
                : Collections.emptyList();

        return ValidationResponse.builder()
                .id(execution.getId())
                .statementId(execution.getFinancialStatement() != null ? execution.getFinancialStatement().getId() : null)
                .confirmedDataVersion(execution.getConfirmedDataVersion())
                .accountingStandard(execution.getAccountingStandard())
                .executionStatus(execution.getExecutionStatus())
                .executionStatusDisplayName(execution.getExecutionStatus() != null ? execution.getExecutionStatus().getDisplayName() : null)
                .totalRules(execution.getTotalRules())
                .passedRules(execution.getPassedRules())
                .warningRules(execution.getWarningRules())
                .failedRules(execution.getFailedRules())
                .reviewRequiredRules(execution.getReviewRequiredRules())
                .errorRules(execution.getErrorRules())
                .notApplicableRules(execution.getNotApplicableRules())
                .executedAt(execution.getExecutedAt())
                .createdAt(execution.getCreatedAt())
                .results(resultDtos)
                .build();
    }

    public ValidationResultResponse toResultResponse(ValidationResult result) {
        if (result == null) return null;

        return ValidationResultResponse.builder()
                .id(result.getId())
                .ruleCode(result.getRuleCodeSnapshot())
                .ruleName(result.getRuleNameSnapshot())
                .category(result.getCategory())
                .categoryDisplayName(result.getCategory() != null ? result.getCategory().getDisplayName() : null)
                .status(result.getStatus())
                .statusDisplayName(result.getStatus() != null ? result.getStatus().getDisplayName() : null)
                .severity(result.getSeverity())
                .severityDisplayName(result.getSeverity() != null ? result.getSeverity().getDisplayName() : null)
                .expectedValue(result.getExpectedValue())
                .actualValue(result.getActualValue())
                .differenceValue(result.getDifferenceValue())
                .toleranceValue(result.getToleranceValue())
                .explanation(result.getExplanation())
                .affectedIndicators(result.getAffectedIndicators())
                .sourcePages(result.getSourcePages())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public ValidationHistoryResponse toHistoryResponse(ValidationExecution execution) {
        if (execution == null) return null;

        return ValidationHistoryResponse.builder()
                .id(execution.getId())
                .statementId(execution.getFinancialStatement() != null ? execution.getFinancialStatement().getId() : null)
                .confirmedDataVersion(execution.getConfirmedDataVersion())
                .accountingStandard(execution.getAccountingStandard())
                .executionStatus(execution.getExecutionStatus())
                .executionStatusDisplayName(execution.getExecutionStatus() != null ? execution.getExecutionStatus().getDisplayName() : null)
                .totalRules(execution.getTotalRules())
                .passedRules(execution.getPassedRules())
                .warningRules(execution.getWarningRules())
                .failedRules(execution.getFailedRules())
                .reviewRequiredRules(execution.getReviewRequiredRules())
                .executedAt(execution.getExecutedAt())
                .createdAt(execution.getCreatedAt())
                .build();
    }

    public ValidationRuleResponse toRuleResponse(ValidationRule rule) {
        if (rule == null) return null;

        return ValidationRuleResponse.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .accountingStandard(rule.getAccountingStandard())
                .statementType(rule.getStatementType())
                .category(rule.getCategory())
                .categoryDisplayName(rule.getCategory() != null ? rule.getCategory().getDisplayName() : null)
                .severity(rule.getSeverity())
                .severityDisplayName(rule.getSeverity() != null ? rule.getSeverity().getDisplayName() : null)
                .configuration(rule.getConfiguration())
                .toleranceValue(rule.getToleranceValue())
                .toleranceType(rule.getToleranceType())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    public ValidationRule toRuleEntity(ValidationRuleRequest request) {
        if (request == null) return null;

        return ValidationRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .accountingStandard(request.getAccountingStandard())
                .statementType(request.getStatementType())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .configuration(request.getConfiguration())
                .toleranceValue(request.getToleranceValue() != null ? request.getToleranceValue() : BigDecimal.ZERO)
                .toleranceType(request.getToleranceType())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public void updateRuleEntity(ValidationRule rule, ValidationRuleRequest request) {
        if (rule == null || request == null) return;

        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setAccountingStandard(request.getAccountingStandard());
        rule.setStatementType(request.getStatementType());
        rule.setCategory(request.getCategory());
        rule.setSeverity(request.getSeverity());
        rule.setConfiguration(request.getConfiguration());
        if (request.getToleranceValue() != null) {
            rule.setToleranceValue(request.getToleranceValue());
        }
        if (request.getToleranceType() != null) {
            rule.setToleranceType(request.getToleranceType());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
    }
}

