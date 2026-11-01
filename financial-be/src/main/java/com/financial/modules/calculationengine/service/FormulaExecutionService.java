package com.financial.modules.calculationengine.service;

import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialstatement.entity.FinancialStatement;

public interface FormulaExecutionService {

    /**
     * Executes calculation for a single financial ratio against confirmed statement data.
     * Generates input and formula snapshots, applies scale/rounding, and returns a FinancialCalculationResult.
     */
    FinancialCalculationResult executeRatioCalculation(
            FinancialStatement statement,
            FinancialRatioDefinition ratioDefinition,
            Integer scaleOverride
    );
}

