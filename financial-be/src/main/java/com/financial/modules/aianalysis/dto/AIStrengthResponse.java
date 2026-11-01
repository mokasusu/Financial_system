package com.financial.modules.aianalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Identified financial strength item")
public class AIStrengthResponse {

    @Schema(description = "Strength ID", example = "1")
    private Long id;

    @Schema(description = "Strength title", example = "Khả năng thanh toán tức thời vững chắc")
    private String title;

    @Schema(description = "Detailed description of the strength", example = "Doanh nghiệp duy trì tỷ lệ tiền mặt dồi dào so với nợ ngắn hạn...")
    private String description;

    @Schema(description = "Supporting data evidence from ratios or statements")
    private List<String> evidence;
}

