package com.financial.modules.financialindicator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.IndicatorNotFoundException;
import com.financial.common.exception.UnsupportedAccountingStandardException;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.FinancialUnit;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialIndicatorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinancialIndicatorService indicatorService;

    @InjectMocks
    private FinancialIndicatorController indicatorController;

    private FinancialIndicatorResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(indicatorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = FinancialIndicatorResponse.builder()
                .id(1L)
                .indicatorCode("CURRENT_ASSETS")
                .indicatorName("Tài sản ngắn hạn")
                .indicatorNameEn("Current Assets")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .dataType("DECIMAL")
                .unit(FinancialUnit.VND)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/financial-indicators - should return 200 with paginated indicators")
    void shouldReturnPaginatedIndicators() throws Exception {
        Page<FinancialIndicatorResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(indicatorService.getIndicators(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/financial-indicators")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].indicatorCode").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/{id} - should return 200 when indicator exists")
    void shouldReturnIndicatorById() throws Exception {
        when(indicatorService.getIndicatorById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-indicators/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.indicatorCode").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/{id} - should return 404 when indicator does not exist")
    void shouldReturn404WhenIndicatorNotFound() throws Exception {
        when(indicatorService.getIndicatorById(999L)).thenThrow(new IndicatorNotFoundException(999L));

        mockMvc.perform(get("/api/financial-indicators/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INDICATOR_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/code/{code} - should return 200 when found")
    void shouldReturnIndicatorByCode() throws Exception {
        when(indicatorService.getIndicatorByCode("CURRENT_ASSETS")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-indicators/code/CURRENT_ASSETS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.indicatorCode").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/code/{code} - should return 404 when code not found")
    void shouldReturn404WhenIndicatorCodeNotFound() throws Exception {
        when(indicatorService.getIndicatorByCode("UNKNOWN")).thenThrow(IndicatorNotFoundException.byCode("UNKNOWN"));

        mockMvc.perform(get("/api/financial-indicators/code/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INDICATOR_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/by-standard/{standard} - should return 200 with indicators list")
    void shouldReturnIndicatorsByStandard() throws Exception {
        when(indicatorService.getIndicatorsByStandard("TT200")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/financial-indicators/by-standard/TT200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].indicatorCode").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("GET /api/financial-indicators/by-standard/{standard} - should return 400 for invalid standard")
    void shouldReturn400ForInvalidStandard() throws Exception {
        when(indicatorService.getIndicatorsByStandard("INVALID")).thenThrow(new UnsupportedAccountingStandardException("INVALID"));

        mockMvc.perform(get("/api/financial-indicators/by-standard/INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_ACCOUNTING_STANDARD"));
    }
}

