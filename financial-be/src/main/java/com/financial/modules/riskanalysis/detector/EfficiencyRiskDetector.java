package com.financial.modules.riskanalysis.detector;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EfficiencyRiskDetector extends AbstractRatioRiskDetector {

    private static final List<String> TARGET_RATIOS = List.of(
            "ASSET_TURNOVER",
            "INVENTORY_TURNOVER",
            "RECEIVABLES_TURNOVER",
            "PAYABLES_TURNOVER"
    );

    public EfficiencyRiskDetector(RiskAnalysisMapper mapper) {
        super(mapper);
    }

    @Override
    public RiskCategory getCategory() {
        return RiskCategory.OPERATIONAL_EFFICIENCY;
    }

    @Override
    public List<String> getTargetRatioCodes() {
        return TARGET_RATIOS;
    }
}

