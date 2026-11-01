package com.financial.modules.financialindicator.service;

import com.financial.common.exception.IndicatorNotFoundException;
import com.financial.common.exception.MappingNotFoundException;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.common.exception.UnsupportedAccountingStandardException;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import com.financial.modules.financialindicator.enums.*;
import com.financial.modules.financialindicator.mapper.FinancialIndicatorMapper;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialindicator.repository.FinancialIndicatorRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import com.financial.modules.financialindicator.service.impl.FinancialIndicatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialIndicatorServiceTest {

    @Mock
    private FinancialIndicatorRepository indicatorRepository;

    @Mock
    private FinancialRatioDefinitionRepository ratioRepository;

    @Mock
    private FinancialRatioInputRepository ratioInputRepository;

    @Mock
    private AccountingIndicatorMappingRepository mappingRepository;

    @Spy
    private FinancialIndicatorMapper mapper = new FinancialIndicatorMapper();

    @InjectMocks
    private FinancialIndicatorServiceImpl indicatorService;

    private FinancialIndicator sampleIndicator;
    private FinancialRatioDefinition sampleRatio;
    private FinancialRatioInput sampleRatioInput;
    private AccountingIndicatorMapping sampleMapping;

    @BeforeEach
    void setUp() {
        sampleIndicator = FinancialIndicator.builder()
                .id(1L)
                .indicatorCode("CURRENT_ASSETS")
                .indicatorName("Tài sản ngắn hạn")
                .indicatorNameEn("Current Assets")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .dataType("DECIMAL")
                .unit(FinancialUnit.VND)
                .description("Tổng tài sản ngắn hạn")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRatio = FinancialRatioDefinition.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .ratioNameEn("Current Ratio")
                .category(FinancialRatioCategory.LIQUIDITY)
                .formulaExpression("CURRENT_ASSETS / CURRENT_LIABILITIES")
                .unit(FinancialUnit.TIMES)
                .evaluationDirection(EvaluationDirection.HIGHER_IS_BETTER)
                .decimalScale(2)
                .isCoreKpi(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRatioInput = FinancialRatioInput.builder()
                .id(1L)
                .ratioDefinition(sampleRatio)
                .indicator(sampleIndicator)
                .inputAlias("CURRENT_ASSETS")
                .inputOrder(1)
                .required(true)
                .periodReference(PeriodReference.CURRENT)
                .build();

        sampleMapping = AccountingIndicatorMapping.builder()
                .id(1L)
                .indicator(sampleIndicator)
                .accountingStandard(AccountingStandard.TT200)
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .sourceCode("100")
                .sourceKey("CURRENT_ASSETS")
                .sourceLabel("Tài sản ngắn hạn")
                .mappingStatus(MappingStatus.ACTIVE)
                .build();
    }

    // --- Financial Indicator Tests ---

    @Test
    @DisplayName("Should retrieve paginated indicators successfully")
    void shouldRetrievePaginatedIndicators() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialIndicator> page = new PageImpl<>(List.of(sampleIndicator), pageable, 1);
        when(indicatorRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<FinancialIndicatorResponse> result = indicatorService.getIndicators(FinancialStatementType.BALANCE_SHEET, true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should retrieve indicator by ID")
    void shouldRetrieveIndicatorById() {
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(sampleIndicator));

        FinancialIndicatorResponse response = indicatorService.getIndicatorById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should throw IndicatorNotFoundException when indicator ID not found")
    void shouldThrowWhenIndicatorIdNotFound() {
        when(indicatorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> indicatorService.getIndicatorById(999L))
                .isInstanceOf(IndicatorNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should retrieve indicator by Code")
    void shouldRetrieveIndicatorByCode() {
        when(indicatorRepository.findByIndicatorCode("CURRENT_ASSETS")).thenReturn(Optional.of(sampleIndicator));

        FinancialIndicatorResponse response = indicatorService.getIndicatorByCode("CURRENT_ASSETS");

        assertThat(response).isNotNull();
        assertThat(response.getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should throw when indicator code is blank or not found")
    void shouldThrowWhenIndicatorCodeNotFoundOrBlank() {
        assertThatThrownBy(() -> indicatorService.getIndicatorByCode(""))
                .isInstanceOf(IllegalArgumentException.class);

        when(indicatorRepository.findByIndicatorCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> indicatorService.getIndicatorByCode("UNKNOWN"))
                .isInstanceOf(IndicatorNotFoundException.class);
    }

    @Test
    @DisplayName("Should retrieve indicators by accounting standard")
    void shouldRetrieveIndicatorsByStandard() {
        when(indicatorRepository.findDistinctByAccountingStandard(AccountingStandard.TT200))
                .thenReturn(List.of(sampleIndicator));

        List<FinancialIndicatorResponse> result = indicatorService.getIndicatorsByStandard("TT200");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should throw UnsupportedAccountingStandardException for unknown standard")
    void shouldThrowForUnsupportedStandard() {
        assertThatThrownBy(() -> indicatorService.getIndicatorsByStandard("INVALID_STANDARD"))
                .isInstanceOf(UnsupportedAccountingStandardException.class);
    }

    // --- Financial Ratio Tests ---

    @Test
    @DisplayName("Should retrieve paginated ratios")
    void shouldRetrievePaginatedRatios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FinancialRatioDefinition> page = new PageImpl<>(List.of(sampleRatio), pageable, 1);
        when(ratioRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<FinancialRatioResponse> result = indicatorService.getRatios(FinancialRatioCategory.LIQUIDITY, true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRatioCode()).isEqualTo("CURRENT_RATIO");
    }

    @Test
    @DisplayName("Should retrieve ratio by Code")
    void shouldRetrieveRatioByCode() {
        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));

        FinancialRatioResponse response = indicatorService.getRatioByCode("CURRENT_RATIO");

        assertThat(response).isNotNull();
        assertThat(response.getRatioCode()).isEqualTo("CURRENT_RATIO");
        assertThat(response.getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve ratios by category")
    void shouldRetrieveRatiosByCategory() {
        when(ratioRepository.findByCategoryAndIsActiveTrue(FinancialRatioCategory.LIQUIDITY))
                .thenReturn(List.of(sampleRatio));

        List<FinancialRatioResponse> result = indicatorService.getRatiosByCategory(FinancialRatioCategory.LIQUIDITY);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(FinancialRatioCategory.LIQUIDITY);
    }

    @Test
    @DisplayName("Should retrieve the four core KPIs")
    void shouldRetrieveCoreKpis() {
        when(ratioRepository.findByIsCoreKpiTrueAndIsActiveTrue()).thenReturn(List.of(sampleRatio));

        List<FinancialRatioResponse> kpis = indicatorService.getCoreKpis();

        assertThat(kpis).hasSize(1);
        assertThat(kpis.get(0).getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve input dependencies for a ratio")
    void shouldRetrieveRatioInputs() {
        when(ratioRepository.existsByRatioCode("CURRENT_RATIO")).thenReturn(true);
        when(ratioInputRepository.findByRatioDefinitionRatioCodeOrderByInputOrderAsc("CURRENT_RATIO"))
                .thenReturn(List.of(sampleRatioInput));

        List<FinancialRatioInputResponse> inputs = indicatorService.getRatioInputs("CURRENT_RATIO");

        assertThat(inputs).hasSize(1);
        assertThat(inputs.get(0).getInputAlias()).isEqualTo("CURRENT_ASSETS");
        assertThat(inputs.get(0).getPeriodReference()).isEqualTo(PeriodReference.CURRENT);
    }

    @Test
    @DisplayName("Should throw RatioNotFoundException when getting inputs for non-existent ratio")
    void shouldThrowWhenGettingInputsForNonExistentRatio() {
        when(ratioRepository.existsByRatioCode("UNKNOWN_RATIO")).thenReturn(false);

        assertThatThrownBy(() -> indicatorService.getRatioInputs("UNKNOWN_RATIO"))
                .isInstanceOf(RatioNotFoundException.class);
    }

    // --- Accounting Mapping Tests ---

    @Test
    @DisplayName("Should resolve mapping by sourceCode")
    void shouldResolveMappingBySourceCode() {
        when(mappingRepository.findFirstByAccountingStandardAndStatementTypeAndSourceCodeAndMappingStatus(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, "100", MappingStatus.ACTIVE
        )).thenReturn(Optional.of(sampleMapping));

        FinancialIndicatorResponse resolved = indicatorService.resolveMapping(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, "100", null
        );

        assertThat(resolved).isNotNull();
        assertThat(resolved.getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should resolve mapping by sourceKey when sourceCode not provided")
    void shouldResolveMappingBySourceKey() {
        when(mappingRepository.findFirstByAccountingStandardAndStatementTypeAndSourceKeyAndMappingStatus(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, "CURRENT_ASSETS", MappingStatus.ACTIVE
        )).thenReturn(Optional.of(sampleMapping));

        FinancialIndicatorResponse resolved = indicatorService.resolveMapping(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, null, "CURRENT_ASSETS"
        );

        assertThat(resolved).isNotNull();
        assertThat(resolved.getIndicatorCode()).isEqualTo("CURRENT_ASSETS");
    }

    @Test
    @DisplayName("Should throw MappingNotFoundException when mapping does not exist")
    void shouldThrowMappingNotFoundException() {
        when(mappingRepository.findFirstByAccountingStandardAndStatementTypeAndSourceCodeAndMappingStatus(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, "9999", MappingStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> indicatorService.resolveMapping(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, "9999", null
        )).isInstanceOf(MappingNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when both sourceCode and sourceKey are null")
    void shouldThrowWhenBothCodeAndKeyNull() {
        assertThatThrownBy(() -> indicatorService.resolveMapping(
                AccountingStandard.TT200, FinancialStatementType.BALANCE_SHEET, null, null
        )).isInstanceOf(IllegalArgumentException.class);
    }
}

