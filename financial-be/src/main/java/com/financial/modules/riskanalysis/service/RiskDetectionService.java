package com.financial.modules.riskanalysis.service;

import com.financial.modules.riskanalysis.detector.RiskDetectionContext;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;

import java.util.List;

public interface RiskDetectionService {

    List<IdentifiedRisk> detectAllRisks(RiskDetectionContext context, List<RiskRule> rules);
}

