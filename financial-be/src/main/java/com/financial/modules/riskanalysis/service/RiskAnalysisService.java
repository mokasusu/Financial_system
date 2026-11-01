package com.financial.modules.riskanalysis.service;

import com.financial.modules.riskanalysis.dto.*;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RiskAnalysisService {

    RiskAnalysisResponse analyzeRisks(Long userId, Long statementId, RiskAnalysisRequest request);

    Page<RiskAnalysisHistoryResponse> getRiskAnalysisHistory(Long userId, Long statementId, Pageable pageable);

    RiskAnalysisResponse getLatestRiskAnalysis(Long userId, Long statementId);

    RiskAnalysisResponse getRiskAnalysisById(Long userId, Long statementId, Long analysisId);

    List<IdentifiedRiskResponse> getIdentifiedRisks(
            Long userId, Long statementId, Long analysisId,
            RiskCategory category, RiskSeverity severity, RiskStatus status
    );

    RiskSummaryResponse getRiskSummary(Long userId, Long statementId, Long analysisId);

    Page<RiskRuleResponse> getRiskRules(
            RiskCategory category, String ratioCode, RiskSeverity severity, Boolean isActive, Pageable pageable
    );

    RiskRuleResponse getRiskRuleById(Long id);

    RiskRuleResponse createRiskRule(RiskRuleRequest request);

    RiskRuleResponse updateRiskRule(Long id, RiskRuleRequest request);

    RiskRuleResponse updateRiskRuleStatus(Long id, Boolean isActive);
}

