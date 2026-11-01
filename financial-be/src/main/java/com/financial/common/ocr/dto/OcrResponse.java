package com.financial.common.ocr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "External OCR response payload")
public class OcrResponse {

    @Schema(description = "Whether the OCR processing succeeded", example = "true")
    private Boolean success;

    @Schema(description = "Task or session ID returned by OCR engine", example = "ocr-task-12345")
    private String taskId;

    @Schema(description = "Status of the OCR processing task", example = "COMPLETED")
    private String status;

    @Schema(description = "Error code if OCR failed", example = "OCR_TIMEOUT")
    private String errorCode;

    @Schema(description = "Error message if OCR failed")
    private String errorMessage;

    @Schema(description = "Raw transcribed text from OCR")
    private String rawText;

    @Schema(description = "Structured key-value pairs or extracted financial metrics")
    private Map<String, Object> data;
}

