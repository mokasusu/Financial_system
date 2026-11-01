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
@Schema(description = "Financial analysis limitation note")
public class AILimitationResponse {

    @Schema(description = "Limitation ID", example = "1")
    private Long id;

    @Schema(description = "Description of data or analysis limitation", example = "Chưa có đủ số liệu hàng tồn kho để tính toán hệ số vòng quay...")
    private String description;
}

