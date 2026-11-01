package com.financial.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete aggregated enterprise financial health dashboard overview")
public class DashboardOverviewResponse {

    @Schema(description = "Global context identifying company and financial statement")
    private DashboardContextResponse context;

    @Schema(description = "Data availability statuses across all upstream subsystems")
    private DashboardDataStatusDTO dataStatuses;

    @Schema(description = "Core financial KPI scoreboard (Current Ratio, Debt-to-Equity, ROA, ROE)")
    private List<DashboardKPIResponse> coreKpis;

    @Schema(description = "All 14 financial ratios grouped into four analytical categories")
    private List<DashboardCategoryResponse> financialRatios;

    @Schema(description = "Visual analytics structures for frontend charts and heatmaps")
    private DashboardVisualAnalyticsResponse visualAnalytics;

    @Schema(description = "Validation engine overview and consistency checks")
    private DashboardValidationResponse validation;

    @Schema(description = "Financial risk analysis overview and identified risks")
    private DashboardRiskResponse riskOverview;

    @Schema(description = "AI financial health analysis commentary")
    private DashboardAICommentaryResponse aiCommentary;
}

