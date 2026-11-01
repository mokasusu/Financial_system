package com.financial.modules.dashboard.service;

import com.financial.modules.dashboard.dto.*;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;

import java.util.List;

public interface DashboardService {

    DashboardOverviewResponse getDashboardOverview(Long userId, String period, Long financialStatementId);

    List<DashboardKPIResponse> getCoreKpis(Long userId, String period, Long financialStatementId);

    List<DashboardCategoryResponse> getFinancialRatios(Long userId, String period, Long financialStatementId);

    DashboardVisualAnalyticsResponse getVisualAnalytics(Long userId, String period, Long financialStatementId);

    DashboardValidationResponse getValidationOverview(Long userId, String period, Long financialStatementId);

    DashboardRiskResponse getRiskOverview(Long userId, String period, Long financialStatementId,
                                          RiskSeverity severity, RiskCategory category, RiskStatus status);

    DashboardAICommentaryResponse getAICommentary(Long userId, String period, Long financialStatementId);

    DashboardTrendResponse getFinancialTrends(Long userId, String fromPeriod, String toPeriod, List<String> ratioCodes);
}

