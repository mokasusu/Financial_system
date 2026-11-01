package com.financial.modules.validation.service.impl;

import com.financial.common.exception.*;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.validation.dto.*;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.mapper.ValidationMapper;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import com.financial.modules.validation.repository.ValidationResultRepository;
import com.financial.modules.validation.repository.ValidationRuleRepository;
import com.financial.modules.validation.service.ValidationRuleEngine;
import com.financial.modules.validation.service.ValidationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository statementRepository;
    private final FinancialStatementItemRepository itemRepository;
    private final ValidationRuleRepository ruleRepository;
    private final ValidationExecutionRepository executionRepository;
    private final ValidationResultRepository resultRepository;
    private final ValidationRuleEngine ruleEngine;
    private final ValidationMapper mapper;

    @Override
    @Transactional
    public ValidationResponse validateStatement(Long userId, Long statementId, ValidationRequest request) {
        log.info("Initiating financial statement validation for statement ID {} by user ID {}", statementId, userId);
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        // Rule: Only validate confirmed financial statement data
        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            log.warn("Validation rejected: statement ID {} is in status {}", statementId, statement.getProcessingStatus());
            throw new UnconfirmedStatementException(statementId,
                    statement.getProcessingStatus() != null ? statement.getProcessingStatus().name() : "NULL");
        }

        List<FinancialStatementItem> items = itemRepository.findByFinancialStatementId(statementId);
        if (items.isEmpty()) {
            throw new InvalidStatementStatusException("Financial statement does not contain any indicator items to validate.");
        }

        // Attach loaded items to statement instance without breaking Hibernate's persistent collection reference
        if (statement.getItems() == null) {
            statement.setItems(new ArrayList<>());
        } else {
            statement.getItems().clear();
        }
        statement.getItems().addAll(items);

        // Execute rule validation engine
        List<ValidationResult> results = ruleEngine.executeValidation(statement);

        int total = results.size();
        int passed = 0;
        int warning = 0;
        int failed = 0;
        int reviewRequired = 0;
        int error = 0;
        int notApplicable = 0;

        for (ValidationResult r : results) {
            if (r.getStatus() == ValidationStatus.PASS) passed++;
            else if (r.getStatus() == ValidationStatus.WARNING) warning++;
            else if (r.getStatus() == ValidationStatus.FAIL) failed++;
            else if (r.getStatus() == ValidationStatus.REVIEW_REQUIRED) reviewRequired++;
            else if (r.getStatus() == ValidationStatus.ERROR) error++;
            else if (r.getStatus() == ValidationStatus.NOT_APPLICABLE) notApplicable++;
        }

        ValidationStatus overallStatus;
        if (failed > 0) {
            overallStatus = ValidationStatus.FAIL;
        } else if (error > 0) {
            overallStatus = ValidationStatus.ERROR;
        } else if (reviewRequired > 0) {
            overallStatus = ValidationStatus.REVIEW_REQUIRED;
        } else if (warning > 0) {
            overallStatus = ValidationStatus.WARNING;
        } else if (passed > 0) {
            overallStatus = ValidationStatus.PASS;
        } else {
            overallStatus = ValidationStatus.NOT_APPLICABLE;
        }

        String confirmedVersion = statement.getUpdatedAt() != null
                ? statement.getUpdatedAt().toString()
                : statement.getCreatedAt().toString();

        ValidationExecution execution = ValidationExecution.builder()
                .financialStatement(statement)
                .confirmedDataVersion(confirmedVersion)
                .accountingStandard(ruleEngine.resolveAccountingStandard(statement))
                .executionStatus(overallStatus)
                .totalRules(total)
                .passedRules(passed)
                .warningRules(warning)
                .failedRules(failed)
                .reviewRequiredRules(reviewRequired)
                .errorRules(error)
                .notApplicableRules(notApplicable)
                .executedAt(LocalDateTime.now())
                .build();

        for (ValidationResult r : results) {
            execution.addResult(r);
        }

        ValidationExecution saved = executionRepository.save(execution);
        log.info("Persisted validation execution ID {} with {} results for statement ID {}. Status: {}",
                saved.getId(), saved.getResults().size(), statementId, saved.getExecutionStatus());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse getLatestValidation(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);

        ValidationExecution execution = executionRepository.findTopByFinancialStatementIdOrderByExecutedAtDesc(statementId)
                .orElseThrow(() -> new ValidationNotFoundException("No validation executions found for financial statement ID: " + statementId));

        return mapper.toResponse(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidationHistoryResponse> getValidationHistory(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);

        List<ValidationExecution> history = executionRepository.findByFinancialStatementIdOrderByExecutedAtDesc(statementId);
        return history.stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse getValidationById(Long userId, Long statementId, Long executionId) {
        getOwnedStatement(userId, statementId);

        ValidationExecution execution = executionRepository.findByIdAndFinancialStatementId(executionId, statementId)
                .orElseThrow(() -> new ValidationNotFoundException("Validation execution ID " + executionId + " not found for financial statement ID: " + statementId));

        return mapper.toResponse(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidationResultResponse> getValidationResults(Long userId, Long statementId, Long executionId) {
        getOwnedStatement(userId, statementId);

        ValidationExecution execution = executionRepository.findByIdAndFinancialStatementId(executionId, statementId)
                .orElseThrow(() -> new ValidationNotFoundException("Validation execution ID " + executionId + " not found for financial statement ID: " + statementId));

        return execution.getResults().stream()
                .map(mapper::toResultResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ValidationRuleResponse> getValidationRules(
            AccountingStandard standard,
            FinancialStatementType statementType,
            ValidationCategory category,
            ValidationSeverity severity,
            Boolean isActive,
            Pageable pageable
    ) {
        Specification<ValidationRule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (standard != null) predicates.add(cb.equal(root.get("accountingStandard"), standard));
            if (statementType != null) predicates.add(cb.equal(root.get("statementType"), statementType));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (severity != null) predicates.add(cb.equal(root.get("severity"), severity));
            if (isActive != null) predicates.add(cb.equal(root.get("isActive"), isActive));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ruleRepository.findAll(spec, pageable).map(mapper::toRuleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationRuleResponse getValidationRuleById(Long id) {
        ValidationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ValidationRuleNotFoundException(id));
        return mapper.toRuleResponse(rule);
    }

    @Override
    @Transactional
    public ValidationRuleResponse createValidationRule(ValidationRuleRequest request) {
        if (ruleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new DuplicateValidationRuleException("Validation rule with code '" + request.getRuleCode() + "' already exists");
        }

        ValidationRule rule = mapper.toRuleEntity(request);
        ValidationRule saved = ruleRepository.save(rule);
        log.info("Created new validation rule ID {} with code '{}'", saved.getId(), saved.getRuleCode());
        return mapper.toRuleResponse(saved);
    }

    @Override
    @Transactional
    public ValidationRuleResponse updateValidationRule(Long id, ValidationRuleRequest request) {
        ValidationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ValidationRuleNotFoundException(id));

        if (ruleRepository.existsByRuleCodeAndIdNot(request.getRuleCode(), id)) {
            throw new DuplicateValidationRuleException("Validation rule with code '" + request.getRuleCode() + "' already exists");
        }

        mapper.updateRuleEntity(rule, request);
        ValidationRule saved = ruleRepository.save(rule);
        log.info("Updated validation rule ID {} ('{}')", saved.getId(), saved.getRuleCode());
        return mapper.toRuleResponse(saved);
    }

    @Override
    @Transactional
    public ValidationRuleResponse updateValidationRuleStatus(Long id, Boolean isActive) {
        ValidationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ValidationRuleNotFoundException(id));

        rule.setIsActive(isActive);
        ValidationRule saved = ruleRepository.save(rule);
        log.info("Updated validation rule ID {} active status to {}", saved.getId(), isActive);
        return mapper.toRuleResponse(saved);
    }

    private FinancialStatement getOwnedStatement(Long userId, Long statementId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new StatementNotFoundException("User does not have an associated company"));

        return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId));
    }
}

