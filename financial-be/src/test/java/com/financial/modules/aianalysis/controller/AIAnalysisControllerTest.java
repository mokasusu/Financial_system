package com.financial.modules.aianalysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.AIAnalysisNotFoundException;
import com.financial.common.exception.ConcurrentAIAnalysisException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.UpstreamDataUnavailableException;
import com.financial.modules.aianalysis.dto.AIAnalysisFinalCommentRequest;
import com.financial.modules.aianalysis.dto.AIAnalysisHistoryResponse;
import com.financial.modules.aianalysis.dto.AIAnalysisResponse;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.service.AIAnalysisService;
import com.financial.modules.auth.security.UserPrincipal;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AIAnalysisControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private AIAnalysisService aiAnalysisService;

    @InjectMocks
    private AIAnalysisController aiAnalysisController;

    private UserPrincipal samplePrincipal;
    private AIAnalysisResponse sampleResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("analyst@financial.com")
                .fullName("Senior Financial Analyst")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleResponse = AIAnalysisResponse.builder()
                .id(1L)
                .assessmentId(10L)
                .financialStatementId(100L)
                .companyId(5L)
                .companyName("Công ty Cổ phần ABC")
                .analysisVersion(1)
                .confirmedDataVersion("v1")
                .provider("OPENROUTER")
                .model("openrouter/free")
                .status(AIAnalysisStatus.COMPLETED)
                .statusDisplayName("Hoàn thành phân tích")
                .reviewStatus(AIReviewStatus.PENDING)
                .reviewStatusDisplayName("Chờ duyệt")
                .executiveSummary("Doanh nghiệp có cơ cấu vốn an toàn và thanh khoản tốt.")
                .aiGeneratedComment("{\"executiveSummary\": \"Good\"}")
                .finalComment("{\"executiveSummary\": \"Good\"}")
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .strengths(Collections.emptyList())
                .risks(Collections.emptyList())
                .recommendations(Collections.emptyList())
                .limitations(Collections.emptyList())
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

        mockMvc = MockMvcBuilders.standaloneSetup(aiAnalysisController)
                .setCustomArgumentResolvers(authPrincipalResolver, new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/ai-analyses/assessments/{assessmentId}/generate returns 202 Accepted on success")
    void testGenerateAIAnalysis_Success() throws Exception {
        sampleResponse.setStatus(AIAnalysisStatus.PENDING);
        sampleResponse.setStatusDisplayName("Đang chờ xử lý");

        when(aiAnalysisService.generateAIAnalysis(eq(1L), eq(10L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/ai-analyses/assessments/10/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/ai-analyses/assessments/{assessmentId}/generate returns 400 when upstream data is missing")
    void testGenerateAIAnalysis_UpstreamMissing() throws Exception {
        when(aiAnalysisService.generateAIAnalysis(eq(1L), eq(10L), any()))
                .thenThrow(new UpstreamDataUnavailableException("CALCULATION", "Calculation results missing"));

        mockMvc.perform(post("/api/ai-analyses/assessments/10/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/ai-analyses/assessments/{assessmentId}/generate returns 409 when concurrent analysis running")
    void testGenerateAIAnalysis_ConcurrentConflict() throws Exception {
        when(aiAnalysisService.generateAIAnalysis(eq(1L), eq(10L), any()))
                .thenThrow(new ConcurrentAIAnalysisException(10L, "AI analysis already running"));

        mockMvc.perform(post("/api/ai-analyses/assessments/10/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/ai-analyses/assessments/{assessmentId}/latest returns 200 OK")
    void testGetLatestAIAnalysis_Success() throws Exception {
        when(aiAnalysisService.getLatestAIAnalysis(1L, 10L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/ai-analyses/assessments/10/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/ai-analyses/assessments/{assessmentId}/latest returns 404 when not found")
    void testGetLatestAIAnalysis_NotFound() throws Exception {
        when(aiAnalysisService.getLatestAIAnalysis(1L, 10L))
                .thenThrow(new AIAnalysisNotFoundException("Not found"));

        mockMvc.perform(get("/api/ai-analyses/assessments/10/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/ai-analyses/{analysisId} returns 200 OK with details")
    void testGetAIAnalysisById_Success() throws Exception {
        when(aiAnalysisService.getAIAnalysisById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/ai-analyses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.provider").value("OPENROUTER"));
    }

    @Test
    @DisplayName("GET /api/ai-analyses/assessments/{assessmentId}/history returns 200 OK with paginated items")
    void testGetAIAnalysisHistory_Success() throws Exception {
        AIAnalysisHistoryResponse history = AIAnalysisHistoryResponse.builder()
                .id(1L)
                .assessmentId(10L)
                .analysisVersion(1)
                .status(AIAnalysisStatus.COMPLETED)
                .reviewStatus(AIReviewStatus.PENDING)
                .executiveSummary("Summary")
                .createdAt(LocalDateTime.now())
                .build();

        when(aiAnalysisService.getAIAnalysisHistory(eq(1L), eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/ai-analyses/assessments/10/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].analysisVersion").value(1));
    }

    @Test
    @DisplayName("PUT /api/ai-analyses/{analysisId}/final-comment returns 200 OK on update")
    void testUpdateFinalComment_Success() throws Exception {
        AIAnalysisFinalCommentRequest req = AIAnalysisFinalCommentRequest.builder()
                .finalComment("User edited commentary")
                .build();

        sampleResponse.setFinalComment("User edited commentary");
        sampleResponse.setReviewStatus(AIReviewStatus.EDITED);
        sampleResponse.setReviewStatusDisplayName("Đã chỉnh sửa");

        when(aiAnalysisService.updateFinalComment(eq(1L), eq(1L), any(AIAnalysisFinalCommentRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/ai-analyses/1/final-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.finalComment").value("User edited commentary"))
                .andExpect(jsonPath("$.data.reviewStatus").value("EDITED"));
    }

    @Test
    @DisplayName("POST /api/ai-analyses/{analysisId}/approve returns 200 OK")
    void testApproveAIAnalysis_Success() throws Exception {
        sampleResponse.setReviewStatus(AIReviewStatus.APPROVED);
        sampleResponse.setReviewStatusDisplayName("Đã phê duyệt");

        when(aiAnalysisService.approveAIAnalysis(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/ai-analyses/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/ai-analyses/{analysisId}/reject returns 200 OK")
    void testRejectAIAnalysis_Success() throws Exception {
        sampleResponse.setReviewStatus(AIReviewStatus.REJECTED);
        sampleResponse.setReviewStatusDisplayName("Đã từ chối");

        when(aiAnalysisService.rejectAIAnalysis(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/ai-analyses/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewStatus").value("REJECTED"));
    }
}

