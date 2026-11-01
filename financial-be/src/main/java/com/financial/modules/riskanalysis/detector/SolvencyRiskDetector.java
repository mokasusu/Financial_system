package com.financial.modules.riskanalysis.detector;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SolvencyRiskDetector extends AbstractRatioRiskDetector {

    private static final List<String> TARGET_RATIOS = List.of(
            "DEBT_TO_EQUITY",
            "DEBT_RATIO",
            "EQUITY_RATIO"
    );

    public SolvencyRiskDetector(RiskAnalysisMapper mapper) {
        super(mapper);
    }

    @Override
    public RiskCategory getCategory() {
        return RiskCategory.SOLVENCY;
    }

    @Override
    public List<String> getTargetRatioCodes() {
        return TARGET_RATIOS;
    }
}

