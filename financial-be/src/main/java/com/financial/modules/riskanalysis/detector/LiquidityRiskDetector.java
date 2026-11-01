package com.financial.modules.riskanalysis.detector;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiquidityRiskDetector extends AbstractRatioRiskDetector {

    private static final List<String> TARGET_RATIOS = List.of(
            "CURRENT_RATIO",
            "QUICK_RATIO",
            "CASH_RATIO"
    );

    public LiquidityRiskDetector(RiskAnalysisMapper mapper) {
        super(mapper);
    }

    @Override
    public RiskCategory getCategory() {
        return RiskCategory.LIQUIDITY;
    }

    @Override
    public List<String> getTargetRatioCodes() {
        return TARGET_RATIOS;
    }
}

