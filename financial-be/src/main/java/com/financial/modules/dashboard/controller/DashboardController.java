package com.financial.modules.dashboard.controller;

import com.financial.common.response.ApiResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.dashboard.dto.*;
import com.financial.modules.dashboard.service.DashboardService;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "10. Dashboard", description = "Endpoints for retrieving unified enterprise health assessment dashboards, KPIs, charts, validations, risks, and commentary")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(
            summary = "Get complete dashboard overview",
            description = "Aggregates the entire enterprise financial health dashboard: global context, core KPIs, 14 ratios, visual analytics, validation, risks, and AI commentary."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardOverviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getDashboardOverview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter (e.g. '2024')")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        DashboardOverviewResponse response = dashboardService.getDashboardOverview(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard overview retrieved successfully", response));
    }

    @GetMapping("/kpis")
    @Operation(
            summary = "Get core KPI scoreboard",
            description = "Retrieves the four core financial KPIs (Current Ratio, Debt-to-Equity, ROA, ROE) along with configured criteria thresholds and evaluation statuses."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Core KPIs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<DashboardKPIResponse>>> getCoreKpis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        List<DashboardKPIResponse> response = dashboardService.getCoreKpis(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("Core KPIs retrieved successfully", response));
    }

    @GetMapping("/financial-ratios")
    @Operation(
            summary = "Get all 14 financial ratios",
            description = "Retrieves all 14 financial ratios grouped into four categories: Liquidity, Solvency, Profitability, and Efficiency."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Financial ratios retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<List<DashboardCategoryResponse>>> getFinancialRatios(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        List<DashboardCategoryResponse> response = dashboardService.getFinancialRatios(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("Financial ratios retrieved successfully", response));
    }

    @GetMapping("/visual-analytics")
    @Operation(
            summary = "Get visual analytics data",
            description = "Provides ratio threshold comparisons, capital structure distribution, and category heatmaps for frontend charts."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Visual analytics data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardVisualAnalyticsResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<DashboardVisualAnalyticsResponse>> getVisualAnalytics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        DashboardVisualAnalyticsResponse response = dashboardService.getVisualAnalytics(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("Visual analytics data retrieved successfully", response));
    }

    @GetMapping("/validation")
    @Operation(
            summary = "Get validation overview",
            description = "Retrieves accounting consistency validation execution details, rule breakdowns, and discrepancy notices."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardValidationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<DashboardValidationResponse>> getValidationOverview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        DashboardValidationResponse response = dashboardService.getValidationOverview(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("Validation overview retrieved successfully", response));
    }

    @GetMapping("/risks")
    @Operation(
            summary = "Get risk overview",
            description = "Retrieves risk analysis summary and list of identified financial risks with optional filtering by severity, category, or status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Risk overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardRiskResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<DashboardRiskResponse>> getRiskOverview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId,
            @Parameter(description = "Filter by risk severity")
            @RequestParam(required = false) RiskSeverity severity,
            @Parameter(description = "Filter by risk category")
            @RequestParam(required = false) RiskCategory category,
            @Parameter(description = "Filter by risk status")
            @RequestParam(required = false) RiskStatus status
    ) {
        DashboardRiskResponse response = dashboardService.getRiskOverview(userPrincipal.getId(), period, financialStatementId, severity, category, status);
        return ResponseEntity.ok(ApiResponse.success("Risk overview retrieved successfully", response));
    }

    @GetMapping("/ai-commentary")
    @Operation(
            summary = "Get latest AI commentary",
            description = "Retrieves executive summary, strengths, risks, recommendations, and review status generated by OpenRouter AI analysis."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI commentary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardAICommentaryResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified financial statement not found")
    })
    public ResponseEntity<ApiResponse<DashboardAICommentaryResponse>> getAICommentary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Optional reporting period filter")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional financial statement ID filter")
            @RequestParam(required = false) Long financialStatementId
    ) {
        DashboardAICommentaryResponse response = dashboardService.getAICommentary(userPrincipal.getId(), period, financialStatementId);
        return ResponseEntity.ok(ApiResponse.success("AI commentary retrieved successfully", response));
    }

    @GetMapping("/trends")
    @Operation(
            summary = "Get historical financial trends",
            description = "Retrieves historical KPI and ratio trends across confirmed financial statements for multi-period analysis."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Financial trends retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardTrendResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DashboardTrendResponse>> getFinancialTrends(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Starting period (inclusive, e.g. '2022')")
            @RequestParam(required = false) String fromPeriod,
            @Parameter(description = "Ending period (inclusive, e.g. '2024')")
            @RequestParam(required = false) String toPeriod,
            @Parameter(description = "Optional list of ratio codes to include (defaults to 4 core KPIs)")
            @RequestParam(required = false) List<String> ratioCodes
    ) {
        DashboardTrendResponse response = dashboardService.getFinancialTrends(userPrincipal.getId(), fromPeriod, toPeriod, ratioCodes);
        return ResponseEntity.ok(ApiResponse.success("Financial trends retrieved successfully", response));
    }
}

