package com.financial.modules.aianalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.OpenRouterApiException;
import com.financial.common.exception.OpenRouterTimeoutException;
import com.financial.modules.aianalysis.client.OpenRouterClient;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import com.financial.modules.aianalysis.dto.internal.AIAnalysisInputPayload;
import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.enums.RecommendationPriority;
import com.financial.modules.aianalysis.mapper.AIAnalysisMapper;
import com.financial.modules.aianalysis.prompt.SystemPromptProvider;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAnalysisAsyncProcessorTest {

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;
    @Mock
    private OpenRouterClient openRouterClient;
    @Mock
    private SystemPromptProvider systemPromptProvider;

    private OpenRouterProperties openRouterProperties;
    private final AIAnalysisMapper mapper = new AIAnalysisMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AIAnalysisAsyncProcessor processor;

    private AIAnalysis analysis;
    private AIAnalysisInputPayload payload;

    @BeforeEach
    void setUp() {
        openRouterProperties = new OpenRouterProperties();
        openRouterProperties.setModel("openrouter/free");

        processor = new AIAnalysisAsyncProcessor(
                aiAnalysisRepository,
                openRouterClient,
                openRouterProperties,
                systemPromptProvider,
                mapper,
                objectMapper
        );

        IdentifiedRisk upstreamRisk = IdentifiedRisk.builder()
                .id(101L)
                .relatedRatioCode("CURRENT_RATIO")
                .riskTitle("Rủi ro thanh khoản")
                .riskCategory(RiskCategory.LIQUIDITY)
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .build();

        RiskAnalysis riskAnalysis = RiskAnalysis.builder()
                .id(50L)
                .identifiedRisks(List.of(upstreamRisk))
                .build();

        analysis = AIAnalysis.builder()
                .id(1L)
                .status(AIAnalysisStatus.PENDING)
                .reviewStatus(AIReviewStatus.PENDING)
                .riskAnalysis(riskAnalysis)
                .strengths(new ArrayList<>())
                .risks(new ArrayList<>())
                .recommendations(new ArrayList<>())
                .limitations(new ArrayList<>())
                .build();

        payload = AIAnalysisInputPayload.builder()
                .company(AIAnalysisInputPayload.CompanyInfo.builder().companyName("Công ty ABC").build())
                .build();
    }

    @Test
    @DisplayName("processAIAnalysisAsync completes successfully and parses JSON with code fences")
    void testProcessAIAnalysisAsync_Success() {
        String jsonWithFence = """
                ```json
                {
                  "executiveSummary": "Tình hình tài chính của công ty ổn định với tính thanh khoản cao.",
                  "strengths": [
                    {
                      "title": "Khả năng thanh toán hiện hành vững chắc",
                      "description": "Current ratio đạt 1.85 lần, đảm bảo thanh toán ngắn hạn.",
                      "evidence": ["CURRENT_RATIO = 1.85"]
                    }
                  ],
                  "risks": [
                    {
                      "title": "Cần theo dõi sát CURRENT_RATIO",
                      "description": "Áp lực nợ phải trả đang gia tăng.",
                      "severity": "HIGH",
                      "category": "LIQUIDITY",
                      "evidence": ["Nợ ngắn hạn tăng"]
                    }
                  ],
                  "recommendations": [
                    {
                      "title": "Cải thiện vòng quay khoản phải thu",
                      "description": "Rút ngắn kỳ thu tiền bình quân.",
                      "priority": "HIGH",
                      "relatedRisk": "Thanh khoản"
                    }
                  ],
                  "limitations": [
                    "BCTC chưa có số liệu chi tiết về hàng tồn kho."
                  ]
                }
                ```
                """;

        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any())).thenReturn(jsonWithFence);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, "openrouter/free");

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getReviewStatus()).isEqualTo(AIReviewStatus.PENDING);
        assertThat(analysis.getExecutiveSummary()).contains("ổn định");
        assertThat(analysis.getAiGeneratedComment()).isNotBlank();
        assertThat(analysis.getFinalComment()).isNotBlank();
        assertThat(analysis.getCompletedAt()).isNotNull();

        // Strengths
        assertThat(analysis.getStrengths()).hasSize(1);
        assertThat(analysis.getStrengths().get(0).getTitle()).contains("thanh toán hiện hành");

        // Risks
        assertThat(analysis.getRisks()).hasSize(1);
        assertThat(analysis.getRisks().get(0).getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(analysis.getRisks().get(0).getCategory()).isEqualTo(RiskCategory.LIQUIDITY);
        assertThat(analysis.getRisks().get(0).getSourceRiskId()).isEqualTo(101L);

        // Recommendations
        assertThat(analysis.getRecommendations()).hasSize(1);
        assertThat(analysis.getRecommendations().get(0).getPriority()).isEqualTo(RecommendationPriority.HIGH);

        // Limitations
        assertThat(analysis.getLimitations()).hasSize(1);
        assertThat(analysis.getLimitations().get(0).getDescription()).contains("hàng tồn kho");
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback and completes on malformed JSON response")
    void testProcessAIAnalysisAsync_MalformedJson() {
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any())).thenReturn("Not a valid json response");
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback and completes on OpenRouterTimeoutException")
    void testProcessAIAnalysisAsync_Timeout() {
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any()))
                .thenThrow(new OpenRouterTimeoutException(120L));
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback and completes on OpenRouterApiException")
    void testProcessAIAnalysisAsync_ApiError() {
        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any()))
                .thenThrow(new OpenRouterApiException("OPENROUTER_RATE_LIMIT", "Rate limit exceeded", 429));
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback on invalid enum values in AI output")
    void testProcessAIAnalysisAsync_InvalidEnumValue() {
        String invalidEnumJson = """
                {
                  "executiveSummary": "Summary",
                  "strengths": [],
                  "risks": [
                    {
                      "title": "Risk with invalid severity",
                      "description": "Desc",
                      "severity": "UNKNOWN_SEVERITY_XYZ",
                      "category": "LIQUIDITY",
                      "evidence": []
                    }
                  ],
                  "recommendations": [],
                  "limitations": []
                }
                """;

        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any())).thenReturn(invalidEnumJson);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback when AI attempts to change authoritative risk severity")
    void testProcessAIAnalysisAsync_AuthoritativeSeverityChangeRejected() {
        String changeSeverityJson = """
                {
                  "executiveSummary": "Summary",
                  "strengths": [],
                  "risks": [
                    {
                      "sourceRiskId": "101",
                      "title": "Altered Severity Risk",
                      "description": "Desc",
                      "severity": "LOW",
                      "category": "LIQUIDITY",
                      "evidence": []
                    }
                  ],
                  "recommendations": [],
                  "limitations": []
                }
                """;

        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any())).thenReturn(changeSeverityJson);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }

    @Test
    @DisplayName("processAIAnalysisAsync triggers fallback when AI references a non-existent risk")
    void testProcessAIAnalysisAsync_NonExistentRiskRejected() {
        String nonExistentRiskJson = """
                {
                  "executiveSummary": "Summary",
                  "strengths": [],
                  "risks": [
                    {
                      "sourceRiskId": "9999",
                      "title": "Non-existent Risk",
                      "description": "Desc",
                      "severity": "HIGH",
                      "category": "LIQUIDITY",
                      "evidence": []
                    }
                  ],
                  "recommendations": [],
                  "limitations": []
                }
                """;

        when(aiAnalysisRepository.findById(1L)).thenReturn(Optional.of(analysis));
        when(systemPromptProvider.getSystemPrompt()).thenReturn("System Prompt");
        when(openRouterClient.generateChatCompletion(any())).thenReturn(nonExistentRiskJson);
        when(aiAnalysisRepository.save(any(AIAnalysis.class))).thenReturn(analysis);

        processor.processAIAnalysisAsync(1L, payload, null);

        assertThat(analysis.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(analysis.getExecutiveSummary()).isNotBlank();
    }
}

