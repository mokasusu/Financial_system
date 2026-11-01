package com.financial.modules.aianalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating user-edited financial analysis comments")
public class AIAnalysisFinalCommentRequest {

    @NotBlank(message = "Final comment cannot be blank")
    @Schema(description = "User edited and approved financial commentary", example = "Doanh nghiệp có cơ cấu tài chính ổn định nhưng cần theo dõi sát kỳ thu tiền bình quân.")
    private String finalComment;
}

