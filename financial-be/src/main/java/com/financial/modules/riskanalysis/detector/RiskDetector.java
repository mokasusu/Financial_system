package com.financial.modules.riskanalysis.detector;

import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;

import java.util.List;

public interface RiskDetector {

    RiskCategory getCategory();

    List<IdentifiedRisk> detect(RiskDetectionContext context, List<RiskRule> rules);
}

