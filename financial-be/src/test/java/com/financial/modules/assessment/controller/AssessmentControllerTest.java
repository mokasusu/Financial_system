package com.financial.modules.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.AssessmentNotFoundException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.modules.assessment.dto.*;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.assessment.service.AssessmentService;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssessmentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AssessmentService assessmentService;

    @InjectMocks
    private AssessmentController assessmentController;

    private UserPrincipal samplePrincipal;
    private AssessmentResponse sampleResponse;
    private AssessmentDetailResponse sampleDetailResponse;
    private AssessmentCategoryResponse sampleCategoryResponse;
    private AssessmentSummaryResponse sampleSummaryResponse;
    private AssessmentHistoryResponse sampleHistoryResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("analyst@financial.com")
                .fullName("Auditor User")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleDetailResponse = AssessmentDetailResponse.builder()
                .id(101L)
                .ratioCode("CURRENT_RATIO")
                .ratioName("Hệ số khả năng thanh toán hiện hành")
                .category(FinancialRatioCategory.LIQUIDITY)
                .calculatedValue(new BigDecimal("1.8000"))
                .unit(FinancialUnit.TIMES)
                .unitDisplayName("Lần")
                .comparisonOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL)
                .comparisonOperatorSymbol(">=")
                .thresholdValue(new BigDecimal("1.5000"))
                .evaluationStatus(RatioEvaluationStatus.PASS)
                .evaluationExplanation("Chỉ số đạt chuẩn")
                .build();

        sampleCategoryResponse = AssessmentCategoryResponse.builder()
                .id(1L)
                .category(FinancialRatioCategory.LIQUIDITY)
                .categoryName("Khả năng thanh toán")
                .totalRatios(3)
                .passedRatios(1)
                .failedRatios(0)
                .insufficientDataRatios(0)
                .notConfiguredRatios(2)
                .categoryScore(null)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .build();

        sampleResponse = AssessmentResponse.builder()
                .id(501L)
                .financialStatementId(100L)
                .companyId(10L)
                .companyName("Công ty ABC")
                .assessmentVersion(1)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .overallScore(null)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .dataQualityStatus(DataQualityStatus.VALID)
                .totalRatios(14)
                .passedRatios(4)
                .failedRatios(0)
                .insufficientDataRatios(0)
                .notConfiguredRatios(10)
                .assessedAt(LocalDateTime.now())
                .details(List.of(sampleDetailResponse))
                .categoryScores(List.of(sampleCategoryResponse))
                .build();

        sampleSummaryResponse = AssessmentSummaryResponse.builder()
                .assessmentId(501L)
                .financialStatementId(100L)
                .companyId(10L)
                .assessmentVersion(1)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .dataQualityStatus(DataQualityStatus.VALID)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .totalEvaluatedRatios(14)
                .totalPass(4)
                .assessedAt(LocalDateTime.now())
                .build();

        sampleHistoryResponse = AssessmentHistoryResponse.builder()
                .id(501L)
                .assessmentVersion(1)
                .assessmentStatus(AssessmentStatus.COMPLETED)
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .dataQualityStatus(DataQualityStatus.VALID)
                .totalRatios(14)
                .passedRatios(4)
                .assessedAt(LocalDateTime.now())
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

        mockMvc = MockMvcBuilders.standaloneSetup(assessmentController)
                .setCustomArgumentResolvers(authPrincipalResolver, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/financial-statements/{statementId}/assessments returns 201 Created on success")
    void testCreateAssessment_Success() throws Exception {
        when(assessmentService.createAssessment(eq(1L), eq(100L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/financial-statements/100/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.assessmentVersion").value(1))
                .andExpect(jsonPath("$.data.assessmentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.scoringStatus").value("NOT_CONFIGURED"))
                .andExpect(jsonPath("$.data.healthClassification").value("NOT_CONFIGURED"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments returns 200 with history")
    void testGetAssessmentHistory_Success() throws Exception {
        when(assessmentService.getAssessmentHistory(eq(1L), eq(100L), any()))
                .thenReturn(new PageImpl<>(List.of(sampleHistoryResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/financial-statements/100/assessments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(501))
                .andExpect(jsonPath("$.data.items[0].assessmentVersion").value(1));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/latest returns 200 on success")
    void testGetLatestAssessment_Success() throws Exception {
        when(assessmentService.getLatestAssessment(1L, 100L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-statements/100/assessments/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.financialStatementId").value(100));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/{assessmentId} returns 200 on success")
    void testGetAssessmentById_Success() throws Exception {
        when(assessmentService.getAssessmentById(1L, 100L, 501L))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-statements/100/assessments/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(501));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/{assessmentId} returns 404 when not found")
    void testGetAssessmentById_NotFound() throws Exception {
        when(assessmentService.getAssessmentById(1L, 100L, 999L))
                .thenThrow(new AssessmentNotFoundException(999L));

        mockMvc.perform(get("/api/financial-statements/100/assessments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/{assessmentId}/details returns 200 on success")
    void testGetAssessmentDetails_Success() throws Exception {
        when(assessmentService.getAssessmentDetails(1L, 100L, 501L))
                .thenReturn(List.of(sampleDetailResponse));

        mockMvc.perform(get("/api/financial-statements/100/assessments/501/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ratioCode").value("CURRENT_RATIO"))
                .andExpect(jsonPath("$.data[0].evaluationStatus").value("PASS"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/{assessmentId}/categories returns 200 on success")
    void testGetAssessmentCategories_Success() throws Exception {
        when(assessmentService.getAssessmentCategories(1L, 100L, 501L))
                .thenReturn(List.of(sampleCategoryResponse));

        mockMvc.perform(get("/api/financial-statements/100/assessments/501/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].category").value("LIQUIDITY"))
                .andExpect(jsonPath("$.data[0].passedRatios").value(1));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{statementId}/assessments/{assessmentId}/summary returns 200 on success")
    void testGetAssessmentSummary_Success() throws Exception {
        when(assessmentService.getAssessmentSummary(1L, 100L, 501L))
                .thenReturn(sampleSummaryResponse);

        mockMvc.perform(get("/api/financial-statements/100/assessments/501/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assessmentId").value(501))
                .andExpect(jsonPath("$.data.totalEvaluatedRatios").value(14));
    }
}

