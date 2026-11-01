package com.financial.modules.aianalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Optional request parameters for AI financial analysis generation")
public class AIAnalysisGenerateRequest {

    @Schema(description = "Optional custom notes or business instructions for generation", example = "Tập trung phân tích rủi ro thanh khoản ngắn hạn.")
    private String customNotes;

    @Schema(description = "Optional OpenRouter model override", example = "openrouter/free")
    private String modelOverride;
}

