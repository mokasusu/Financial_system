package com.financial.modules.calculationengine.service;

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
import com.financial.modules.calculationengine.service.impl.CalculationServiceImpl;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
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
class CalculationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private FinancialStatementRepository statementRepository;

    @Mock
    private FinancialStatementItemRepository itemRepository;

    @Mock
    private FinancialRatioDefinitionRepository ratioDefinitionRepository;

    @Mock
    private FinancialCalculationRepository calculationRepository;

    @Mock
    private FinancialCalculationResultRepository calculationResultRepository;

    @Mock
    private FormulaExecutionService formulaExecutionService;

    @Spy
    private CalculationMapper calculationMapper = new CalculationMapper();

    @InjectMocks
    private CalculationServiceImpl calculationService;

    private Long userId = 1L;
    private Long companyId = 10L;
    private Long statementId = 100L;

    private Company company;
    private FinancialStatement confirmedStatement;
    private FinancialStatement unconfirmedStatement;
    private List<FinancialStatementItem> statementItems;
    private List<FinancialRatioDefinition> ratioDefinitions;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(companyId).build();

        confirmedStatement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        unconfirmedStatement = FinancialStatement.builder()
                .id(statementId)
                .company(company)
                .processingStatus(FinancialStatementStatus.REVIEWING_DATA)
                .build();

        statementItems = List.of(
                FinancialStatementItem.builder().id(1L).code("100").currentValue(new BigDecimal("50000000000")).build()
        );

        FinancialRatioDefinition crRatio = FinancialRatioDefinition.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .isCoreKpi(true)
                .isActive(true)
                .build();

        FinancialRatioDefinition roaRatio = FinancialRatioDefinition.builder()
                .id(2L)
                .ratioCode("ROA")
                .ratioName("Tỷ suất sinh lời trên tổng tài sản")
                .category(FinancialRatioCategory.PROFITABILITY)
                .unit(FinancialUnit.PERCENT)
                .isCoreKpi(true)
                .isActive(true)
                .build();

        ratioDefinitions = List.of(crRatio, roaRatio);
    }

    @Test
    @DisplayName("Calculate for confirmed statement creates version 1 and persists results")
    void testCalculateForConfirmedStatement_Version1() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(itemRepository.findByFinancialStatementId(statementId)).thenReturn(statementItems);
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.empty());
        when(ratioDefinitionRepository.findByIsActiveTrue()).thenReturn(ratioDefinitions);

        when(formulaExecutionService.executeRatioCalculation(eq(confirmedStatement), any(FinancialRatioDefinition.class), any()))
                .thenAnswer(inv -> {
                    FinancialRatioDefinition def = inv.getArgument(1);
                    return FinancialCalculationResult.builder()
                            .ratioDefinition(def)
                            .ratioCode(def.getRatioCode())
                            .ratioName(def.getRatioName())
                            .category(def.getCategory())
                            .unit(def.getUnit())
                            .isCoreKpi(def.getIsCoreKpi())
                            .status(CalculationStatus.SUCCESS)
                            .resultValue(new BigDecimal("2.0000"))
                            .build();
                });

        when(calculationRepository.save(any(FinancialCalculation.class))).thenAnswer(inv -> {
            FinancialCalculation c = inv.getArgument(0);
            c.setId(501L);
            return c;
        });

        CalculationResponse response = calculationService.calculateForStatement(userId, statementId, null);

        assertThat(response).isNotNull();
        assertThat(response.getCalculationVersion()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(response.getResults()).hasSize(2);
        verify(calculationRepository, times(1)).save(any(FinancialCalculation.class));
    }

    @Test
    @DisplayName("Subsequent calculation increments version to 2 (historical calculations preserved)")
    void testCalculate_IncrementsVersion() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(itemRepository.findByFinancialStatementId(statementId)).thenReturn(statementItems);

        FinancialCalculation existingV1 = FinancialCalculation.builder()
                .id(501L)
                .calculationVersion(1)
                .build();
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.of(existingV1));
        when(ratioDefinitionRepository.findByIsActiveTrue()).thenReturn(ratioDefinitions);

        when(formulaExecutionService.executeRatioCalculation(any(), any(), any()))
                .thenReturn(FinancialCalculationResult.builder()
                        .ratioCode("CURRENT_RATIO")
                        .ratioName("Current Ratio")
                        .category(FinancialRatioCategory.LIQUIDITY)
                        .unit(FinancialUnit.TIMES)
                        .isCoreKpi(true)
                        .status(CalculationStatus.SUCCESS)
                        .resultValue(new BigDecimal("2.0000"))
                        .build());

        when(calculationRepository.save(any(FinancialCalculation.class))).thenAnswer(inv -> {
            FinancialCalculation c = inv.getArgument(0);
            c.setId(502L);
            return c;
        });

        CalculationResponse response = calculationService.calculateForStatement(userId, statementId, null);

        assertThat(response.getCalculationVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("Calculation on unconfirmed statement throws UnconfirmedStatementException")
    void testCalculate_UnconfirmedStatement_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(unconfirmedStatement));

        assertThatThrownBy(() -> calculationService.calculateForStatement(userId, statementId, null))
                .isInstanceOf(UnconfirmedStatementException.class)
                .hasMessageContaining("cannot be calculated because it is not confirmed");

        verify(calculationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Calculation on non-existent or unowned statement throws StatementNotFoundException")
    void testCalculate_StatementNotFound_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calculationService.calculateForStatement(userId, statementId, null))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("Calculation on confirmed statement with empty items throws InvalidStatementStatusException")
    void testCalculate_EmptyItems_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(itemRepository.findByFinancialStatementId(statementId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> calculationService.calculateForStatement(userId, statementId, null))
                .isInstanceOf(InvalidStatementStatusException.class)
                .hasMessageContaining("does not contain any indicator items");
    }

    @Test
    @DisplayName("getLatestCalculation returns latest calculation record")
    void testGetLatestCalculation() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));

        FinancialCalculation calc = FinancialCalculation.builder()
                .id(501L)
                .financialStatement(confirmedStatement)
                .calculationVersion(2)
                .status(CalculationStatus.SUCCESS)
                .calculatedAt(LocalDateTime.now())
                .results(new ArrayList<>())
                .build();

        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId))
                .thenReturn(Optional.of(calc));

        CalculationResponse response = calculationService.getLatestCalculation(userId, statementId);

        assertThat(response).isNotNull();
        assertThat(response.getCalculationVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("getLatestCalculation when no calculations exist throws CalculationNotFoundException")
    void testGetLatestCalculation_NotFound() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));
        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calculationService.getLatestCalculation(userId, statementId))
                .isInstanceOf(CalculationNotFoundException.class);
    }

    @Test
    @DisplayName("getLatestCoreKpis filters and returns only core KPIs")
    void testGetLatestCoreKpis() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));

        FinancialCalculation calc = FinancialCalculation.builder()
                .id(501L)
                .financialStatement(confirmedStatement)
                .calculationVersion(1)
                .status(CalculationStatus.SUCCESS)
                .calculatedAt(LocalDateTime.now())
                .build();

        FinancialCalculationResult kpi1 = FinancialCalculationResult.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Current Ratio")
                .unit(FinancialUnit.TIMES)
                .isCoreKpi(true)
                .status(CalculationStatus.SUCCESS)
                .resultValue(new BigDecimal("2.0000"))
                .build();

        FinancialCalculationResult nonKpi = FinancialCalculationResult.builder()
                .id(2L)
                .ratioCode("QUICK_RATIO")
                .ratioName("Quick Ratio")
                .unit(FinancialUnit.TIMES)
                .isCoreKpi(false)
                .status(CalculationStatus.SUCCESS)
                .resultValue(new BigDecimal("1.4000"))
                .build();

        calc.addResult(kpi1);
        calc.addResult(nonKpi);

        when(calculationRepository.findTopByFinancialStatementIdOrderByCalculationVersionDesc(statementId))
                .thenReturn(Optional.of(calc));

        List<CalculationResultResponse> kpis = calculationService.getLatestCoreKpis(userId, statementId);

        assertThat(kpis).hasSize(1);
        assertThat(kpis.get(0).getRatioCode()).isEqualTo("CURRENT_RATIO");
        assertThat(kpis.get(0).getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("getCalculationHistory returns historical summaries ordered desc")
    void testGetCalculationHistory() {
        when(companyRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(company));
        when(statementRepository.findByIdAndCompanyId(statementId, companyId)).thenReturn(Optional.of(confirmedStatement));

        FinancialCalculation c1 = FinancialCalculation.builder().id(1L).calculationVersion(1).status(CalculationStatus.SUCCESS).results(new ArrayList<>()).build();
        FinancialCalculation c2 = FinancialCalculation.builder().id(2L).calculationVersion(2).status(CalculationStatus.SUCCESS).results(new ArrayList<>()).build();

        when(calculationRepository.findByFinancialStatementIdOrderByCalculationVersionDesc(statementId))
                .thenReturn(List.of(c2, c1));

        List<CalculationHistoryResponse> history = calculationService.getCalculationHistory(userId, statementId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getCalculationVersion()).isEqualTo(2);
        assertThat(history.get(1).getCalculationVersion()).isEqualTo(1);
    }
}
