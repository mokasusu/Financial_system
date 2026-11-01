package com.financial.modules.assessment.entity;

import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_category_scores", indexes = {
        @Index(name = "idx_acs_assessment_id", columnList = "assessment_id"),
        @Index(name = "idx_acs_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentCategoryScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FinancialRatioCategory category;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "total_ratios", nullable = false)
    @Builder.Default
    private Integer totalRatios = 0;

    @Column(name = "passed_ratios", nullable = false)
    @Builder.Default
    private Integer passedRatios = 0;

    @Column(name = "failed_ratios", nullable = false)
    @Builder.Default
    private Integer failedRatios = 0;

    @Column(name = "insufficient_data_ratios", nullable = false)
    @Builder.Default
    private Integer insufficientDataRatios = 0;

    @Column(name = "not_configured_ratios", nullable = false)
    @Builder.Default
    private Integer notConfiguredRatios = 0;

    @Column(name = "category_score", precision = 10, scale = 2)
    private BigDecimal categoryScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_status", nullable = false, length = 30)
    private AssessmentScoringStatus scoringStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

