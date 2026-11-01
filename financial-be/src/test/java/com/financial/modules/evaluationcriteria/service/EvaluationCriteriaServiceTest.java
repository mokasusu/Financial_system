package com.financial.modules.evaluationcriteria.service;

import com.financial.common.exception.CriteriaNotFoundException;
import com.financial.common.exception.DuplicateCriteriaException;
import com.financial.common.exception.InvalidCriteriaException;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaUpdateRequest;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.mapper.EvaluationCriteriaMapper;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.evaluationcriteria.service.impl.EvaluationCriteriaServiceImpl;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.EvaluationDirection;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationCriteriaServiceTest {

    @Mock
    private EvaluationCriteriaRepository criteriaRepository;

    @Mock
    private FinancialRatioDefinitionRepository ratioRepository;

    @Spy
    private EvaluationCriteriaMapper mapper = new EvaluationCriteriaMapper();

    @InjectMocks
    private EvaluationCriteriaServiceImpl criteriaService;

    private FinancialRatioDefinition sampleRatio;
    private EvaluationCriteria sampleCriteria;

    @BeforeEach
    void setUp() {
        sampleRatio = FinancialRatioDefinition.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .evaluationDirection(EvaluationDirection.HIGHER_IS_BETTER)
                .isActive(true)
                .build();

        sampleCriteria = EvaluationCriteria.builder()
                .id(1L)
                .ratioDefinition(sampleRatio)
                .criterionName("Current Ratio Standard Threshold")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5"))
                .unit(FinancialUnit.TIMES)
                .description("Current assets should be at least 1.5x liabilities")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve paginated criteria successfully")
    void shouldRetrievePaginatedCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EvaluationCriteria> page = new PageImpl<>(List.of(sampleCriteria), pageable, 1);
        when(criteriaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<EvaluationCriteriaResponse> result = criteriaService.getCriteria("CURRENT_RATIO", true, ComparisonOperator.GREATER_THAN_OR_EQUAL, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRatioCode()).isEqualTo("CURRENT_RATIO");
        assertThat(result.getContent().get(0).getThresholdValue()).isEqualByComparingTo("1.5");
    }

    @Test
    @DisplayName("Should retrieve criterion by ID")
    void shouldRetrieveCriterionById() {
        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));

        EvaluationCriteriaResponse response = criteriaService.getCriteriaById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRatioCode()).isEqualTo("CURRENT_RATIO");
    }

    @Test
    @DisplayName("Should throw CriteriaNotFoundException when criterion ID does not exist")
    void shouldThrowWhenCriterionIdNotFound() {
        when(criteriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> criteriaService.getCriteriaById(999L))
                .isInstanceOf(CriteriaNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should retrieve criteria by ratio code")
    void shouldRetrieveCriteriaByRatioCode() {
        when(ratioRepository.existsByRatioCode("CURRENT_RATIO")).thenReturn(true);
        when(criteriaRepository.findByRatioDefinitionRatioCode("CURRENT_RATIO")).thenReturn(List.of(sampleCriteria));

        List<EvaluationCriteriaResponse> result = criteriaService.getCriteriaByRatioCode("CURRENT_RATIO");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRatioCode()).isEqualTo("CURRENT_RATIO");
    }

    @Test
    @DisplayName("Should throw RatioNotFoundException when getting criteria for non-existent ratio code")
    void shouldThrowWhenRatioCodeNotFound() {
        when(ratioRepository.existsByRatioCode("UNKNOWN")).thenReturn(false);

        assertThatThrownBy(() -> criteriaService.getCriteriaByRatioCode("UNKNOWN"))
                .isInstanceOf(RatioNotFoundException.class);
    }

    @Test
    @DisplayName("Should create valid criterion with single threshold value")
    void shouldCreateValidCriterion() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Current Ratio Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrue(1L)).thenReturn(false);
        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> {
            EvaluationCriteria entity = i.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        EvaluationCriteriaResponse response = criteriaService.createCriteria(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRatioCode()).isEqualTo("CURRENT_RATIO");
        assertThat(response.getThresholdValue()).isEqualByComparingTo("1.5");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should create valid criterion with BETWEEN operator and range")
    void shouldCreateCriterionWithBetweenRange() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Optimal Current Ratio Range")
                .comparisonOperator(ComparisonOperator.BETWEEN)
                .minimumValue(new BigDecimal("1.5"))
                .maximumValue(new BigDecimal("3.0"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrue(1L)).thenReturn(false);
        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> {
            EvaluationCriteria entity = i.getArgument(0);
            entity.setId(11L);
            return entity;
        });

        EvaluationCriteriaResponse response = criteriaService.createCriteria(request);

        assertThat(response).isNotNull();
        assertThat(response.getComparisonOperator()).isEqualTo(ComparisonOperator.BETWEEN);
        assertThat(response.getMinimumValue()).isEqualByComparingTo("1.5");
        assertThat(response.getMaximumValue()).isEqualByComparingTo("3.0");
        assertThat(response.getThresholdValue()).isNull();
    }

    @Test
    @DisplayName("Should reject BETWEEN operator when minimumValue is greater than maximumValue")
    void shouldRejectBetweenWhenMinGreaterThanMax() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Invalid Range")
                .comparisonOperator(ComparisonOperator.BETWEEN)
                .minimumValue(new BigDecimal("3.0"))
                .maximumValue(new BigDecimal("1.5"))
                .unit(FinancialUnit.TIMES)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));

        assertThatThrownBy(() -> criteriaService.createCriteria(request))
                .isInstanceOf(InvalidCriteriaException.class)
                .hasMessageContaining("Minimum value must be less than or equal to maximum value");
    }

    @Test
    @DisplayName("Should reject BETWEEN operator when minimumValue or maximumValue is missing")
    void shouldRejectBetweenWhenMissingBoundaries() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Missing Boundary")
                .comparisonOperator(ComparisonOperator.BETWEEN)
                .minimumValue(new BigDecimal("1.5"))
                .maximumValue(null)
                .unit(FinancialUnit.TIMES)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));

        assertThatThrownBy(() -> criteriaService.createCriteria(request))
                .isInstanceOf(InvalidCriteriaException.class)
                .hasMessageContaining("BETWEEN operator requires both minimumValue and maximumValue");
    }

    @Test
    @DisplayName("Should reject negative threshold values")
    void shouldRejectNegativeThresholdValues() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Negative Threshold")
                .comparisonOperator(ComparisonOperator.GREATER_THAN)
                .thresholdValue(new BigDecimal("-1.0"))
                .unit(FinancialUnit.TIMES)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));

        assertThatThrownBy(() -> criteriaService.createCriteria(request))
                .isInstanceOf(InvalidCriteriaException.class)
                .hasMessageContaining("Threshold value cannot be negative");
    }

    @Test
    @DisplayName("Should reject duplicate active criteria for the same ratio")
    void shouldRejectDuplicateActiveCriteriaOnCreate() {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Duplicate Current Ratio")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("2.0"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(ratioRepository.findByRatioCode("CURRENT_RATIO")).thenReturn(Optional.of(sampleRatio));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> criteriaService.createCriteria(request))
                .isInstanceOf(DuplicateCriteriaException.class)
                .hasMessageContaining("CURRENT_RATIO");
    }

    @Test
    @DisplayName("Should update criterion successfully")
    void shouldUpdateCriterionSuccessfully() {
        EvaluationCriteriaUpdateRequest updateRequest = EvaluationCriteriaUpdateRequest.builder()
                .criterionName("Updated Threshold")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.8"))
                .unit(FinancialUnit.TIMES)
                .description("Updated description")
                .isActive(true)
                .build();

        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(1L, 1L)).thenReturn(false);
        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> i.getArgument(0));

        EvaluationCriteriaResponse response = criteriaService.updateCriteria(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCriterionName()).isEqualTo("Updated Threshold");
        assertThat(response.getThresholdValue()).isEqualByComparingTo("1.8");
    }

    @Test
    @DisplayName("Should reject duplicate active criteria on update when another active exists")
    void shouldRejectDuplicateActiveCriteriaOnUpdate() {
        EvaluationCriteriaUpdateRequest updateRequest = EvaluationCriteriaUpdateRequest.builder()
                .criterionName("Activate Conflict")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.8"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> criteriaService.updateCriteria(1L, updateRequest))
                .isInstanceOf(DuplicateCriteriaException.class);
    }

    @Test
    @DisplayName("Should activate and deactivate criterion status")
    void shouldActivateAndDeactivateCriterionStatus() {
        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));
        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Deactivate
        EvaluationCriteriaResponse deactivated = criteriaService.updateCriteriaStatus(1L, false);
        assertThat(deactivated.getIsActive()).isFalse();

        // 2. Activate (no conflict)
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(1L, 1L)).thenReturn(false);
        EvaluationCriteriaResponse activated = criteriaService.updateCriteriaStatus(1L, true);
        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should reject status activation when another active criterion exists")
    void shouldRejectStatusActivationOnConflict() {
        sampleCriteria.setIsActive(false);
        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));
        when(criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> criteriaService.updateCriteriaStatus(1L, true))
                .isInstanceOf(DuplicateCriteriaException.class);
    }

    @Test
    @DisplayName("Should soft delete criterion by setting inactive")
    void shouldSoftDeleteCriterion() {
        when(criteriaRepository.findById(1L)).thenReturn(Optional.of(sampleCriteria));
        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> i.getArgument(0));

        criteriaService.deleteCriteria(1L);

        assertThat(sampleCriteria.getIsActive()).isFalse();
        verify(criteriaRepository).save(sampleCriteria);
    }
}

