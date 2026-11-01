package com.financial.modules.evaluationcriteria.initializer;

import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluationCriteriaDataInitializerTest {

    @Mock
    private EvaluationCriteriaRepository criteriaRepository;

    @Mock
    private FinancialRatioDefinitionRepository ratioRepository;

    @InjectMocks
    private EvaluationCriteriaDataInitializer initializer;

    private final Map<String, FinancialRatioDefinition> ratioStore = new HashMap<>();
    private final Map<String, EvaluationCriteria> criteriaStore = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        ratioStore.clear();
        criteriaStore.clear();

        // Populate the 4 core ratio definitions
        ratioStore.put("CURRENT_RATIO", FinancialRatioDefinition.builder().id(1L).ratioCode("CURRENT_RATIO").ratioName("Current Ratio").unit(FinancialUnit.TIMES).build());
        ratioStore.put("DEBT_TO_EQUITY", FinancialRatioDefinition.builder().id(2L).ratioCode("DEBT_TO_EQUITY").ratioName("Debt to Equity").unit(FinancialUnit.TIMES).build());
        ratioStore.put("ROA", FinancialRatioDefinition.builder().id(3L).ratioCode("ROA").ratioName("ROA").unit(FinancialUnit.PERCENT).build());
        ratioStore.put("ROE", FinancialRatioDefinition.builder().id(4L).ratioCode("ROE").ratioName("ROE").unit(FinancialUnit.PERCENT).build());

        when(ratioRepository.findByRatioCode(anyString())).thenAnswer(i -> {
            String code = i.getArgument(0);
            return Optional.ofNullable(ratioStore.get(code));
        });

        when(criteriaRepository.existsByRatioDefinitionRatioCode(anyString())).thenAnswer(i -> {
            String code = i.getArgument(0);
            return criteriaStore.containsKey(code);
        });

        when(criteriaRepository.save(any(EvaluationCriteria.class))).thenAnswer(i -> {
            EvaluationCriteria entity = i.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(idGenerator.getAndIncrement());
            }
            criteriaStore.put(entity.getRatioDefinition().getRatioCode(), entity);
            return entity;
        });
    }

    @Test
    @DisplayName("Should seed exactly the four initial criteria on startup")
    void shouldSeedInitialFourCriteria() {
        initializer.run(mock(ApplicationArguments.class));

        assertThat(criteriaStore).hasSize(4);
        assertThat(criteriaStore).containsKeys("CURRENT_RATIO", "DEBT_TO_EQUITY", "ROA", "ROE");

        // Verify CURRENT_RATIO: >= 1.5 TIMES
        EvaluationCriteria currentRatioCriteria = criteriaStore.get("CURRENT_RATIO");
        assertThat(currentRatioCriteria.getComparisonOperator()).isEqualTo(ComparisonOperator.GREATER_THAN_OR_EQUAL);
        assertThat(currentRatioCriteria.getThresholdValue()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(currentRatioCriteria.getUnit()).isEqualTo(FinancialUnit.TIMES);

        // Verify DEBT_TO_EQUITY: <= 1.0 TIMES
        EvaluationCriteria dteCriteria = criteriaStore.get("DEBT_TO_EQUITY");
        assertThat(dteCriteria.getComparisonOperator()).isEqualTo(ComparisonOperator.LESS_THAN_OR_EQUAL);
        assertThat(dteCriteria.getThresholdValue()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(dteCriteria.getUnit()).isEqualTo(FinancialUnit.TIMES);

        // Verify ROA: >= 5.0 PERCENT
        EvaluationCriteria roaCriteria = criteriaStore.get("ROA");
        assertThat(roaCriteria.getComparisonOperator()).isEqualTo(ComparisonOperator.GREATER_THAN_OR_EQUAL);
        assertThat(roaCriteria.getThresholdValue()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(roaCriteria.getUnit()).isEqualTo(FinancialUnit.PERCENT);

        // Verify ROE: >= 12.0 PERCENT
        EvaluationCriteria roeCriteria = criteriaStore.get("ROE");
        assertThat(roeCriteria.getComparisonOperator()).isEqualTo(ComparisonOperator.GREATER_THAN_OR_EQUAL);
        assertThat(roeCriteria.getThresholdValue()).isEqualByComparingTo(new BigDecimal("12.0"));
        assertThat(roeCriteria.getUnit()).isEqualTo(FinancialUnit.PERCENT);
    }

    @Test
    @DisplayName("Should NOT overwrite user-modified criteria on subsequent restarts (idempotency)")
    void shouldNotOverwriteExistingCriteriaOnRestart() {
        // Initial run
        initializer.run(mock(ApplicationArguments.class));
        assertThat(criteriaStore).hasSize(4);

        // User modifies CURRENT_RATIO threshold from 1.5 to 2.0
        EvaluationCriteria modified = criteriaStore.get("CURRENT_RATIO");
        modified.setThresholdValue(new BigDecimal("2.0"));

        // Application restarts
        initializer.run(mock(ApplicationArguments.class));

        // Threshold should remain user-configured 2.0, not reset to 1.5
        assertThat(criteriaStore.get("CURRENT_RATIO").getThresholdValue()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(criteriaStore).hasSize(4);
    }

    @Test
    @DisplayName("Should handle missing ratio definitions gracefully without crashing")
    void shouldHandleMissingRatioGracefully() {
        ratioStore.remove("ROE");

        initializer.run(mock(ApplicationArguments.class));

        // Only 3 should be seeded, no exception thrown
        assertThat(criteriaStore).hasSize(3);
        assertThat(criteriaStore).doesNotContainKey("ROE");
    }
}

