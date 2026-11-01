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
                    new OpenRouterMessage("user", userJsonPayload)
            );

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
            validateParsedOutput(analysis, parsed);

            // 5. Populate structured child entities
            populateChildEntities(analysis, parsed);

            // 6. Update analysis record
            analysis.setExecutiveSummary(parsed.getExecutiveSummary());
            analysis.setAiGeneratedComment(cleanJson);
            analysis.setFinalComment(cleanJson);
            analysis.setStatus(AIAnalysisStatus.COMPLETED);
            analysis.setReviewStatus(AIReviewStatus.PENDING);
            analysis.setErrorCode(null);
            analysis.setErrorMessage(null);
            analysis.setCompletedAt(LocalDateTime.now());

            aiAnalysisRepository.save(analysis);
            log.info("Successfully completed AI financial analysis for ID {}. Summary: {} chars",
                    analysisId, parsed.getExecutiveSummary() != null ? parsed.getExecutiveSummary().length() : 0);

        } catch (OpenRouterTimeoutException ex) {
            log.error("OpenRouter request timed out for analysis ID {}: {}", analysisId, ex.getMessage());
            markFailed(analysis, ex.getErrorCode(), ex.getMessage());
        } catch (OpenRouterApiException ex) {
            log.error("OpenRouter API error for analysis ID {}: {}", analysisId, ex.getMessage());
            markFailed(analysis, ex.getErrorCode(), ex.getMessage());
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse JSON response from OpenRouter for analysis ID {}: {}", analysisId, ex.getMessage());
            markFailed(analysis, "AI_RESPONSE_PARSING_FAILED", "Invalid JSON format returned by AI provider: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("AI response validation failed for analysis ID {}: {}", analysisId, ex.getMessage());
            markFailed(analysis, "AI_VALIDATION_FAILED", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during AI analysis for ID {}: {}", analysisId, ex.getMessage(), ex);
            markFailed(analysis, "AI_PROCESSING_ERROR", ex.getMessage());
        }
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

    private void populateChildEntities(AIAnalysis analysis, AIParsedAnalysisOutput parsed) {
        // Clear existing children if any
        if (analysis.getStrengths() != null) analysis.getStrengths().clear();
        if (analysis.getRisks() != null) analysis.getRisks().clear();
        if (analysis.getRecommendations() != null) analysis.getRecommendations().clear();
        if (analysis.getLimitations() != null) analysis.getLimitations().clear();

        // 1. Strengths
        if (parsed.getStrengths() != null) {
            for (AIParsedAnalysisOutput.StrengthItem s : parsed.getStrengths()) {
                if (s.getTitle() == null || s.getTitle().isBlank()) continue;
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
        if (analysis.getRiskAnalysis() != null && analysis.getRiskAnalysis().getIdentifiedRisks() != null) {
            for (IdentifiedRisk ir : analysis.getRiskAnalysis().getIdentifiedRisks()) {
                if (ir.getRelatedRatioCode() != null) {
                    upstreamRiskMap.put(ir.getRelatedRatioCode().toUpperCase(), ir.getId());
                }
            }
        }

        // 2. Risks
        if (parsed.getRisks() != null) {
            for (AIParsedAnalysisOutput.RiskItem r : parsed.getRisks()) {
                if (r.getTitle() == null || r.getTitle().isBlank()) continue;

                RiskSeverity severity = RiskSeverity.valueOf(r.getSeverity().trim().toUpperCase());
                RiskCategory category = RiskCategory.valueOf(r.getCategory().trim().toUpperCase());

                Long matchedRiskId = parseLongOrNull(r.getSourceRiskId());
                if (matchedRiskId == null) {
                    for (Map.Entry<String, Long> entry : upstreamRiskMap.entrySet()) {
                        if (r.getTitle().toUpperCase().contains(entry.getKey())
                                || (r.getDescription() != null && r.getDescription().toUpperCase().contains(entry.getKey()))) {
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
                if (rec.getTitle() == null || rec.getTitle().isBlank()) continue;

                RecommendationPriority priority = RecommendationPriority.valueOf(rec.getPriority().trim().toUpperCase());

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
                if (lim == null || lim.isBlank()) continue;
                AIAnalysisLimitation entity = AIAnalysisLimitation.builder()
                        .description(lim.trim())
                        .build();
                analysis.addLimitation(entity);
            }
        }
    }

    private void validateParsedOutput(AIAnalysis analysis, AIParsedAnalysisOutput parsed) {
        if (parsed == null) {
            throw new IllegalArgumentException("Parsed AI analysis output is null");
        }
        if (parsed.getExecutiveSummary() == null || parsed.getExecutiveSummary().isBlank()) {
            throw new IllegalArgumentException("AI response is missing required field: executiveSummary");
        }

        // Upstream risks map for consistency verification
        Map<Long, IdentifiedRisk> upstreamRisksById = new HashMap<>();
        if (analysis.getRiskAnalysis() != null && analysis.getRiskAnalysis().getIdentifiedRisks() != null) {
            for (IdentifiedRisk ir : analysis.getRiskAnalysis().getIdentifiedRisks()) {
                if (ir.getId() != null) {
                    upstreamRisksById.put(ir.getId(), ir);
                }
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
                    throw new IllegalArgumentException("Invalid risk severity enum value: '" + r.getSeverity() + "'. Allowed: LOW, MEDIUM, HIGH, CRITICAL");
                }

                if (r.getCategory() == null || r.getCategory().isBlank()) {
                    throw new IllegalArgumentException("Risk item category cannot be blank");
                }
                try {
                    RiskCategory.valueOf(r.getCategory().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Invalid risk category enum value: '" + r.getCategory() + "'. Allowed: LIQUIDITY, SOLVENCY, PROFITABILITY, OPERATIONAL_EFFICIENCY, DATA_QUALITY");
                }

                // Validate source risk reference if provided
                if (r.getSourceRiskId() != null && !r.getSourceRiskId().isBlank()) {
                    Long sourceId = parseLongOrNull(r.getSourceRiskId());
                    if (sourceId != null) {
                        IdentifiedRisk upstream = upstreamRisksById.get(sourceId);
                        if (upstream == null) {
                            throw new IllegalArgumentException("AI references non-existent risk ID: " + r.getSourceRiskId());
                        }
                        // Reject unsupported risk severity changes
                        if (upstream.getSeverity() != null && upstream.getSeverity() != severity) {
                            throw new IllegalArgumentException("Unsupported risk severity modification for risk ID " + sourceId
                                    + ": authoritative severity is " + upstream.getSeverity() + ", but AI provided " + severity);
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
                    throw new IllegalArgumentException("Invalid recommendation priority: '" + rec.getPriority() + "'. Allowed: LOW, MEDIUM, HIGH, CRITICAL");
                }

                if (rec.getRelatedRiskId() != null && !rec.getRelatedRiskId().isBlank()) {
                    Long relatedId = parseLongOrNull(rec.getRelatedRiskId());
                    if (relatedId != null && !upstreamRisksById.isEmpty() && !upstreamRisksById.containsKey(relatedId)) {
                        throw new IllegalArgumentException("AI recommendation references non-existent risk ID: " + rec.getRelatedRiskId());
                    }
                }
            }
        }
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.isBlank()) return null;
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
        if (raw == null) return "{}";
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

