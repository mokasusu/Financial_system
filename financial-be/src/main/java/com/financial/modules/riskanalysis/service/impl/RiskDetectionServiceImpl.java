package com.financial.modules.riskanalysis.service.impl;

import com.financial.modules.riskanalysis.detector.RiskDetectionContext;
import com.financial.modules.riskanalysis.detector.RiskDetector;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.service.RiskDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskDetectionServiceImpl implements RiskDetectionService {

    private final List<RiskDetector> detectors;

    @Override
    public List<IdentifiedRisk> detectAllRisks(RiskDetectionContext context, List<RiskRule> rules) {
        log.info("Running risk detectors. Total detectors available: {}, total active rules: {}",
                detectors != null ? detectors.size() : 0, rules != null ? rules.size() : 0);

        List<IdentifiedRisk> allIdentifiedRisks = new ArrayList<>();

        if (detectors != null) {
            for (RiskDetector detector : detectors) {
                List<IdentifiedRisk> detectorRisks = detector.detect(context, rules);
                if (detectorRisks != null) {
                    allIdentifiedRisks.addAll(detectorRisks);
                }
            }
        }

        log.debug("Completed risk detection. Total identified risk records produced: {}", allIdentifiedRisks.size());
        return allIdentifiedRisks;
    }
}

