package com.financial.modules.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.StatementNotFoundException;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.dashboard.dto.*;
import com.financial.modules.dashboard.service.DashboardService;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private UserPrincipal samplePrincipal;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("ceo@enterprise.com")
                .fullName("Enterprise CEO")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return samplePrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setCustomArgumentResolvers(authPrincipalResolver, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/dashboard/overview returns 200 OK with complete dashboard data")
    void testGetDashboardOverview_Success() throws Exception {
        DashboardOverviewResponse mockResponse = DashboardOverviewResponse.builder()
                .context(DashboardContextResponse.builder()
                        .companyId(1L)
                        .companyName("Công ty ABC")
                        .hasCompany(true)
                        .hasFinancialStatement(true)
                        .dataStatus("READY")
                        .build())
                .coreKpis(List.of(DashboardKPIResponse.builder()
                        .code("CURRENT_RATIO")
                        .value(new BigDecimal("1.8500"))
                        .build()))
                .build();

        when(dashboardService.getDashboardOverview(eq(1L), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/dashboard/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.context.companyName").value("Công ty ABC"))
                .andExpect(jsonPath("$.data.coreKpis[0].code").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/dashboard/overview returns 404 when requested statement is not found")
    void testGetDashboardOverview_StatementNotFound() throws Exception {
        when(dashboardService.getDashboardOverview(eq(1L), any(), eq(999L)))
                .thenThrow(new StatementNotFoundException(999L));

        mockMvc.perform(get("/api/dashboard/overview")
                        .param("financialStatementId", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("STATEMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/dashboard/kpis returns 200 OK with core KPI scoreboard")
    void testGetCoreKpis_Success() throws Exception {
        DashboardKPIResponse kpi = DashboardKPIResponse.builder()
                .code("CURRENT_RATIO")
                .name("Khả năng thanh toán hiện hành")
                .value(new BigDecimal("1.8500"))
                .configuredThreshold(new BigDecimal("1.5000"))
                .comparisonOperator(">=")
                .evaluationStatus("PASS")
                .build();

        when(dashboardService.getCoreKpis(eq(1L), any(), any()))
                .thenReturn(List.of(kpi));

        mockMvc.perform(get("/api/dashboard/kpis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("CURRENT_RATIO"))
                .andExpect(jsonPath("$.data[0].value").value(1.85));
    }

    @Test
    @DisplayName("GET /api/dashboard/financial-ratios returns 200 OK with categorized ratios")
    void testGetFinancialRatios_Success() throws Exception {
        DashboardCategoryResponse category = DashboardCategoryResponse.builder()
                .category("LIQUIDITY")
                .displayName("Thanh khoản")
                .ratios(List.of(DashboardRatioResponse.builder()
                        .code("CURRENT_RATIO")
                        .value(new BigDecimal("1.8500"))
                        .build()))
                .build();

        when(dashboardService.getFinancialRatios(eq(1L), any(), any()))
                .thenReturn(List.of(category));

        mockMvc.perform(get("/api/dashboard/financial-ratios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("LIQUIDITY"));
    }

    @Test
    @DisplayName("GET /api/dashboard/visual-analytics returns 200 OK with comparisons and capital structure")
    void testGetVisualAnalytics_Success() throws Exception {
        DashboardVisualAnalyticsResponse visualAnalytics = DashboardVisualAnalyticsResponse.builder()
                .capitalStructure(CapitalStructureDTO.builder()
                        .totalAssets(new BigDecimal("10000000000"))
                        .totalLiabilities(new BigDecimal("4000000000"))
                        .equity(new BigDecimal("6000000000"))
                        .build())
                .ratioComparisons(Collections.emptyList())
                .categoryOverview(Collections.emptyList())
                .build();

        when(dashboardService.getVisualAnalytics(eq(1L), any(), any()))
                .thenReturn(visualAnalytics);

        mockMvc.perform(get("/api/dashboard/visual-analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.capitalStructure.totalAssets").value(10000000000L));
    }

    @Test
    @DisplayName("GET /api/dashboard/validation returns 200 OK with validation summary and rules")
    void testGetValidationOverview_Success() throws Exception {
        DashboardValidationResponse validation = DashboardValidationResponse.builder()
                .validationId(301L)
                .overallStatus("PASS")
                .totalRules(5)
                .passedRules(5)
                .build();

        when(dashboardService.getValidationOverview(eq(1L), any(), any()))
                .thenReturn(validation);

        mockMvc.perform(get("/api/dashboard/validation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallStatus").value("PASS"));
    }

    @Test
    @DisplayName("GET /api/dashboard/risks returns 200 OK with risk summary and identified risks")
    void testGetRiskOverview_Success() throws Exception {
        DashboardRiskResponse riskResponse = DashboardRiskResponse.builder()
                .riskAnalysisId(401L)
                .executionStatus("COMPLETED")
                .summary(DashboardRiskSummaryDTO.builder().totalRisks(1).highRisks(1).build())
                .identifiedRisks(List.of(DashboardIdentifiedRiskDTO.builder()
                        .riskId(101L)
                        .title("Rủi ro thanh khoản")
                        .severity("HIGH")
                        .category("LIQUIDITY")
                        .status("DETECTED")
                        .build()))
                .build();

        when(dashboardService.getRiskOverview(eq(1L), any(), any(), eq(RiskSeverity.HIGH), eq(RiskCategory.LIQUIDITY), eq(RiskStatus.DETECTED)))
                .thenReturn(riskResponse);

        mockMvc.perform(get("/api/dashboard/risks")
                        .param("severity", "HIGH")
                        .param("category", "LIQUIDITY")
                        .param("status", "DETECTED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.identifiedRisks[0].title").value("Rủi ro thanh khoản"));
    }

    @Test
    @DisplayName("GET /api/dashboard/ai-commentary returns 200 OK with AI executive summary and commentary")
    void testGetAICommentary_Success() throws Exception {
        DashboardAICommentaryResponse aiCommentary = DashboardAICommentaryResponse.builder()
                .analysisId(501L)
                .executiveSummary("Tình hình tài chính tương đối tốt.")
                .aiGenerationStatus("COMPLETED")
                .reviewStatus("APPROVED")
                .generatedAt(LocalDateTime.now())
                .build();

        when(dashboardService.getAICommentary(eq(1L), any(), any()))
                .thenReturn(aiCommentary);

        mockMvc.perform(get("/api/dashboard/ai-commentary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.executiveSummary").value("Tình hình tài chính tương đối tốt."));
    }

    @Test
    @DisplayName("GET /api/dashboard/trends returns 200 OK with historical financial trends")
    void testGetFinancialTrends_Success() throws Exception {
        DashboardTrendResponse trendResponse = DashboardTrendResponse.builder()
                .companyId(1L)
                .companyName("Công ty ABC")
                .availablePeriods(List.of("2023", "2024"))
                .trendData(List.of(TrendDataPointDTO.builder()
                        .reportingPeriod("2024")
                        .ratioCode("CURRENT_RATIO")
                        .value(new BigDecimal("1.8500"))
                        .build()))
                .build();

        when(dashboardService.getFinancialTrends(eq(1L), eq("2023"), eq("2024"), any()))
                .thenReturn(trendResponse);

        mockMvc.perform(get("/api/dashboard/trends")
                        .param("fromPeriod", "2023")
                        .param("toPeriod", "2024")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availablePeriods[0]").value("2023"))
                .andExpect(jsonPath("$.data.trendData[0].value").value(1.85));
    }
}

