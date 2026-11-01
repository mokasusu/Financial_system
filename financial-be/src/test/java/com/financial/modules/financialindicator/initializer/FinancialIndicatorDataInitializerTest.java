package com.financial.modules.financialindicator.initializer;

import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialindicator.repository.FinancialIndicatorRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.ApplicationArguments;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FinancialIndicatorDataInitializerTest {

    @Mock
    private FinancialIndicatorRepository indicatorRepository;

    @Mock
    private FinancialRatioDefinitionRepository ratioRepository;

    @Mock
    private FinancialRatioInputRepository ratioInputRepository;

    @Mock
    private AccountingIndicatorMappingRepository mappingRepository;

    @InjectMocks
    private FinancialIndicatorDataInitializer initializer;

    private final Map<String, FinancialIndicator> indicatorStore = new HashMap<>();
    private final Map<String, FinancialRatioDefinition> ratioStore = new HashMap<>();
    private final List<FinancialRatioInput> inputStore = new ArrayList<>();
    private final List<AccountingIndicatorMapping> mappingStore = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @BeforeEach
    void setUp() {
        indicatorStore.clear();
        ratioStore.clear();
        inputStore.clear();
        mappingStore.clear();

        // Simulate save and find for indicatorRepository
        when(indicatorRepository.findByIndicatorCode(anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(indicatorStore.get(code));
        });
        when(indicatorRepository.save(any(FinancialIndicator.class))).thenAnswer(invocation -> {
            FinancialIndicator entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(idGenerator.getAndIncrement());
            }
            indicatorStore.put(entity.getIndicatorCode(), entity);
            return entity;
        });

        // Simulate save and find for ratioRepository
        when(ratioRepository.findByRatioCode(anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(ratioStore.get(code));
        });
        when(ratioRepository.save(any(FinancialRatioDefinition.class))).thenAnswer(invocation -> {
            FinancialRatioDefinition entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(idGenerator.getAndIncrement());
            }
            ratioStore.put(entity.getRatioCode(), entity);
            return entity;
        });

        // Simulate save and find for ratioInputRepository
        when(ratioInputRepository.findByRatioDefinitionIdOrderByInputOrderAsc(anyLong())).thenAnswer(invocation -> {
            Long ratioId = invocation.getArgument(0);
            return inputStore.stream()
                    .filter(input -> input.getRatioDefinition() != null && Objects.equals(input.getRatioDefinition().getId(), ratioId))
                    .toList();
        });
        when(ratioInputRepository.save(any(FinancialRatioInput.class))).thenAnswer(invocation -> {
            FinancialRatioInput entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(idGenerator.getAndIncrement());
            }
            inputStore.add(entity);
            return entity;
        });

        // Simulate mappingRepository
        when(mappingRepository.existsByAccountingStandardAndStatementTypeAndSourceCode(any(), any(), anyString()))
                .thenAnswer(invocation -> {
                    var standard = invocation.getArgument(0);
                    var type = invocation.getArgument(1);
                    String code = invocation.getArgument(2);
                    return mappingStore.stream().anyMatch(m ->
                            m.getAccountingStandard() == standard &&
                            m.getStatementType() == type &&
                            Objects.equals(m.getSourceCode(), code)
                    );
                });
        when(mappingRepository.save(any(AccountingIndicatorMapping.class))).thenAnswer(invocation -> {
            AccountingIndicatorMapping entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(idGenerator.getAndIncrement());
            }
            mappingStore.add(entity);
            return entity;
        });
    }

    @Test
    @DisplayName("Should seed all 19 indicators, 14 ratios, and mappings on startup")
    void shouldSeedAllInitialData() {
        initializer.run(mock(ApplicationArguments.class));

        // 1. Verify 19 indicators seeded
        assertThat(indicatorStore).hasSize(19);
        assertThat(indicatorStore).containsKeys(
                "CURRENT_ASSETS", "INVENTORY", "CASH_AND_EQUIVALENTS", "CURRENT_LIABILITIES",
                "TOTAL_LIABILITIES", "TOTAL_ASSETS", "EQUITY", "NET_PROFIT", "NET_REVENUE",
                "GROSS_PROFIT", "COST_OF_GOODS_SOLD", "INVENTORY_BEGINNING", "INVENTORY_ENDING",
                "RECEIVABLES_BEGINNING", "RECEIVABLES_ENDING", "PAYABLES_BEGINNING", "PAYABLES_ENDING",
                "TOTAL_ASSETS_BEGINNING", "TOTAL_EQUITY_BEGINNING"
        );

        // 2. Verify 14 ratios seeded
        assertThat(ratioStore).hasSize(14);
        assertThat(ratioStore).containsKeys(
                "CURRENT_RATIO", "QUICK_RATIO", "CASH_RATIO",
                "DEBT_TO_EQUITY", "DEBT_RATIO", "EQUITY_RATIO",
                "ROA", "ROE", "NET_PROFIT_MARGIN", "GROSS_PROFIT_MARGIN",
                "ASSET_TURNOVER", "INVENTORY_TURNOVER", "RECEIVABLES_TURNOVER", "PAYABLES_TURNOVER"
        );

        // 3. Verify exactly 4 core KPIs
        long coreKpiCount = ratioStore.values().stream().filter(FinancialRatioDefinition::getIsCoreKpi).count();
        assertThat(coreKpiCount).isEqualTo(4);
        assertThat(ratioStore.get("CURRENT_RATIO").getIsCoreKpi()).isTrue();
        assertThat(ratioStore.get("DEBT_TO_EQUITY").getIsCoreKpi()).isTrue();
        assertThat(ratioStore.get("ROA").getIsCoreKpi()).isTrue();
        assertThat(ratioStore.get("ROE").getIsCoreKpi()).isTrue();

        // 4. Verify ROA and ROE periodReference = ENDING
        FinancialRatioDefinition roa = ratioStore.get("ROA");
        var roaInputs = inputStore.stream()
                .filter(i -> i.getRatioDefinition().getId().equals(roa.getId()))
                .toList();
        assertThat(roaInputs).hasSize(2);
        assertThat(roaInputs.stream().anyMatch(i ->
                "TOTAL_ASSETS".equals(i.getIndicator().getIndicatorCode()) &&
                i.getPeriodReference() == PeriodReference.ENDING
        )).isTrue();

        FinancialRatioDefinition roe = ratioStore.get("ROE");
        var roeInputs = inputStore.stream()
                .filter(i -> i.getRatioDefinition().getId().equals(roe.getId()))
                .toList();
        assertThat(roeInputs).hasSize(2);
        assertThat(roeInputs.stream().anyMatch(i ->
                "EQUITY".equals(i.getIndicator().getIndicatorCode()) &&
                i.getPeriodReference() == PeriodReference.ENDING
        )).isTrue();

        // 5. Verify mappings seeded
        assertThat(mappingStore).isNotEmpty();
    }

    @Test
    @DisplayName("Should be idempotent when run multiple times")
    void shouldBeIdempotentOnSubsequentRuns() {
        // First run
        initializer.run(mock(ApplicationArguments.class));
        int indicatorCountFirst = indicatorStore.size();
        int ratioCountFirst = ratioStore.size();
        int inputCountFirst = inputStore.size();
        int mappingCountFirst = mappingStore.size();

        // Second run
        initializer.run(mock(ApplicationArguments.class));

        assertThat(indicatorStore).hasSize(indicatorCountFirst);
        assertThat(ratioStore).hasSize(ratioCountFirst);
        assertThat(inputStore).hasSize(inputCountFirst);
        assertThat(mappingStore).hasSize(mappingCountFirst);
    }
}

