package com.financial.modules.validation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.ValidationRuleNotFoundException;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.dto.ValidationRuleRequest;
import com.financial.modules.validation.dto.ValidationRuleResponse;
import com.financial.modules.validation.dto.ValidationRuleStatusRequest;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.service.ValidationService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ValidationRuleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ValidationRuleController ruleController;

    private ValidationRuleResponse sampleRuleResponse;

    @BeforeEach
    void setUp() {
        sampleRuleResponse = ValidationRuleResponse.builder()
                .id(1L)
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("Cân đối BCTC TT200")
                .description("Kiểm tra phương trình kế toán")
                .accountingStandard(AccountingStandard.TT200)
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .categoryDisplayName("Phương trình kế toán")
                .severity(ValidationSeverity.CRITICAL)
                .severityDisplayName("Nghiêm trọng")
                .toleranceValue(BigDecimal.ZERO)
                .toleranceType(ToleranceType.ABSOLUTE)
                .isActive(true)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(ruleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/validation-rules returns 200 with paginated rules")
    void testGetRules_Success() throws Exception {
        when(validationService.getValidationRules(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(sampleRuleResponse), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/validation-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].ruleCode").value("BS_EQ_TT200_01"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/validation-rules/{id} returns 200 on success")
    void testGetRuleById_Success() throws Exception {
        when(validationService.getValidationRuleById(1L))
                .thenReturn(sampleRuleResponse);

        mockMvc.perform(get("/api/validation-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ruleCode").value("BS_EQ_TT200_01"));
    }

    @Test
    @DisplayName("GET /api/validation-rules/{id} returns 404 when not found")
    void testGetRuleById_NotFound() throws Exception {
        when(validationService.getValidationRuleById(99L))
                .thenThrow(new ValidationRuleNotFoundException(99L));

        mockMvc.perform(get("/api/validation-rules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_RULE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/validation-rules creates rule and returns 201 Created")
    void testCreateRule_Success() throws Exception {
        ValidationRuleRequest req = ValidationRuleRequest.builder()
                .ruleCode("BS_EQ_CUSTOM_01")
                .ruleName("Custom Rule")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.HIGH)
                .build();

        when(validationService.createValidationRule(any(ValidationRuleRequest.class)))
                .thenReturn(sampleRuleResponse);

        mockMvc.perform(post("/api/validation-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ruleCode").value("BS_EQ_TT200_01"));
    }

    @Test
    @DisplayName("PUT /api/validation-rules/{id} updates rule and returns 200 OK")
    void testUpdateRule_Success() throws Exception {
        ValidationRuleRequest req = ValidationRuleRequest.builder()
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("Updated Rule Name")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .build();

        when(validationService.updateValidationRule(eq(1L), any(ValidationRuleRequest.class)))
                .thenReturn(sampleRuleResponse);

        mockMvc.perform(put("/api/validation-rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/validation-rules/{id}/status toggles active status and returns 200 OK")
    void testUpdateRuleStatus_Success() throws Exception {
        ValidationRuleStatusRequest req = new ValidationRuleStatusRequest(false);

        ValidationRuleResponse deactivated = ValidationRuleResponse.builder()
                .id(1L)
                .ruleCode("BS_EQ_TT200_01")
                .isActive(false)
                .build();

        when(validationService.updateValidationRuleStatus(1L, false))
                .thenReturn(deactivated);

        mockMvc.perform(patch("/api/validation-rules/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
