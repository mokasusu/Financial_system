package com.financial.modules.riskanalysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.RiskAnalysisNotFoundException;
import com.financial.common.exception.UnconfirmedStatementException;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.riskanalysis.dto.IdentifiedRiskResponse;
import com.financial.modules.riskanalysis.dto.RiskAnalysisHistoryResponse;
import com.financial.modules.riskanalysis.dto.RiskAnalysisResponse;
import com.financial.modules.riskanalysis.dto.RiskSummaryResponse;
import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.service.RiskAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RiskAnalysisControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private RiskAnalysisService riskAnalysisService;

    @InjectMocks
    private RiskAnalysisController riskAnalysisController;

    private UserPrincipal samplePrincipal;
    private RiskAnalysisResponse sampleAnalysisResponse;
    private RiskSummaryResponse sampleSummaryResponse;
    private IdentifiedRiskResponse sampleRiskResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("risk-analyst@financial.com")
                .fullName("Risk Auditor")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleRiskResponse = IdentifiedRiskResponse.builder()
                .id(101L)
                .riskTitle("Khả năng thanh toán hiện hành thấp")
                .riskDescription("Current ratio thấp")
                .riskCategory(RiskCategory.LIQUIDITY)
                .riskCategoryDisplayName("Khả năng thanh toán")
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .riskStatusDisplayName("Phát hiện rủi ro")
                .relatedRatioCode("CURRENT_RATIO")
                .actualValue(new BigDecimal("0.8500"))
                .thresholdValue(new BigDecimal("1.0000"))
                .recommendation("Cần tăng tài sản ngắn hạn")
                .detectedAt(LocalDateTime.now())
                .build();

        sampleAnalysisResponse = RiskAnalysisResponse.builder()
                .id(501L)
                .financialStatementId(100L)
                .companyId(10L)
                .companyName("Công ty ABC")
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .totalRisks(1)
                .lowRisks(0)
                .mediumRisks(0)
                .highRisks(1)
                .criticalRisks(0)
                .resolvedRisks(0)
                .notConfiguredCount(0)
                .insufficientDataCount(0)
                .analyzedAt(LocalDateTime.now())
                .identifiedRisks(List.of(sampleRiskResponse))
                .build();

        sampleSummaryResponse = RiskSummaryResponse.builder()
                .riskAnalysisId(501L)
                .financialStatementId(100L)
                .companyId(10L)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .totalRisks(1)
                .highRisks(1)
                .categoryCounts(Collections.emptyMap())
                .severityCounts(Collections.emptyMap())
                .analyzedAt(LocalDateTime.now())
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

        mockMvc = MockMvcBuilders.standaloneSetup(riskAnalysisController)
                .setCustomArgumentResolvers(authPrincipalResolver, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/risk-analyses returns 201 Created on success")
    void testAnalyzeRisks_Success() throws Exception {
        when(riskAnalysisService.analyzeRisks(eq(1L), eq(100L), any()))
                .thenReturn(sampleAnalysisResponse);

        mockMvc.perform(post("/api/financial-statements/100/risk-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.analysisVersion").value(1))
                .andExpect(jsonPath("$.data.executionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalRisks").value(1))
                .andExpect(jsonPath("$.data.highRisks").value(1));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/risk-analyses returns 400 when statement is unconfirmed")
    void testAnalyzeRisks_Unconfirmed() throws Exception {
        when(riskAnalysisService.analyzeRisks(eq(1L), eq(100L), any()))
                .thenThrow(new UnconfirmedStatementException(100L, "DRAFT"));

        mockMvc.perform(post("/api/financial-statements/100/risk-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses returns 200 OK with history")
    void testGetRiskAnalysisHistory_Success() throws Exception {
        RiskAnalysisHistoryResponse history = RiskAnalysisHistoryResponse.builder()
                .id(501L)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .totalRisks(1)
                .analyzedAt(LocalDateTime.now())
                .build();

        when(riskAnalysisService.getRiskAnalysisHistory(eq(1L), eq(100L), any()))
                .thenReturn(new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(501))
                .andExpect(jsonPath("$.data.items[0].analysisVersion").value(1));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses/latest returns 200 OK")
    void testGetLatestRiskAnalysis_Success() throws Exception {
        when(riskAnalysisService.getLatestRiskAnalysis(1L, 100L))
                .thenReturn(sampleAnalysisResponse);

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.analysisVersion").value(1));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses/latest returns 404 when not found")
    void testGetLatestRiskAnalysis_NotFound() throws Exception {
        when(riskAnalysisService.getLatestRiskAnalysis(1L, 100L))
                .thenThrow(new RiskAnalysisNotFoundException("No risk analysis found"));

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses/{analysisId} returns 200 OK")
    void testGetRiskAnalysisById_Success() throws Exception {
        when(riskAnalysisService.getRiskAnalysisById(1L, 100L, 501L))
                .thenReturn(sampleAnalysisResponse);

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses/{analysisId}/risks returns 200 OK with list")
    void testGetIdentifiedRisks_Success() throws Exception {
        when(riskAnalysisService.getIdentifiedRisks(eq(1L), eq(100L), eq(501L), any(), any(), any()))
                .thenReturn(List.of(sampleRiskResponse));

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses/501/risks")
                        .param("category", "LIQUIDITY")
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].riskCategory").value("LIQUIDITY"))
                .andExpect(jsonPath("$.data[0].severity").value("HIGH"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/risk-analyses/{analysisId}/summary returns 200 OK with summary")
    void testGetRiskSummary_Success() throws Exception {
        when(riskAnalysisService.getRiskSummary(1L, 100L, 501L))
                .thenReturn(sampleSummaryResponse);

        mockMvc.perform(get("/api/financial-statements/100/risk-analyses/501/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskAnalysisId").value(501))
                .andExpect(jsonPath("$.data.totalRisks").value(1));
    }
}
