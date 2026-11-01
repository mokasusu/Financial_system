package com.financial.modules.aianalysis.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SystemPromptProvider {

    private static final String PROMPT_RESOURCE_PATH = "prompts/financial_analysis_system_prompt.txt";
    private String cachedPrompt;

    public synchronized String getSystemPrompt() {
        if (cachedPrompt != null && !cachedPrompt.isBlank()) {
            return cachedPrompt;
        }

        try {
            ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE_PATH);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    cachedPrompt = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                    log.info("Loaded AI analysis system prompt from {}", PROMPT_RESOURCE_PATH);
                    return cachedPrompt;
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to read system prompt file from classpath: {}. Using fallback prompt.", ex.getMessage());
        }

        cachedPrompt = getFallbackPrompt();
        return cachedPrompt;
    }

    private String getFallbackPrompt() {
        return """
                Bạn là một Chuyên gia Phân tích Tài chính Doanh nghiệp cao cấp.
                Nhiệm vụ của bạn là phân tích dữ liệu tài chính doanh nghiệp đã được hệ thống tính toán, kiểm định, đánh giá và nhận diện rủi ro sẵn.
                
                QUY TẮC:
                1. Chỉ sử dụng dữ liệu được cung cấp, không suy đoán hay bịa đặt số liệu.
                2. Không tính toán lại các chỉ số.
                3. Bảo toàn kết quả từ Calculation Engine, Assessment, Validation và Risk Analysis.
                4. Phân định rõ ràng Facts, Observations và Recommendations.
                5. Nêu rõ hạn chế khi dữ liệu thiếu hoặc có cảnh báo kiểm định.
                6. Phản hồi bằng tiếng Việt dưới định dạng JSON hợp lệ duy nhất:
                {
                  "executiveSummary": "...",
                  "strengths": [{"title": "...", "description": "...", "evidence": ["..."]}],
                  "risks": [{"title": "...", "description": "...", "severity": "HIGH", "category": "LIQUIDITY", "evidence": ["..."]}],
                  "recommendations": [{"title": "...", "description": "...", "priority": "HIGH", "relatedRisk": "..."}],
                  "limitations": ["..."]
                }
                """;
    }
}

