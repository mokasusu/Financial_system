package com.financial.modules.validation.service;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.repository.ValidationRuleRepository;
import com.financial.modules.validation.validator.CategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationRuleEngineTest {

    @Mock
    private ValidationRuleRepository ruleRepository;

    @Mock
    private CategoryValidator mockValidator;

    private ValidationRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        when(mockValidator.getCategory()).thenReturn(ValidationCategory.ACCOUNTING_EQUATION);
        ruleEngine = new ValidationRuleEngine(ruleRepository, List.of(mockValidator));
    }

    @Test
    @DisplayName("Different accounting standards load their specific rule sets")
    void testLoadsStandardSpecificRules() {
        FinancialStatement tt200Stmt = FinancialStatement.builder()
                .id(1L)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        ValidationRule r1 = ValidationRule.builder()
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("TT200 Rule")
                .accountingStandard(AccountingStandard.TT200)
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .isActive(true)
                .build();

        when(ruleRepository.findApplicableRules(AccountingStandard.TT200)).thenReturn(List.of(r1));
        when(mockValidator.validate(eq(tt200Stmt), eq(r1))).thenReturn(ValidationResult.builder()
                .ruleCodeSnapshot("BS_EQ_TT200_01")
                .status(ValidationStatus.PASS)
                .build());

        List<ValidationResult> results = ruleEngine.executeValidation(tt200Stmt);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRuleCodeSnapshot()).isEqualTo("BS_EQ_TT200_01");
        verify(ruleRepository).findApplicableRules(AccountingStandard.TT200);
    }

    @Test
    @DisplayName("Unsupported or invalid accounting standard does not execute arbitrary rules")
    void testUnsupportedStandard_ReturnsNotApplicable() {
        FinancialStatement unknownStmt = FinancialStatement.builder()
                .id(2L)
                .accountingStandard("UNKNOWN_STANDARD_XYZ")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        List<ValidationResult> results = ruleEngine.executeValidation(unknownStmt);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ValidationStatus.NOT_APPLICABLE);
        assertThat(results.get(0).getExplanation()).contains("không được hỗ trợ");
        verify(ruleRepository, never()).findApplicableRules(any());
        verify(mockValidator, never()).validate(any(), any());
    }

    @Test
    @DisplayName("Inactive rules are not executed")
    void testInactiveRules_AreSkipped() {
        FinancialStatement stmt = FinancialStatement.builder()
                .id(1L)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        ValidationRule inactiveRule = ValidationRule.builder()
                .ruleCode("INACTIVE_01")
                .ruleName("Inactive Rule")
                .accountingStandard(AccountingStandard.TT200)
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .isActive(false)
                .build();

        when(ruleRepository.findApplicableRules(AccountingStandard.TT200)).thenReturn(List.of(inactiveRule));

        List<ValidationResult> results = ruleEngine.executeValidation(stmt);

        assertThat(results).isEmpty();
        verify(mockValidator, never()).validate(any(), any());
    }
}

