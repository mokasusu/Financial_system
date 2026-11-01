package com.financial.modules.riskanalysis.service.impl;

import com.financial.modules.riskanalysis.dto.RiskSummaryResponse;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import com.financial.modules.riskanalysis.service.RiskAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAggregationServiceImpl implements RiskAggregationService {

    private final RiskAnalysisMapper mapper;

    @Override
    public RiskSummaryResponse aggregateRiskSummary(RiskAnalysis analysis) {
        log.info("Aggregating risk summary for risk analysis ID {}", analysis != null ? analysis.getId() : "null");
        if (analysis == null) return null;

        Map<RiskCategory, Integer> categoryCounts = new EnumMap<>(RiskCategory.class);
        for (RiskCategory cat : RiskCategory.values()) {
            categoryCounts.put(cat, 0);
        }

        Map<RiskSeverity, Integer> severityCounts = new EnumMap<>(RiskSeverity.class);
        for (RiskSeverity sev : RiskSeverity.values()) {
            severityCounts.put(sev, 0);
        }

        int low = 0;
        int medium = 0;
        int high = 0;
        int critical = 0;
        int resolved = 0;
        int notConfigured = 0;
        int insufficient = 0;

        if (analysis.getIdentifiedRisks() != null) {
            for (IdentifiedRisk r : analysis.getIdentifiedRisks()) {
                if (r.getRiskStatus() == RiskStatus.DETECTED || r.getRiskStatus() == RiskStatus.MONITORING) {
                    if (r.getRiskCategory() != null) {
                        categoryCounts.put(r.getRiskCategory(), categoryCounts.get(r.getRiskCategory()) + 1);
                    }
                    if (r.getSeverity() != null) {
                        severityCounts.put(r.getSeverity(), severityCounts.get(r.getSeverity()) + 1);
                        switch (r.getSeverity()) {
                            case LOW -> low++;
                            case MEDIUM -> medium++;
                            case HIGH -> high++;
                            case CRITICAL -> critical++;
                        }
                    }
                } else if (r.getRiskStatus() == RiskStatus.RESOLVED) {
                    resolved++;
                } else if (r.getRiskStatus() == RiskStatus.NOT_CONFIGURED) {
                    notConfigured++;
                } else if (r.getRiskStatus() == RiskStatus.INSUFFICIENT_DATA) {
                    insufficient++;
                }
            }
        }

        int totalActive = low + medium + high + critical;

        Long statementId = analysis.getFinancialStatement() != null ? analysis.getFinancialStatement().getId() : null;
        Long companyId = analysis.getCompany() != null ? analysis.getCompany().getId() : null;

        return RiskSummaryResponse.builder()
                .riskAnalysisId(analysis.getId())
                .financialStatementId(statementId)
                .companyId(companyId)
                .analysisVersion(analysis.getAnalysisVersion())
                .executionStatus(analysis.getExecutionStatus())
                .totalRisks(totalActive)
                .lowRisks(low)
                .mediumRisks(medium)
                .highRisks(high)
                .criticalRisks(critical)
                .resolvedRisks(resolved)
                .notConfiguredCount(notConfigured)
                .insufficientDataCount(insufficient)
                .categoryCounts(categoryCounts)
                .severityCounts(severityCounts)
                .analyzedAt(analysis.getAnalyzedAt())
                .build();
    }
}

