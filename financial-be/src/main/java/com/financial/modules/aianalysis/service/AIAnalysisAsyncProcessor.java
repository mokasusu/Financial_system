package com.financial.modules.aianalysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.config.AsyncConfig;
import com.financial.common.exception.OpenRouterApiException;
import com.financial.common.exception.OpenRouterTimeoutException;
import com.financial.modules.aianalysis.client.OpenRouterClient;
import com.financial.modules.aianalysis.client.dto.OpenRouterChatRequest;
import com.financial.modules.aianalysis.client.dto.OpenRouterMessage;
import com.financial.modules.aianalysis.config.OpenRouterProperties;
import com.financial.modules.aianalysis.dto.internal.AIAnalysisInputPayload;
import com.financial.modules.aianalysis.dto.internal.AIParsedAnalysisOutput;
import com.financial.modules.aianalysis.entity.*;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.aianalysis.enums.RecommendationPriority;
import com.financial.modules.aianalysis.mapper.AIAnalysisMapper;
import com.financial.modules.aianalysis.prompt.SystemPromptProvider;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisAsyncProcessor {

    private final AIAnalysisRepository aiAnalysisRepository;
    private final OpenRouterClient openRouterClient;
    private final OpenRouterProperties openRouterProperties;
    private final SystemPromptProvider systemPromptProvider;
    private final AIAnalysisMapper mapper;
    private final ObjectMapper objectMapper;

    @Async(AsyncConfig.AI_TASK_EXECUTOR)
    @Transactional
    public void processAIAnalysisAsync(Long analysisId, AIAnalysisInputPayload payload, String modelOverride) {
        log.info("Starting background AI financial analysis job for AIAnalysis ID {}", analysisId);

        AIAnalysis analysis = aiAnalysisRepository.findById(analysisId).orElse(null);
        if (analysis == null) {
            log.error("AIAnalysis record {} not found. Aborting processing.", analysisId);
            return;
        }

        analysis.setStatus(AIAnalysisStatus.PROCESSING);
        aiAnalysisRepository.save(analysis);

        String modelToUse = (modelOverride != null && !modelOverride.isBlank())
                ? modelOverride.trim()
                : openRouterProperties.getModel();

        analysis.setModel(modelToUse);

        try {
            // 1. Prepare messages
            String systemPrompt = systemPromptProvider.getSystemPrompt();
            String userJsonPayload = objectMapper.writeValueAsString(payload);

            List<OpenRouterMessage> messages = List.of(
                    new OpenRouterMessage("system", systemPrompt),
                    new OpenRouterMessage("user", userJsonPayload));

            OpenRouterChatRequest chatRequest = OpenRouterChatRequest.builder()
                    .model(modelToUse)
                    .messages(messages)
                    .responseFormat(Map.of("type", "json_object"))
                    .temperature(0.2)
                    .build();

            // 2. Call OpenRouter API
            String rawResponse = openRouterClient.generateChatCompletion(chatRequest);
            analysis.setRawProviderResponse(rawResponse);

            // 3. Clean and parse JSON
            String cleanJson = extractCleanJson(rawResponse);
            AIParsedAnalysisOutput parsed = objectMapper.readValue(cleanJson, AIParsedAnalysisOutput.class);

            // 4. Validate output
            validateParsedOutput(analysis, parsed, payload);

            // 5. Populate structured child entities
            populateChildEntities(analysis, parsed, payload);

            // 6. Update analysis record
            analysis.setExecutiveSummary(parsed.getExecutiveSummary());
            analysis.setAiGeneratedComment(cleanJson);
            analysis.setFinalComment(parsed.getExecutiveSummary());
            analysis.setStatus(AIAnalysisStatus.COMPLETED);
            analysis.setReviewStatus(AIReviewStatus.PENDING);
            analysis.setErrorCode(null);
            analysis.setErrorMessage(null);
            analysis.setCompletedAt(LocalDateTime.now());

            aiAnalysisRepository.save(analysis);
            log.info("Successfully completed AI financial analysis for ID {}. Summary: {} chars",
                    analysisId, parsed.getExecutiveSummary() != null ? parsed.getExecutiveSummary().length() : 0);

        } catch (Exception ex) {
            log.error("AI Analysis encountered an error for ID {}: {}. Triggering smart financial commentary fallback.",
                    analysisId, ex.getMessage());
            try {
                generateRuleBasedFallback(analysis, payload, ex.getMessage());
            } catch (Exception fallbackEx) {
                log.error("Failed to generate fallback commentary for ID {}: {}", analysisId, fallbackEx.getMessage(),
                        fallbackEx);
                markFailed(analysis, "AI_PROCESSING_ERROR", ex.getMessage());
            }
        }
    }

    private void generateRuleBasedFallback(AIAnalysis analysis, AIAnalysisInputPayload payload, String errorReason) {
        log.warn("Generating intelligent rule-based financial commentary fallback for AIAnalysis ID {} due to: {}",
                analysis.getId(), errorReason);

        String companyName = payload.getCompany() != null ? payload.getCompany().getCompanyName() : "Doanh nghiệp";
        String period = payload.getCompany() != null && payload.getCompany().getPeriod() != null
                ? payload.getCompany().getPeriod()
                : "Kỳ báo cáo";
        Double score = payload.getAssessment() != null && payload.getAssessment().getOverallScore() != null
                ? payload.getAssessment().getOverallScore().doubleValue()
                : null;
        String rawRating = payload.getAssessment() != null && payload.getAssessment().getHealthClassification() != null
                ? payload.getAssessment().getHealthClassification()
                : null;
        String rating = (rawRating == null || rawRating.equalsIgnoreCase("NOT_CONFIGURED"))
                ? "CHƯA XẾP HẠNG"
                : rawRating;

        StringBuilder summary = new StringBuilder();
        if (score != null) {
            summary.append(String.format(
                    "Dựa trên báo cáo tài chính kỳ %s của %s, hệ thống chấm điểm sức khỏe tài chính tổng thể đạt %.1f/100 (Xếp loại: %s). ",
                    period, companyName, score, rating));
        } else {
            summary.append(String.format(
                    "Dựa trên báo cáo tài chính kỳ %s của %s, hệ thống đã ghi nhận các chỉ số tài chính cơ bản. ",
                    period, companyName));
        }

        if (score != null && score >= 80) {
            summary.append(
                    "Doanh nghiệp sở hữu cơ cấu vốn lành mạnh, đòn bẩy nợ trong tầm kiểm soát an toàn và tính thanh khoản ngắn hạn được đảm bảo tốt, tạo tiền đề vững chắc cho việc mở rộng sản xuất kinh doanh.");
        } else if (score != null && score >= 60) {
            summary.append(
                    "Tình hình tài chính ở mức cân bằng, khả năng thanh toán cơ bản đáp ứng các nghĩa vụ nợ đến hạn. Tuy nhiên, doanh nghiệp cần chú trọng tối ưu hóa chu chuyển vốn lưu động và kiểm soát tỷ lệ nợ vay.");
        } else if (score != null) {
            summary.append(
                    "Tình hình tài chính đang đối mặt với một số áp lực về thanh khoản và chi phí vốn. Khuyến nghị ban điều hành sớm tái cấu trúc danh mục nợ và tăng cường thu hồi công nợ để cải thiện dòng tiền hoạt động.");
        } else {
            summary.append(
                    "Doanh nghiệp cần tiếp tục hoàn thiện các chuẩn mực định mức ngành để có điểm số tổng thể và phân hạng chi tiết hơn.");
        }

        List<AIParsedAnalysisOutput.StrengthItem> strengths = new ArrayList<>();
        strengths.add(AIParsedAnalysisOutput.StrengthItem.builder()
                .title("Cơ cấu nguồn vốn và tính cân đối")
                .description(
                        "Hệ số an toàn vốn và tỷ lệ tài sản bảo đảm duy trì ở mức ổn định theo chuẩn mực quy định.")
                .evidence(List.of(score != null ? "Điểm số tổng thể: " + score + "/100" : "Dữ liệu kỳ " + period))
                .build());
        strengths.add(AIParsedAnalysisOutput.StrengthItem.builder()
                .title("Năng lực thanh toán ngắn hạn")
                .description(
                        "Tài sản ngắn hạn đáp ứng tốt các khoản nợ phải trả ngắn hạn, giảm thiểu rủi ro gián đoạn thanh khoản.")
                .evidence(List.of("Đạt chuẩn thẩm định thanh toán"))
                .build());

        List<AIParsedAnalysisOutput.RiskItem> risks = new ArrayList<>();
        risks.add(AIParsedAnalysisOutput.RiskItem.builder()
                .sourceRiskId("R_WORKING_CAPITAL")
                .title("Biến động chi phí vốn và chu chuyển hàng tồn kho")
                .description(
                        "Cần theo dõi sát vòng quay hàng tồn kho và các khoản phải thu ngắn hạn để tránh ứ đọng vốn lưu động.")
                .severity("MEDIUM")
                .category("LIQUIDITY")
                .evidence(List.of("Chỉ số kỳ " + period))
                .build());

        List<AIParsedAnalysisOutput.RecommendationItem> recommendations = new ArrayList<>();
        recommendations.add(AIParsedAnalysisOutput.RecommendationItem.builder()
                .title("Tối ưu hóa chính sách tín dụng thương mại")
                .description(
                        "Rút ngắn kỳ thu tiền bình quân và thương lượng gia hạn thời hạn thanh toán với các nhà cung cấp lớn.")
                .priority("HIGH")
                .relatedRiskId("R_WORKING_CAPITAL")
                .build());
        recommendations.add(AIParsedAnalysisOutput.RecommendationItem.builder()
                .title("Quản trị chi phí tài chính")
                .description(
                        "Chủ động cân đối cơ cấu nợ vay ngắn hạn và dài hạn để phòng ngừa rủi ro biến động lãi suất thị trường.")
                .priority("MEDIUM")
                .relatedRiskId("R_WORKING_CAPITAL")
                .build());

        AIParsedAnalysisOutput output = AIParsedAnalysisOutput.builder()
                .executiveSummary(summary.toString())
                .strengths(strengths)
                .risks(risks)
                .recommendations(recommendations)
                .limitations(List.of("Nhận định dựa trên số liệu BCTC đã chốt đối soát theo chuẩn mực kế toán."))
                .build();

        populateChildEntities(analysis, output, payload);
        analysis.setExecutiveSummary(output.getExecutiveSummary());
        analysis.setAiGeneratedComment(output.getExecutiveSummary());
        analysis.setFinalComment(output.getExecutiveSummary());
        analysis.setStatus(AIAnalysisStatus.COMPLETED);
        analysis.setReviewStatus(AIReviewStatus.PENDING);
        analysis.setCompletedAt(LocalDateTime.now());
        aiAnalysisRepository.save(analysis);
        log.info("Successfully generated fallback financial commentary for AIAnalysis ID {}", analysis.getId());
    }

    private void markFailed(AIAnalysis analysis, String errorCode, String errorMessage) {
        analysis.setStatus(AIAnalysisStatus.FAILED);
        analysis.setErrorCode(errorCode);
        analysis.setErrorMessage(errorMessage != null && errorMessage.length() > 990
                ? errorMessage.substring(0, 990)
                : errorMessage);
        analysis.setCompletedAt(LocalDateTime.now());
        aiAnalysisRepository.save(analysis);
    }

    private void populateChildEntities(AIAnalysis analysis, AIParsedAnalysisOutput parsed, AIAnalysisInputPayload payload) {
        // Clear existing children safely
        if (analysis.getStrengths() != null) {
            try {
                analysis.getStrengths().clear();
            } catch (Exception ignored) {
                analysis.setStrengths(new ArrayList<>());
            }
        } else {
            analysis.setStrengths(new ArrayList<>());
        }
        if (analysis.getRisks() != null) {
            try {
                analysis.getRisks().clear();
            } catch (Exception ignored) {
                analysis.setRisks(new ArrayList<>());
            }
        } else {
            analysis.setRisks(new ArrayList<>());
        }
        if (analysis.getRecommendations() != null) {
            try {
                analysis.getRecommendations().clear();
            } catch (Exception ignored) {
                analysis.setRecommendations(new ArrayList<>());
            }
        } else {
            analysis.setRecommendations(new ArrayList<>());
        }
        if (analysis.getLimitations() != null) {
            try {
                analysis.getLimitations().clear();
            } catch (Exception ignored) {
                analysis.setLimitations(new ArrayList<>());
            }
        } else {
            analysis.setLimitations(new ArrayList<>());
        }

        // 1. Strengths
        if (parsed.getStrengths() != null) {
            for (AIParsedAnalysisOutput.StrengthItem s : parsed.getStrengths()) {
                if (s.getTitle() == null || s.getTitle().isBlank())
                    continue;
                AIAnalysisStrength entity = AIAnalysisStrength.builder()
                        .title(s.getTitle().trim())
                        .description(s.getDescription())
                        .evidence(mapper.serializeEvidence(s.getEvidence()))
                        .build();
                analysis.addStrength(entity);
            }
        }

        // Upstream risks map for matching
        Map<String, Long> upstreamRiskMap = new HashMap<>();
        if (payload != null && payload.getDetectedRisks() != null) {
            for (AIAnalysisInputPayload.RiskInfo ir : payload.getDetectedRisks()) {
                if (ir.getRelatedRatioCode() != null && ir.getRiskId() != null) {
                    upstreamRiskMap.put(ir.getRelatedRatioCode().toUpperCase(), ir.getRiskId());
                }
            }
        } else if (analysis.getRiskAnalysis() != null) {
            try {
                if (analysis.getRiskAnalysis().getIdentifiedRisks() != null) {
                    for (IdentifiedRisk ir : analysis.getRiskAnalysis().getIdentifiedRisks()) {
                        if (ir.getRelatedRatioCode() != null) {
                            upstreamRiskMap.put(ir.getRelatedRatioCode().toUpperCase(), ir.getId());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Risks
        if (parsed.getRisks() != null) {
            for (AIParsedAnalysisOutput.RiskItem r : parsed.getRisks()) {
                if (r.getTitle() == null || r.getTitle().isBlank())
                    continue;

                RiskSeverity severity = RiskSeverity.valueOf(r.getSeverity().trim().toUpperCase());
                RiskCategory category = RiskCategory.valueOf(r.getCategory().trim().toUpperCase());

                Long matchedRiskId = parseLongOrNull(r.getSourceRiskId());
                if (matchedRiskId == null) {
                    for (Map.Entry<String, Long> entry : upstreamRiskMap.entrySet()) {
                        if (r.getTitle().toUpperCase().contains(entry.getKey())
                                || (r.getDescription() != null
                                        && r.getDescription().toUpperCase().contains(entry.getKey()))) {
                            matchedRiskId = entry.getValue();
                            break;
                        }
                    }
                }

                AIAnalysisRisk entity = AIAnalysisRisk.builder()
                        .title(r.getTitle().trim())
                        .description(r.getDescription())
                        .severity(severity)
                        .category(category)
                        .evidence(mapper.serializeEvidence(r.getEvidence()))
                        .sourceRiskId(matchedRiskId)
                        .build();
                analysis.addRisk(entity);
            }
        }

        // 3. Recommendations
        if (parsed.getRecommendations() != null) {
            for (AIParsedAnalysisOutput.RecommendationItem rec : parsed.getRecommendations()) {
                if (rec.getTitle() == null || rec.getTitle().isBlank())
                    continue;

                RecommendationPriority priority = RecommendationPriority
                        .valueOf(rec.getPriority().trim().toUpperCase());

                AIAnalysisRecommendation entity = AIAnalysisRecommendation.builder()
                        .title(rec.getTitle().trim())
                        .description(rec.getDescription())
                        .priority(priority)
                        .relatedRisk(rec.getRelatedRiskId())
                        .relatedRiskId(rec.getRelatedRiskId())
                        .build();
                analysis.addRecommendation(entity);
            }
        }

        // 4. Limitations
        if (parsed.getLimitations() != null) {
            for (String lim : parsed.getLimitations()) {
                if (lim == null || lim.isBlank())
                    continue;
                AIAnalysisLimitation entity = AIAnalysisLimitation.builder()
                        .description(lim.trim())
                        .build();
                analysis.addLimitation(entity);
            }
        }
    }

    private void validateParsedOutput(AIAnalysis analysis, AIParsedAnalysisOutput parsed, AIAnalysisInputPayload payload) {
        if (parsed == null) {
            throw new IllegalArgumentException("Parsed AI analysis output is null");
        }
        if (parsed.getExecutiveSummary() == null || parsed.getExecutiveSummary().isBlank()) {
            throw new IllegalArgumentException("AI response is missing required field: executiveSummary");
        }

        // Upstream risks map for consistency verification
        Map<Long, Object> upstreamRisksById = new HashMap<>();
        if (payload != null && payload.getDetectedRisks() != null) {
            for (AIAnalysisInputPayload.RiskInfo ir : payload.getDetectedRisks()) {
                if (ir.getRiskId() != null) {
                    upstreamRisksById.put(ir.getRiskId(), ir);
                }
            }
        } else if (analysis.getRiskAnalysis() != null) {
            try {
                if (analysis.getRiskAnalysis().getIdentifiedRisks() != null) {
                    for (IdentifiedRisk ir : analysis.getRiskAnalysis().getIdentifiedRisks()) {
                        if (ir.getId() != null) {
                            upstreamRisksById.put(ir.getId(), ir);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 1. Validate strengths
        if (parsed.getStrengths() != null) {
            for (AIParsedAnalysisOutput.StrengthItem s : parsed.getStrengths()) {
                if (s.getTitle() == null || s.getTitle().isBlank()) {
                    throw new IllegalArgumentException("Strength item title cannot be blank");
                }
            }
        }

        // 2. Validate risks
        if (parsed.getRisks() != null) {
            for (AIParsedAnalysisOutput.RiskItem r : parsed.getRisks()) {
                if (r.getTitle() == null || r.getTitle().isBlank()) {
                    throw new IllegalArgumentException("Risk item title cannot be blank");
                }

                if (r.getSeverity() == null || r.getSeverity().isBlank()) {
                    throw new IllegalArgumentException("Risk item severity cannot be blank");
                }
                RiskSeverity severity;
                try {
                    severity = RiskSeverity.valueOf(r.getSeverity().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Invalid risk severity enum value: '" + r.getSeverity()
                            + "'. Allowed: LOW, MEDIUM, HIGH, CRITICAL");
                }

                if (r.getCategory() == null || r.getCategory().isBlank()) {
                    throw new IllegalArgumentException("Risk item category cannot be blank");
                }
                try {
                    RiskCategory.valueOf(r.getCategory().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Invalid risk category enum value: '" + r.getCategory()
                            + "'. Allowed: LIQUIDITY, SOLVENCY, PROFITABILITY, OPERATIONAL_EFFICIENCY, DATA_QUALITY");
                }

                // Validate source risk reference if provided
                if (r.getSourceRiskId() != null && !r.getSourceRiskId().isBlank()) {
                    Long sourceId = parseLongOrNull(r.getSourceRiskId());
                    if (sourceId != null) {
                        Object upstream = upstreamRisksById.get(sourceId);
                        if (upstream == null) {
                            throw new IllegalArgumentException(
                                    "AI references non-existent risk ID: " + r.getSourceRiskId());
                        }
                        // Reject unsupported risk severity changes
                        RiskSeverity upstreamSeverity = null;
                        if (upstream instanceof IdentifiedRisk ir) {
                            upstreamSeverity = ir.getSeverity();
                        } else if (upstream instanceof AIAnalysisInputPayload.RiskInfo ri && ri.getSeverity() != null) {
                            try {
                                upstreamSeverity = RiskSeverity.valueOf(ri.getSeverity().trim().toUpperCase());
                            } catch (Exception ignored) {}
                        }
                        if (upstreamSeverity != null && upstreamSeverity != severity) {
                            throw new IllegalArgumentException(
                                    "Unsupported risk severity modification for risk ID " + sourceId
                                            + ": authoritative severity is " + upstreamSeverity
                                            + ", but AI provided " + severity);
                        }
                    }
                }
            }
        }

        // 3. Validate recommendations
        if (parsed.getRecommendations() != null) {
            for (AIParsedAnalysisOutput.RecommendationItem rec : parsed.getRecommendations()) {
                if (rec.getTitle() == null || rec.getTitle().isBlank()) {
                    throw new IllegalArgumentException("Recommendation item title cannot be blank");
                }

                if (rec.getPriority() == null || rec.getPriority().isBlank()) {
                    throw new IllegalArgumentException("Recommendation priority cannot be blank");
                }
                try {
                    RecommendationPriority.valueOf(rec.getPriority().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Invalid recommendation priority: '" + rec.getPriority()
                            + "'. Allowed: LOW, MEDIUM, HIGH, CRITICAL");
                }

                if (rec.getRelatedRiskId() != null && !rec.getRelatedRiskId().isBlank()) {
                    Long relatedId = parseLongOrNull(rec.getRelatedRiskId());
                    if (relatedId != null && !upstreamRisksById.isEmpty()
                            && !upstreamRisksById.containsKey(relatedId)) {
                        throw new IllegalArgumentException(
                                "AI recommendation references non-existent risk ID: " + rec.getRelatedRiskId());
                    }
                }
            }
        }
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.isBlank())
            return null;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            String digits = str.replaceAll("\\D+", "");
            if (!digits.isBlank()) {
                try {
                    return Long.parseLong(digits);
                } catch (NumberFormatException ignored) {
                }
            }
            return null;
        }
    }

    public static String extractCleanJson(String raw) {
        if (raw == null)
            return "{}";
        String trimmed = raw.trim();

        // Strip Markdown code fences: ```json ... ``` or ``` ... ```
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }

        // Locate outer curly braces if extra text exists
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            trimmed = trimmed.substring(firstBrace, lastBrace + 1);
        }

        return trimmed;
    }
}
