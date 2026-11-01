package com.financial.modules.validation.service;

import com.financial.common.exception.*;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.validation.dto.*;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.mapper.ValidationMapper;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import com.financial.modules.validation.repository.ValidationResultRepository;
import com.financial.modules.validation.repository.ValidationRuleRepository;
import com.financial.modules.validation.service.impl.ValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FinancialStatementRepository statementRepository;

    @Mock
    private FinancialStatementItemRepository itemRepository;

    @Mock
    private ValidationRuleRepository ruleRepository;

    @Mock
    private ValidationExecutionRepository executionRepository;

    @Mock
    private ValidationResultRepository resultRepository;

    @Mock
    private ValidationRuleEngine ruleEngine;

    @Spy
    private ValidationMapper mapper = new ValidationMapper();

    @InjectMocks
    private ValidationServiceImpl validationService;

    private Long userId = 1L;
    private Long companyId = 10L;
    private Long statementId = 100L;

    private Company company;
    private FinancialStatement confirmedStatement;
    private FinancialStatement unconfirmedStatement;
    private List<FinancialStatementItem> statementItems;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(companyId).build();

        confirmedStatement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        unconfirmedStatement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.REVIEWING_DATA)
                .build();

        statementItems = List.of(
                FinancialStatementItem.builder().id(1L).code("100").currentValue(new BigDecimal("50000000000")).build()
        );
    }

    @Test
    @DisplayName("1. Validation executes on confirmed statement data and saves execution record")
    void testValidateStatement_Confirmed_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(itemRepository.findByFinancialStatementId(statementId)).thenReturn(statementItems);
        when(ruleEngine.resolveAccountingStandard(confirmedStatement)).thenReturn(AccountingStandard.TT200);

        ValidationResult r1 = ValidationResult.builder()
                .ruleCodeSnapshot("BS_EQ_01")
                .ruleNameSnapshot("Equation Rule")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .status(ValidationStatus.PASS)
                .build();
        when(ruleEngine.executeValidation(confirmedStatement)).thenReturn(List.of(r1));

        when(executionRepository.save(any(ValidationExecution.class))).thenAnswer(inv -> {
            ValidationExecution exec = inv.getArgument(0);
            exec.setId(501L);
            return exec;
        });

        ValidationResponse response = validationService.validateStatement(userId, statementId, null);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
        assertThat(response.getExecutionStatus()).isEqualTo(ValidationStatus.PASS);
        assertThat(response.getTotalRules()).isEqualTo(1);
        assertThat(response.getPassedRules()).isEqualTo(1);

        verify(executionRepository).save(any(ValidationExecution.class));
        // Verify statement itself is not altered
        verify(statementRepository, never()).save(any());
    }

    @Test
    @DisplayName("2. Unconfirmed statement throws UnconfirmedStatementException (draft data rejected)")
    void testValidateStatement_Unconfirmed_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(unconfirmedStatement));

        assertThatThrownBy(() -> validationService.validateStatement(userId, statementId, null))
                .isInstanceOf(UnconfirmedStatementException.class)
                .hasMessageContaining("cannot be calculated because it is not confirmed");

        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. User cannot validate another user's financial statement")
    void testValidateStatement_Unowned_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationService.validateStatement(userId, statementId, null))
                .isInstanceOf(StatementNotFoundException.class);

        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. Statement with empty items throws InvalidStatementStatusException")
    void testValidateStatement_EmptyItems_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(itemRepository.findByFinancialStatementId(statementId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> validationService.validateStatement(userId, statementId, null))
                .isInstanceOf(InvalidStatementStatusException.class)
                .hasMessageContaining("does not contain any indicator items");
    }

    @Test
    @DisplayName("5. getLatestValidation returns latest execution record")
    void testGetLatestValidation() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));

        ValidationExecution exec = ValidationExecution.builder()
                .id(501L)
                .financialStatement(confirmedStatement)
                .accountingStandard(AccountingStandard.TT200)
                .executionStatus(ValidationStatus.PASS)
                .executedAt(LocalDateTime.now())
                .results(new ArrayList<>())
                .build();
        when(executionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(Optional.of(exec));

        ValidationResponse response = validationService.getLatestValidation(userId, statementId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
    }

    @Test
    @DisplayName("6. getValidationHistory returns historical executions ordered descending")
    void testGetValidationHistory() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));

        ValidationExecution e1 = ValidationExecution.builder().id(1L).executionStatus(ValidationStatus.PASS).executedAt(LocalDateTime.now().minusDays(1)).build();
        ValidationExecution e2 = ValidationExecution.builder().id(2L).executionStatus(ValidationStatus.WARNING).executedAt(LocalDateTime.now()).build();

        when(executionRepository.findByFinancialStatementIdOrderByExecutedAtDesc(statementId)).thenReturn(List.of(e2, e1));

        List<ValidationHistoryResponse> history = validationService.getValidationHistory(userId, statementId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getId()).isEqualTo(2L);
        assertThat(history.get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("7. Duplicate ruleCode on rule creation throws DuplicateValidationRuleException")
    void testCreateValidationRule_DuplicateCode_ThrowsException() {
        ValidationRuleRequest req = ValidationRuleRequest.builder()
                .ruleCode("BS_EQ_01")
                .ruleName("Duplicate Code Rule")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .build();

        when(ruleRepository.existsByRuleCode("BS_EQ_01")).thenReturn(true);

        assertThatThrownBy(() -> validationService.createValidationRule(req))
                .isInstanceOf(DuplicateValidationRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("8. updateValidationRuleStatus toggles active state")
    void testUpdateValidationRuleStatus() {
        ValidationRule rule = ValidationRule.builder()
                .id(10L)
                .ruleCode("BS_EQ_01")
                .isActive(true)
                .build();

        when(ruleRepository.findById(10L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(rule)).thenReturn(rule);

        ValidationRuleResponse response = validationService.updateValidationRuleStatus(10L, false);

        assertThat(response.getIsActive()).isFalse();
        verify(ruleRepository).save(rule);
    }
}

