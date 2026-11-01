package com.financial.modules.riskanalysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.DuplicateRiskRuleException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.RiskRuleNotFoundException;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.dto.RiskRuleRequest;
import com.financial.modules.riskanalysis.dto.RiskRuleResponse;
import com.financial.modules.riskanalysis.dto.RiskRuleStatusRequest;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.service.RiskAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RiskRuleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private RiskAnalysisService riskAnalysisService;

    @InjectMocks
    private RiskRuleController riskRuleController;

    private RiskRuleResponse sampleResponse;
    private RiskRuleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleResponse = RiskRuleResponse.builder()
                .id(1L)
                .ruleCode("RULE_CR_LOW")
                .ruleName("Khả năng thanh toán hiện hành thấp")
                .description("Cảnh báo khi current ratio dưới 1.0")
                .riskCategory(RiskCategory.LIQUIDITY)
                .riskCategoryDisplayName("Khả năng thanh toán")
                .ratioCode("CURRENT_RATIO")
                .comparisonOperator(ComparisonOperator.LESS_THAN)
                .comparisonOperatorSymbol("<")
                .thresholdValue(new BigDecimal("1.0000"))
                .severity(RiskSeverity.HIGH)
                .severityDisplayName("Cao")
                .explanationTemplate("Chỉ số {ratioCode} thấp")
                .recommendationTemplate("Khuyến nghị cải thiện")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        sampleRequest = RiskRuleRequest.builder()
                .ruleCode("RULE_CR_LOW")
                .ruleName("Khả năng thanh toán hiện hành thấp")
                .description("Cảnh báo khi current ratio dưới 1.0")
                .riskCategory(RiskCategory.LIQUIDITY)
                .ratioCode("CURRENT_RATIO")
                .comparisonOperator(ComparisonOperator.LESS_THAN)
                .thresholdValue(new BigDecimal("1.0000"))
                .severity(RiskSeverity.HIGH)
                .explanationTemplate("Chỉ số {ratioCode} thấp")
                .recommendationTemplate("Khuyến nghị cải thiện")
                .isActive(true)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(riskRuleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/risk-rules returns 200 OK with page of rules")
    void testGetRiskRules_Success() throws Exception {
        when(riskAnalysisService.getRiskRules(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/risk-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].ruleCode").value("RULE_CR_LOW"))
                .andExpect(jsonPath("$.data.items[0].riskCategory").value("LIQUIDITY"));
    }

    @Test
    @DisplayName("GET /api/risk-rules/{id} returns 200 OK when rule exists")
    void testGetRiskRuleById_Success() throws Exception {
        when(riskAnalysisService.getRiskRuleById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/risk-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ruleCode").value("RULE_CR_LOW"));
    }

    @Test
    @DisplayName("GET /api/risk-rules/{id} returns 404 when rule not found")
    void testGetRiskRuleById_NotFound() throws Exception {
        when(riskAnalysisService.getRiskRuleById(99L)).thenThrow(new RiskRuleNotFoundException(99L));

        mockMvc.perform(get("/api/risk-rules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/risk-rules returns 201 Created on valid request")
    void testCreateRiskRule_Success() throws Exception {
        when(riskAnalysisService.createRiskRule(any(RiskRuleRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/risk-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ruleCode").value("RULE_CR_LOW"));
    }

    @Test
    @DisplayName("POST /api/risk-rules returns 409 Conflict when rule code is duplicate")
    void testCreateRiskRule_Duplicate() throws Exception {
        when(riskAnalysisService.createRiskRule(any(RiskRuleRequest.class)))
                .thenThrow(new DuplicateRiskRuleException("Risk rule with code 'RULE_CR_LOW' already exists"));

        mockMvc.perform(post("/api/risk-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/risk-rules/{id} returns 200 OK on update")
    void testUpdateRiskRule_Success() throws Exception {
        when(riskAnalysisService.updateRiskRule(eq(1L), any(RiskRuleRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/risk-rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PATCH /api/risk-rules/{id}/status returns 200 OK on status toggle")
    void testUpdateRiskRuleStatus_Success() throws Exception {
        RiskRuleStatusRequest statusRequest = RiskRuleStatusRequest.builder().isActive(false).build();
        sampleResponse.setIsActive(false);

        when(riskAnalysisService.updateRiskRuleStatus(1L, false)).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/risk-rules/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
