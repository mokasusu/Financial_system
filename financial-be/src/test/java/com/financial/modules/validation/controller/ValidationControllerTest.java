package com.financial.modules.validation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.UnconfirmedStatementException;
import com.financial.common.exception.ValidationNotFoundException;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.validation.dto.ValidationHistoryResponse;
import com.financial.modules.validation.dto.ValidationResponse;
import com.financial.modules.validation.dto.ValidationResultResponse;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.service.ValidationService;
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
class ValidationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private ValidationController validationController;

    private UserPrincipal samplePrincipal;
    private ValidationResponse sampleResponse;
    private ValidationResultResponse sampleResultResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("analyst@financial.com")
                .fullName("Auditor User")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleResultResponse = ValidationResultResponse.builder()
                .id(10L)
                .ruleCode("BS_EQ_01")
                .ruleName("Accounting Equation Rule")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .status(ValidationStatus.PASS)
                .severity(ValidationSeverity.CRITICAL)
                .expectedValue(new BigDecimal("100000000000"))
                .actualValue(new BigDecimal("100000000000"))
                .differenceValue(BigDecimal.ZERO)
                .build();

        sampleResponse = ValidationResponse.builder()
                .id(501L)
                .statementId(100L)
                .accountingStandard(AccountingStandard.TT200)
                .executionStatus(ValidationStatus.PASS)
                .totalRules(5)
                .passedRules(5)
                .warningRules(0)
                .failedRules(0)
                .executedAt(LocalDateTime.now())
                .results(List.of(sampleResultResponse))
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

        mockMvc = MockMvcBuilders.standaloneSetup(validationController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/validations returns 201 Created on success")
    void testValidate_Success() throws Exception {
        when(validationService.validateStatement(eq(1L), eq(100L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/financial-statements/100/validations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.executionStatus").value("PASS"));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/validations returns 400 when statement unconfirmed")
    void testValidate_Unconfirmed_ReturnsBadRequest() throws Exception {
        when(validationService.validateStatement(eq(1L), eq(100L), any()))
                .thenThrow(new UnconfirmedStatementException(100L, "REVIEWING_DATA"));

        mockMvc.perform(post("/api/financial-statements/100/validations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("STATEMENT_NOT_CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/validations returns 200 with history")
    void testGetHistory_Success() throws Exception {
        ValidationHistoryResponse item = ValidationHistoryResponse.builder()
                .id(501L)
                .statementId(100L)
                .executionStatus(ValidationStatus.PASS)
                .totalRules(5)
                .passedRules(5)
                .build();

        when(validationService.getValidationHistory(1L, 100L))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/financial-statements/100/validations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(501))
                .andExpect(jsonPath("$.data[0].executionStatus").value("PASS"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/validations/latest returns 200 with latest execution")
    void testGetLatest_Success() throws Exception {
        when(validationService.getLatestValidation(1L, 100L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-statements/100/validations/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/validations/latest returns 404 when not found")
    void testGetLatest_NotFound() throws Exception {
        when(validationService.getLatestValidation(1L, 100L))
                .thenThrow(new ValidationNotFoundException("No validations found"));

        mockMvc.perform(get("/api/financial-statements/100/validations/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/validations/{executionId} returns 200 on success")
    void testGetById_Success() throws Exception {
        when(validationService.getValidationById(1L, 100L, 501L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-statements/100/validations/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/validations/{executionId}/results returns 200 on success")
    void testGetResults_Success() throws Exception {
        when(validationService.getValidationResults(1L, 100L, 501L))
                .thenReturn(List.of(sampleResultResponse));

        mockMvc.perform(get("/api/financial-statements/100/validations/501/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ruleCode").value("BS_EQ_01"));
    }
}

