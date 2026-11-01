package com.financial.modules.evaluationcriteria.initializer;

import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class EvaluationCriteriaDataInitializer implements ApplicationRunner {

    private final EvaluationCriteriaRepository criteriaRepository;
    private final FinancialRatioDefinitionRepository ratioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting Evaluation Criteria Module data initialization...");

        // 1. CURRENT_RATIO (GREATER_THAN_OR_EQUAL, 1.5, TIMES)
        seedCriterion(
                "CURRENT_RATIO",
                "Current Ratio Standard Threshold",
                ComparisonOperator.GREATER_THAN_OR_EQUAL,
                new BigDecimal("1.5"),
                FinancialUnit.TIMES,
                "Chỉ số thanh toán hiện hành đạt từ 1.5 lần trở lên phản ánh khả năng thanh toán ngắn hạn tốt"
        );

        // 2. DEBT_TO_EQUITY (LESS_THAN_OR_EQUAL, 1.0, TIMES)
        seedCriterion(
                "DEBT_TO_EQUITY",
                "Debt to Equity Standard Threshold",
                ComparisonOperator.LESS_THAN_OR_EQUAL,
                new BigDecimal("1.0"),
                FinancialUnit.TIMES,
                "Hệ số nợ trên vốn chủ sở hữu không vượt quá 1.0 lần đảm bảo an toàn cấu trúc vốn"
        );

        // 3. ROA (GREATER_THAN_OR_EQUAL, 5.0, PERCENT)
        seedCriterion(
                "ROA",
                "ROA Standard Threshold",
                ComparisonOperator.GREATER_THAN_OR_EQUAL,
                new BigDecimal("5.0"),
                FinancialUnit.PERCENT,
                "Tỷ suất sinh lời trên tổng tài sản đạt từ 5.0% trở lên"
        );

        // 4. ROE (GREATER_THAN_OR_EQUAL, 12.0, PERCENT)
        seedCriterion(
                "ROE",
                "ROE Standard Threshold",
                ComparisonOperator.GREATER_THAN_OR_EQUAL,
                new BigDecimal("12.0"),
                FinancialUnit.PERCENT,
                "Tỷ suất sinh lời trên vốn chủ sở hữu đạt từ 12.0% trở lên"
        );

        log.info("Evaluation Criteria Module data initialization completed.");
    }

    private void seedCriterion(
            String ratioCode,
            String criterionName,
            ComparisonOperator operator,
            BigDecimal thresholdValue,
            FinancialUnit unit,
            String description
    ) {
        // Safe idempotent check: do not overwrite existing user configuration
        if (criteriaRepository.existsByRatioDefinitionRatioCode(ratioCode)) {
            log.debug("Evaluation criterion for ratio '{}' already exists. Skipping seed.", ratioCode);
            return;
        }

        Optional<FinancialRatioDefinition> ratioOpt = ratioRepository.findByRatioCode(ratioCode);
        if (ratioOpt.isEmpty()) {
            log.warn("Cannot seed criterion: FinancialRatioDefinition '{}' not found in database.", ratioCode);
            return;
        }

        FinancialRatioDefinition ratio = ratioOpt.get();
        EvaluationCriteria criterion = EvaluationCriteria.builder()
                .ratioDefinition(ratio)
                .criterionName(criterionName)
                .comparisonOperator(operator)
                .thresholdValue(thresholdValue)
                .unit(unit)
                .description(description)
                .isActive(true)
                .build();

        criteriaRepository.save(criterion);
        log.info("Seeded initial evaluation criterion for ratio '{}' with threshold {} {}", ratioCode, thresholdValue, unit);
    }
}

