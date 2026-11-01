package com.financial.modules.financialindicator.controller;

import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.enums.EvaluationDirection;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialindicator.service.FinancialIndicatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialRatioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinancialIndicatorService indicatorService;

    @InjectMocks
    private FinancialRatioController ratioController;

    private FinancialRatioResponse sampleRatioResponse;
    private FinancialRatioInputResponse sampleInputResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ratioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleInputResponse = FinancialRatioInputResponse.builder()
                .id(1L)
                .ratioDefinitionId(1L)
                .ratioCode("CURRENT_RATIO")
                .indicatorId(1L)
                .indicatorCode("CURRENT_ASSETS")
                .indicatorName("Tài sản ngắn hạn")
                .inputAlias("CURRENT_ASSETS")
                .inputOrder(1)
                .required(true)
                .periodReference(PeriodReference.CURRENT)
                .build();

        sampleRatioResponse = FinancialRatioResponse.builder()
                .id(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .ratioNameEn("Current Ratio")
                .category(FinancialRatioCategory.LIQUIDITY)
                .formulaExpression("CURRENT_ASSETS / CURRENT_LIABILITIES")
                .unit(FinancialUnit.TIMES)
                .evaluationDirection(EvaluationDirection.HIGHER_IS_BETTER)
                .decimalScale(2)
                .isCoreKpi(true)
                .isActive(true)
                .inputs(List.of(sampleInputResponse))
                .build();
    }

    @Test
    @DisplayName("GET /api/financial-ratios - should return 200 with paginated ratios")
    void shouldReturnPaginatedRatios() throws Exception {
        Page<FinancialRatioResponse> page = new PageImpl<>(List.of(sampleRatioResponse), PageRequest.of(0, 20), 1);
        when(indicatorService.getRatios(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/financial-ratios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/core-kpis - should return 200 with core KPIs list")
    void shouldReturnCoreKpis() throws Exception {
        when(indicatorService.getCoreKpis()).thenReturn(List.of(sampleRatioResponse));

        mockMvc.perform(get("/api/financial-ratios/core-kpis")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ratioCode").value("CURRENT_RATIO"))
                .andExpect(jsonPath("$.data[0].isCoreKpi").value(true));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/category/{category} - should return 200 with category ratios")
    void shouldReturnRatiosByCategory() throws Exception {
        when(indicatorService.getRatiosByCategory(FinancialRatioCategory.LIQUIDITY))
                .thenReturn(List.of(sampleRatioResponse));

        mockMvc.perform(get("/api/financial-ratios/category/LIQUIDITY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("LIQUIDITY"));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/{code} - should return 200 when ratio exists")
    void shouldReturnRatioByCode() throws Exception {
        when(indicatorService.getRatioByCode("CURRENT_RATIO")).thenReturn(sampleRatioResponse);

        mockMvc.perform(get("/api/financial-ratios/CURRENT_RATIO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/{code} - should return 404 when ratio does not exist")
    void shouldReturn404WhenRatioNotFound() throws Exception {
        when(indicatorService.getRatioByCode("UNKNOWN")).thenThrow(RatioNotFoundException.byCode("UNKNOWN"));

        mockMvc.perform(get("/api/financial-ratios/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RATIO_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/{code}/inputs - should return 200 with ratio input list")
    void shouldReturnRatioInputs() throws Exception {
        when(indicatorService.getRatioInputs("CURRENT_RATIO")).thenReturn(List.of(sampleInputResponse));

        mockMvc.perform(get("/api/financial-ratios/CURRENT_RATIO/inputs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].inputAlias").value("CURRENT_ASSETS"))
                .andExpect(jsonPath("$.data[0].periodReference").value("CURRENT"));
    }

    @Test
    @DisplayName("GET /api/financial-ratios/{code}/inputs - should return 404 when ratio does not exist")
    void shouldReturn404WhenRatioInputsRatioNotFound() throws Exception {
        when(indicatorService.getRatioInputs("UNKNOWN")).thenThrow(RatioNotFoundException.byCode("UNKNOWN"));

        mockMvc.perform(get("/api/financial-ratios/UNKNOWN/inputs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RATIO_NOT_FOUND"));
    }
}

