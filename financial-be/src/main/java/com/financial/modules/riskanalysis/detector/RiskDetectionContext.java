package com.financial.modules.riskanalysis.detector;

import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.validation.entity.ValidationExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskDetectionContext {

    private FinancialStatement statement;
    private Assessment assessment;
    private FinancialCalculation calculation;
    private ValidationExecution validationExecution;

    @Builder.Default
    private Map<String, FinancialCalculationResult> calculationResultsByCode = Collections.emptyMap();

    @Builder.Default
    private List<IdentifiedRisk> previousRisks = Collections.emptyList();
}

