package com.financial.modules.riskanalysis.service;

import com.financial.modules.riskanalysis.dto.RiskSummaryResponse;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;

public interface RiskAggregationService {

    RiskSummaryResponse aggregateRiskSummary(RiskAnalysis analysis);
}

