package com.financial.modules.evaluationcriteria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.CriteriaNotFoundException;
import com.financial.common.exception.DuplicateCriteriaException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.InvalidCriteriaException;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaStatusRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaUpdateRequest;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.service.EvaluationCriteriaService;
import com.financial.modules.financialindicator.enums.FinancialUnit;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EvaluationCriteriaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EvaluationCriteriaService criteriaService;

    @InjectMocks
    private EvaluationCriteriaController criteriaController;

    private EvaluationCriteriaResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(criteriaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = EvaluationCriteriaResponse.builder()
                .id(1L)
                .ratioDefinitionId(1L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Chỉ số thanh toán hiện hành")
                .criterionName("Current Ratio Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/evaluation-criteria - should return 200 with paginated criteria")
    void shouldReturnPaginatedCriteria() throws Exception {
        Page<EvaluationCriteriaResponse> page = new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1);
        when(criteriaService.getCriteria(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/evaluation-criteria")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/evaluation-criteria/{id} - should return 200 when criterion exists")
    void shouldReturnCriterionById() throws Exception {
        when(criteriaService.getCriteriaById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/evaluation-criteria/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/evaluation-criteria/{id} - should return 404 when criterion not found")
    void shouldReturn404WhenCriterionNotFound() throws Exception {
        when(criteriaService.getCriteriaById(999L)).thenThrow(new CriteriaNotFoundException(999L));

        mockMvc.perform(get("/api/evaluation-criteria/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CRITERIA_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/evaluation-criteria/ratio/{ratioCode} - should return 200 with ratio criteria")
    void shouldReturnCriteriaByRatioCode() throws Exception {
        when(criteriaService.getCriteriaByRatioCode("CURRENT_RATIO")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/evaluation-criteria/ratio/CURRENT_RATIO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("GET /api/evaluation-criteria/ratio/{ratioCode} - should return 404 when ratio does not exist")
    void shouldReturn404WhenRatioCodeNotFound() throws Exception {
        when(criteriaService.getCriteriaByRatioCode("UNKNOWN")).thenThrow(RatioNotFoundException.byCode("UNKNOWN"));

        mockMvc.perform(get("/api/evaluation-criteria/ratio/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RATIO_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/evaluation-criteria - should return 201 on successful creation")
    void shouldCreateCriterionSuccessfully() throws Exception {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Current Ratio Standard")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.5"))
                .unit(FinancialUnit.TIMES)
                .isActive(true)
                .build();

        when(criteriaService.createCriteria(any(EvaluationCriteriaRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/evaluation-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ratioCode").value("CURRENT_RATIO"));
    }

    @Test
    @DisplayName("POST /api/evaluation-criteria - should return 400 when missing required fields")
    void shouldReturn400WhenMissingRequiredFields() throws Exception {
        EvaluationCriteriaRequest invalidRequest = EvaluationCriteriaRequest.builder()
                .ratioCode("") // blank
                .criterionName(null)
                .build();

        mockMvc.perform(post("/api/evaluation-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/evaluation-criteria - should return 409 when active criterion already exists")
    void shouldReturn409OnDuplicateActiveCriteria() throws Exception {
        EvaluationCriteriaRequest request = EvaluationCriteriaRequest.builder()
                .ratioCode("CURRENT_RATIO")
                .criterionName("Duplicate")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("2.0"))
                .unit(FinancialUnit.TIMES)
                .build();

        when(criteriaService.createCriteria(any(EvaluationCriteriaRequest.class)))
                .thenThrow(DuplicateCriteriaException.forRatio("CURRENT_RATIO"));

        mockMvc.perform(post("/api/evaluation-criteria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_ACTIVE_CRITERIA"));
    }

    @Test
    @DisplayName("PUT /api/evaluation-criteria/{id} - should return 200 on successful update")
    void shouldUpdateCriterionSuccessfully() throws Exception {
        EvaluationCriteriaUpdateRequest updateRequest = EvaluationCriteriaUpdateRequest.builder()
                .criterionName("Updated")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .thresholdValue(new BigDecimal("1.8"))
                .unit(FinancialUnit.TIMES)
                .build();

        when(criteriaService.updateCriteria(eq(1L), any(EvaluationCriteriaUpdateRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/evaluation-criteria/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/evaluation-criteria/{id}/status - should return 200 on status update")
    void shouldUpdateStatusSuccessfully() throws Exception {
        EvaluationCriteriaStatusRequest statusRequest = new EvaluationCriteriaStatusRequest(true);
        when(criteriaService.updateCriteriaStatus(1L, true)).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/evaluation-criteria/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/evaluation-criteria/{id} - should return 200 on deactivation")
    void shouldDeactivateCriterionSuccessfully() throws Exception {
        doNothing().when(criteriaService).deleteCriteria(1L);

        mockMvc.perform(delete("/api/evaluation-criteria/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

