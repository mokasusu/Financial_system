package com.financial.modules.riskanalysis.detector;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfitabilityRiskDetector extends AbstractRatioRiskDetector {

    private static final List<String> TARGET_RATIOS = List.of(
            "ROA",
            "ROE",
            "NET_PROFIT_MARGIN",
            "GROSS_PROFIT_MARGIN"
    );

    public ProfitabilityRiskDetector(RiskAnalysisMapper mapper) {
        super(mapper);
    }

    @Override
    public RiskCategory getCategory() {
        return RiskCategory.PROFITABILITY;
    }

    @Override
    public List<String> getTargetRatioCodes() {
        return TARGET_RATIOS;
    }
}

