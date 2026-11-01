package com.financial.modules.calculationengine.service.impl;

import com.financial.common.exception.CalculationNotFoundException;
import com.financial.common.exception.InvalidStatementStatusException;
import com.financial.common.exception.StatementNotFoundException;
import com.financial.common.exception.UnconfirmedStatementException;
import com.financial.modules.calculationengine.dto.CalculationHistoryResponse;
import com.financial.modules.calculationengine.dto.CalculationRequest;
import com.financial.modules.calculationengine.dto.CalculationResponse;
import com.financial.modules.calculationengine.dto.CalculationResultResponse;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.mapper.CalculationMapper;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.calculationengine.repository.FinancialCalculationResultRepository;
import com.financial.modules.calculationengine.service.CalculationService;
import com.financial.modules.calculationengine.service.FormulaExecutionService;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository statementRepository;
    private final FinancialStatementItemRepository itemRepository;
    private final FinancialRatioDefinitionRepository ratioDefinitionRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final FinancialCalculationResultRepository calculationResultRepository;
    private final FormulaExecutionService formulaExecutionService;
    private final CalculationMapper calculationMapper;

    @Override
    @Transactional
    public CalculationResponse calculateForStatement(Long userId, Long statementId, CalculationRequest request) {
        log.info("Initiating calculation for statement ID {} requested by user ID {}", statementId, userId);
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        // Rule: Only calculate from confirmed financial statement data
        if (statement.getProcessingStatus() != FinancialStatementStatus.DATA_CONFIRMED) {
            log.warn("Calculation rejected: statement ID {} has status {}", statementId, statement.getProcessingStatus());
            throw new UnconfirmedStatementException(statementId,
                    statement.getProcessingStatus() != null ? statement.getProcessingStatus().name() : "NULL");
        }

        List<FinancialStatementItem> items = itemRepository.findByFinancialStatementId(statementId);
        if (items.isEmpty()) {
            throw new InvalidStatementStatusException("Financial statement does not contain any indicator items to calculate.");
        }

        // Determine next calculation version
        int nextVersion = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)
                .map(prev -> prev.getCalculationVersion() + 1)
                .orElse(1);

        String confirmedVersion = statement.getUpdatedAt() != null
                ? statement.getUpdatedAt().toString()
                : statement.getCreatedAt().toString();

        FinancialCalculation calculation = FinancialCalculation.builder()
                .financialStatement(statement)
                .calculationVersion(nextVersion)
                .confirmedDataVersion(confirmedVersion)
                .calculatedAt(LocalDateTime.now())
                .build();

        // Retrieve active ratio definitions
        List<FinancialRatioDefinition> ratioDefinitions = ratioDefinitionRepository.findByIsActiveTrue();
        if (ratioDefinitions.isEmpty()) {
            log.warn("No active ratio definitions found in database during calculation");
        }

        Integer scaleOverride = request != null ? request.getDecimalScale() : null;

        for (FinancialRatioDefinition ratioDef : ratioDefinitions) {
            FinancialCalculationResult result = formulaExecutionService.executeRatioCalculation(statement, ratioDef, scaleOverride);
            calculation.addResult(result);
        }

        // Determine overall calculation status
        long successCount = calculation.getResults().stream()
                .filter(r -> r.getStatus() == CalculationStatus.SUCCESS)
                .count();

        if (successCount == calculation.getResults().size() && !calculation.getResults().isEmpty()) {
            calculation.setStatus(CalculationStatus.SUCCESS);
        } else if (successCount > 0) {
            calculation.setStatus(CalculationStatus.PARTIAL);
        } else {
            calculation.setStatus(CalculationStatus.FAILED);
        }

        FinancialCalculation saved = calculationRepository.save(calculation);
        log.info("Persisted calculation ID {} (version {}) with {} ratio results for statement ID {}",
                saved.getId(), saved.getCalculationVersion(), saved.getResults().size(), statementId);

        return calculationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CalculationResponse getLatestCalculation(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);

        FinancialCalculation calculation = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)
                .orElseThrow(() -> new CalculationNotFoundException("No calculations found for financial statement ID: " + statementId));

        return calculationMapper.toResponse(calculation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalculationHistoryResponse> getCalculationHistory(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);

        List<FinancialCalculation> history = calculationRepository.findByFinancialStatementIdOrderByCalculationVersionDesc(statementId);
        return history.stream()
                .map(calculationMapper::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalculationResponse getCalculationById(Long userId, Long statementId, Long calculationId) {
        getOwnedStatement(userId, statementId);

        FinancialCalculation calculation = calculationRepository.findByIdAndFinancialStatementId(calculationId, statementId)
                .orElseThrow(() -> new CalculationNotFoundException("Calculation ID " + calculationId + " not found for financial statement ID: " + statementId));

        return calculationMapper.toResponse(calculation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalculationResultResponse> getLatestCoreKpis(Long userId, Long statementId) {
        getOwnedStatement(userId, statementId);

        FinancialCalculation calculation = calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)
                .orElseThrow(() -> new CalculationNotFoundException("No calculations found for financial statement ID: " + statementId));

        return calculation.getResults().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCoreKpi()))
                .map(calculationMapper::toResultResponse)
                .toList();
    }

    private FinancialStatement getOwnedStatement(Long userId, Long statementId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new StatementNotFoundException("User does not have an associated company"));

        return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId));
    }
}
