package com.financial.modules.calculationengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.CalculationNotFoundException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.UnconfirmedStatementException;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.calculationengine.dto.CalculationHistoryResponse;
import com.financial.modules.calculationengine.dto.CalculationResponse;
import com.financial.modules.calculationengine.dto.CalculationResultResponse;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.service.CalculationService;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CalculationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CalculationService calculationService;

    @InjectMocks
    private CalculationController calculationController;

    private UserPrincipal samplePrincipal;
    private CalculationResponse sampleCalcResponse;
    private CalculationResultResponse sampleKpiResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("test@financial.com")
                .fullName("Financial Analyst")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleKpiResponse = CalculationResultResponse.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY)
                .unit(FinancialUnit.TIMES)
                .isCoreKpi(true)
                .status(CalculationStatus.SUCCESS)
                .statusDisplayName("Thành công")
                .resultValue(new BigDecimal("2.0000"))
                .formulaSnapshot("CURRENT_ASSETS / CURRENT_LIABILITIES")
                .build();

        sampleCalcResponse = CalculationResponse.builder()
                .id(101L)
                .statementId(100L)
                .calculationVersion(1)
                .confirmedDataVersion("2026-11-01T08:00:00")
                .status(CalculationStatus.SUCCESS)
                .statusDisplayName("Thành công")
                .calculatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .results(List.of(sampleKpiResponse))
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

        mockMvc = MockMvcBuilders.standaloneSetup(calculationController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/calculations returns 201 Created on success")
    void testCalculate_Success() throws Exception {
        when(calculationService.calculateForStatement(eq(1L), eq(100L), any()))
                .thenReturn(sampleCalcResponse);

        mockMvc.perform(post("/api/financial-statements/100/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.calculationVersion").value(1))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/calculations returns 400 when unconfirmed")
    void testCalculate_Unconfirmed_ReturnsBadRequest() throws Exception {
        when(calculationService.calculateForStatement(eq(1L), eq(100L), any()))
                .thenThrow(new UnconfirmedStatementException(100L, "REVIEWING_DATA"));

        mockMvc.perform(post("/api/financial-statements/100/calculations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("STATEMENT_NOT_CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/calculations returns 200 with history")
    void testGetHistory_Success() throws Exception {
        CalculationHistoryResponse historyItem = CalculationHistoryResponse.builder()
                .id(101L)
                .statementId(100L)
                .calculationVersion(1)
                .status(CalculationStatus.SUCCESS)
                .totalRatios(14)
                .successfulRatios(14)
                .build();

        when(calculationService.getCalculationHistory(1L, 100L))
                .thenReturn(List.of(historyItem));

        mockMvc.perform(get("/api/financial-statements/100/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].calculationVersion").value(1))
                .andExpect(jsonPath("$.data[0].totalRatios").value(14));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/calculations/latest returns 200 on success")
    void testGetLatest_Success() throws Exception {
        when(calculationService.getLatestCalculation(1L, 100L))
                .thenReturn(sampleCalcResponse);

        mockMvc.perform(get("/api/financial-statements/100/calculations/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101))
                .andExpect(jsonPath("$.data.results[0].ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/calculations/latest returns 404 when not found")
    void testGetLatest_NotFound() throws Exception {
        when(calculationService.getLatestCalculation(1L, 100L))
                .thenThrow(new CalculationNotFoundException("No calculations found"));

        mockMvc.perform(get("/api/financial-statements/100/calculations/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CALCULATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/calculations/{calculationId} returns 200 on success")
    void testGetById_Success() throws Exception {
        when(calculationService.getCalculationById(1L, 100L, 101L))
                .thenReturn(sampleCalcResponse);

        mockMvc.perform(get("/api/financial-statements/100/calculations/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/calculations/latest/kpis returns 200 with 4 core KPIs")
    void testGetLatestCoreKpis_Success() throws Exception {
        when(calculationService.getLatestCoreKpis(1L, 100L))
                .thenReturn(List.of(sampleKpiResponse));

        mockMvc.perform(get("/api/financial-statements/100/calculations/latest/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ratioCode").value("CURRENT_RATIO"))
                .andExpect(jsonPath("$.data[0].isCoreKpi").value(true));
    }
}

