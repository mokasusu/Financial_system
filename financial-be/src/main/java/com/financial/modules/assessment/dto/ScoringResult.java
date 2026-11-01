package com.financial.modules.assessment.dto;

import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringResult {

    private AssessmentScoringStatus scoringStatus;
    private BigDecimal overallScore;
    private HealthClassification healthClassification;
    private String scoringNotes;

    public static ScoringResult notConfigured() {
        return ScoringResult.builder()
                .scoringStatus(AssessmentScoringStatus.NOT_CONFIGURED)
                .overallScore(null)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .scoringNotes("Chưa có cấu hình tính điểm và thang phân loại sức khỏe tài chính được phê duyệt.")
                .build();
    }

    public static ScoringResult partial(String reason) {
        return ScoringResult.builder()
                .scoringStatus(AssessmentScoringStatus.PARTIAL)
                .overallScore(null)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .scoringNotes(reason != null ? reason : "Cấu hình tính điểm chưa hoàn thiện.")
                .build();
    }

    public static ScoringResult insufficientData() {
        return ScoringResult.builder()
                .scoringStatus(AssessmentScoringStatus.INSUFFICIENT_DATA)
                .overallScore(null)
                .healthClassification(HealthClassification.NOT_CONFIGURED)
                .scoringNotes("Không đủ dữ liệu tỷ số tài chính để chấm điểm.")
                .build();
    }
}

