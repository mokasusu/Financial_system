package com.financial.modules.calculationengine.service;

import com.financial.modules.calculationengine.dto.CalculationHistoryResponse;
import com.financial.modules.calculationengine.dto.CalculationRequest;
import com.financial.modules.calculationengine.dto.CalculationResponse;
import com.financial.modules.calculationengine.dto.CalculationResultResponse;

import java.util.List;

public interface CalculationService {

    /**
     * Executes official financial ratio and core KPI calculations for a confirmed financial statement.
     */
    CalculationResponse calculateForStatement(Long userId, Long statementId, CalculationRequest request);

    /**
     * Retrieves the latest calculation execution and all its ratio results.
     */
    CalculationResponse getLatestCalculation(Long userId, Long statementId);

    /**
     * Retrieves the historical calculations for the specified statement.
     */
    List<CalculationHistoryResponse> getCalculationHistory(Long userId, Long statementId);

    /**
     * Retrieves a specific calculation by its ID.
     */
    CalculationResponse getCalculationById(Long userId, Long statementId, Long calculationId);

    /**
     * Retrieves the 4 core KPI results from the latest calculation.
     */
    List<CalculationResultResponse> getLatestCoreKpis(Long userId, Long statementId);
}

