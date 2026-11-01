package com.financial.modules.assessment.entity;

import com.financial.modules.assessment.enums.RatioEvaluationStatus;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_details", indexes = {
        @Index(name = "idx_ad_assessment_id", columnList = "assessment_id"),
        @Index(name = "idx_ad_ratio_code", columnList = "ratio_code_snapshot"),
        @Index(name = "idx_ad_category", columnList = "category"),
        @Index(name = "idx_ad_eval_status", columnList = "evaluation_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratio_definition_id")
    private FinancialRatioDefinition ratioDefinition;

    @Column(name = "ratio_code_snapshot", nullable = false, length = 50)
    private String ratioCodeSnapshot;

    @Column(name = "ratio_name_snapshot", nullable = false, length = 255)
    private String ratioNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FinancialRatioCategory category;

    @Column(name = "calculated_value", precision = 24, scale = 4)
    private BigDecimal calculatedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 50)
    private FinancialUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id")
    private EvaluationCriteria criterion;

    @Column(name = "criterion_snapshot", length = 255)
    private String criterionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_operator_snapshot", length = 50)
    private ComparisonOperator comparisonOperatorSnapshot;

    @Column(name = "threshold_value_snapshot", precision = 19, scale = 4)
    private BigDecimal thresholdValueSnapshot;

    @Column(name = "minimum_value_snapshot", precision = 19, scale = 4)
    private BigDecimal minimumValueSnapshot;

    @Column(name = "maximum_value_snapshot", precision = 19, scale = 4)
    private BigDecimal maximumValueSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_status", nullable = false, length = 30)
    private RatioEvaluationStatus evaluationStatus;

    @Column(name = "evaluation_explanation", length = 1000)
    private String evaluationExplanation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

