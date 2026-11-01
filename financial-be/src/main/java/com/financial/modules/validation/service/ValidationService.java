package com.financial.modules.validation.service;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.dto.*;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ValidationService {

    /**
     * Executes official financial data validation against the statement's latest confirmed data version.
     */
    ValidationResponse validateStatement(Long userId, Long statementId, ValidationRequest request);

    /**
     * Retrieves the latest validation execution for a financial statement.
     */
    ValidationResponse getLatestValidation(Long userId, Long statementId);

    /**
     * Retrieves validation history summary for a financial statement.
     */
    List<ValidationHistoryResponse> getValidationHistory(Long userId, Long statementId);

    /**
     * Retrieves a specific validation execution by its ID.
     */
    ValidationResponse getValidationById(Long userId, Long statementId, Long executionId);

    /**
     * Retrieves detailed results for a specific validation execution.
     */
    List<ValidationResultResponse> getValidationResults(Long userId, Long statementId, Long executionId);

    /**
     * Lists validation rules with optional filters.
     */
    Page<ValidationRuleResponse> getValidationRules(
            AccountingStandard standard,
            FinancialStatementType statementType,
            ValidationCategory category,
            ValidationSeverity severity,
            Boolean isActive,
            Pageable pageable
    );

    /**
     * Retrieves a validation rule by its ID.
     */
    ValidationRuleResponse getValidationRuleById(Long id);

    /**
     * Creates a new validation rule.
     */
    ValidationRuleResponse createValidationRule(ValidationRuleRequest request);

    /**
     * Updates an existing validation rule.
     */
    ValidationRuleResponse updateValidationRule(Long id, ValidationRuleRequest request);

    /**
     * Activates or deactivates a validation rule.
     */
    ValidationRuleResponse updateValidationRuleStatus(Long id, Boolean isActive);
}

