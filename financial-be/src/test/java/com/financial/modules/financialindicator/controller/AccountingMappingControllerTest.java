package com.financial.modules.financialindicator.controller;

import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.MappingNotFoundException;
import com.financial.modules.financialindicator.dto.AccountingIndicatorMappingResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.MappingStatus;
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
class AccountingMappingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinancialIndicatorService indicatorService;

    @InjectMocks
    private AccountingMappingController mappingController;

    private AccountingIndicatorMappingResponse sampleMappingResponse;
    private FinancialIndicatorResponse sampleIndicatorResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mappingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleMappingResponse = AccountingIndicatorMappingResponse.builder()
                .id(1L)
                .indicatorId(1L)
                .indicatorCode("CURRENT_ASSETS")
                .indicatorName("Tài sản ngắn hạn")
                .accountingStandard(AccountingStandard.TT200)
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .sourceCode("100")
                .sourceKey("CURRENT_ASSETS")
                .sourceLabel("Tài sản ngắn hạn")
                .mappingStatus(MappingStatus.ACTIVE)
                .build();

        sampleIndicatorResponse = FinancialIndicatorResponse.builder()
                .id(1L)
                .indicatorCode("CURRENT_ASSETS")
                .indicatorName("Tài sản ngắn hạn")
                .indicatorNameEn("Current Assets")
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .build();
    }

    @Test
    @DisplayName("GET /api/accounting-mappings - should return 200 with paginated mappings")
    void shouldReturnPaginatedMappings() throws Exception {
        Page<AccountingIndicatorMappingResponse> page = new PageImpl<>(List.of(sampleMappingResponse), PageRequest.of(0, 50), 1);
        when(indicatorService.getMappings(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/accounting-mappings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].sourceCode").value("100"));
    }

    @Test
    @DisplayName("GET /api/accounting-mappings/standard/{standard} - should return 200 with standard mappings")
    void shouldReturnMappingsByStandard() throws Exception {
        when(indicatorService.getMappingsByStandard(AccountingStandard.TT200))
                .thenReturn(List.of(sampleMappingResponse));

        mockMvc.perform(get("/api/accounting-mappings/standard/TT200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sourceCode").value("100"));
    }

    @Test
    @DisplayName("GET /api/accounting-mappings/resolve - should return 200 when resolved")
    void shouldResolveMappingSuccessfully() throws Exception {
        when(indicatorService.resolveMapping(
                eq(AccountingStandard.TT200), eq(FinancialStatementType.BALANCE_SHEET), eq("100"), any()
        )).thenReturn(sampleIndicatorResponse);

        mockMvc.perform(get("/api/accounting-mappings/resolve")
                        .param("accountingStandard", "TT200")
                        .param("statementType", "BALANCE_SHEET")
                        .param("sourceCode", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.indicatorCode").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("GET /api/accounting-mappings/resolve - should return 404 when mapping not found")
    void shouldReturn404WhenMappingNotFound() throws Exception {
        when(indicatorService.resolveMapping(
                eq(AccountingStandard.TT200), eq(FinancialStatementType.BALANCE_SHEET), eq("9999"), any()
        )).thenThrow(MappingNotFoundException.forLookup("TT200", "BALANCE_SHEET", "9999"));

        mockMvc.perform(get("/api/accounting-mappings/resolve")
                        .param("accountingStandard", "TT200")
                        .param("statementType", "BALANCE_SHEET")
                        .param("sourceCode", "9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MAPPING_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/accounting-mappings/resolve - should return 400 when missing required parameters")
    void shouldReturn400WhenMissingParameters() throws Exception {
        mockMvc.perform(get("/api/accounting-mappings/resolve")
                        .param("accountingStandard", "TT200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

