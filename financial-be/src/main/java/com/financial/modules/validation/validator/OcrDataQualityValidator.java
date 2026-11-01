package com.financial.modules.validation.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OcrDataQualityValidator implements CategoryValidator {

    private final ObjectMapper objectMapper;

    public OcrDataQualityValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ValidationCategory getCategory() {
        return ValidationCategory.OCR_DATA_QUALITY;
    }

    @Override
    public ValidationResult validate(FinancialStatement statement, ValidationRule rule) {
        BigDecimal minConfidence = rule.getToleranceValue() != null && rule.getToleranceValue().compareTo(BigDecimal.ZERO) > 0
                ? rule.getToleranceValue()
                : new BigDecimal("0.70"); // 70% minimum confidence default

        List<FinancialStatementItem> items = statement.getItems();
        if (items == null || items.isEmpty()) {
            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.REVIEW_REQUIRED)
                    .explanation("Không tìm thấy dòng mục nào trong báo cáo tài chính để đánh giá chất lượng OCR.")
                    .build();
        }

        List<String> unmappedItems = new ArrayList<>();
        List<String> reviewItems = new ArrayList<>();
        List<String> lowConfidenceItems = new ArrayList<>();
        List<String> sourcePages = new ArrayList<>();

        for (FinancialStatementItem item : items) {
            String itemDesc = (item.getCode() != null && !item.getCode().isBlank())
                    ? item.getCode()
                    : (item.getLabel() != null && !item.getLabel().isBlank()
                            ? item.getLabel()
                            : (item.getRawLabel() != null && !item.getRawLabel().isBlank() ? item.getRawLabel() : "Chỉ tiêu không mã"));
            if (item.getSourcePage() != null && !sourcePages.contains(String.valueOf(item.getSourcePage()))) {
                sourcePages.add(String.valueOf(item.getSourcePage()));
            }

            if (item.getMappingStatus() == MappingStatus.UNMAPPED) {
                unmappedItems.add(itemDesc);
            } else if (item.getMappingStatus() == MappingStatus.NEEDS_REVIEW) {
                reviewItems.add(itemDesc);
            }

            if (item.getConfidence() != null && item.getConfidence().compareTo(minConfidence) < 0) {
                lowConfidenceItems.add(String.format("%s (độ tin cậy %s)", itemDesc, item.getConfidence()));
            }
        }

        // Check original OCR JSON warnings if available
        List<String> ocrWarnings = new ArrayList<>();
        if (statement.getOriginalOcrResponse() != null && !statement.getOriginalOcrResponse().isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(statement.getOriginalOcrResponse());
                JsonNode doc = root.path("document");
                if (doc.path("needs_review").asBoolean(false)) {
                    ocrWarnings.add("Văn bản OCR được đánh dấu cần rà soát (needs_review = true)");
                }
                JsonNode warningsNode = doc.path("warnings");
                if (warningsNode.isArray()) {
                    for (JsonNode w : warningsNode) {
                        ocrWarnings.add(w.asText());
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse original OCR response JSON: {}", e.getMessage());
            }
        }

        boolean hasIssues = !unmappedItems.isEmpty() || !reviewItems.isEmpty() || !lowConfidenceItems.isEmpty() || !ocrWarnings.isEmpty();

        if (hasIssues) {
            StringBuilder sb = new StringBuilder();
            if (!unmappedItems.isEmpty()) {
                sb.append("Có ").append(unmappedItems.size()).append(" chỉ tiêu chưa ánh xạ chuẩn: ")
                        .append(formatSampleList(unmappedItems, 8)).append(". ");
            }
            if (!reviewItems.isEmpty()) {
                sb.append("Có ").append(reviewItems.size()).append(" chỉ tiêu cần kiểm tra lại ánh xạ: ")
                        .append(formatSampleList(reviewItems, 8)).append(". ");
            }
            if (!lowConfidenceItems.isEmpty()) {
                sb.append("Có ").append(lowConfidenceItems.size()).append(" chỉ tiêu độ tin cậy thấp (< ").append(minConfidence).append("): ")
                        .append(formatSampleList(lowConfidenceItems, 6)).append(". ");
            }
            if (!ocrWarnings.isEmpty()) {
                sb.append("Cảnh báo OCR gốc: ").append(String.join("; ", ocrWarnings)).append(".");
            }

            return ValidationResult.builder()
                    .validationRule(rule)
                    .ruleCodeSnapshot(rule.getRuleCode())
                    .ruleNameSnapshot(rule.getRuleName())
                    .category(rule.getCategory())
                    .severity(rule.getSeverity())
                    .status(ValidationStatus.WARNING)
                    .toleranceValue(minConfidence)
                    .sourcePages(String.join(",", sourcePages))
                    .explanation(sb.toString().trim())
                    .build();
        }

        return ValidationResult.builder()
                .validationRule(rule)
                .ruleCodeSnapshot(rule.getRuleCode())
                .ruleNameSnapshot(rule.getRuleName())
                .category(rule.getCategory())
                .severity(rule.getSeverity())
                .status(ValidationStatus.PASS)
                .toleranceValue(minConfidence)
                .sourcePages(String.join(",", sourcePages))
                .explanation("Dữ liệu OCR đạt tiêu chuẩn chất lượng: 100% chỉ tiêu được ánh xạ chuẩn và độ tin cậy đều trên ngưỡng cho phép.")
                .build();
    }

    private String formatSampleList(List<String> list, int maxItems) {
        if (list.size() <= maxItems) {
            return String.join(", ", list);
        }
        List<String> sub = list.subList(0, maxItems);
        return String.join(", ", sub) + String.format(" (và %d mục khác)", list.size() - maxItems);
    }
}

