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
@Schema(description = "Visual analytics structures for frontend charts, heatmaps, and distributions")
public class DashboardVisualAnalyticsResponse {

    @Schema(description = "Comparison data for financial ratios against configured criteria thresholds")
    private List<RatioComparisonDTO> ratioComparisons;

    @Schema(description = "Capital structure values (Assets, Liabilities, Equity)")
    private CapitalStructureDTO capitalStructure;

    @Schema(description = "Category-based ratio groupings for heatmaps and overview cards")
    private List<DashboardCategoryResponse> categoryOverview;
}

