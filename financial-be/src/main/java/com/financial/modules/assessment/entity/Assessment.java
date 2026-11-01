package com.financial.modules.assessment.entity;

import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.AssessmentStatus;
import com.financial.modules.assessment.enums.DataQualityStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.entity.ValidationExecution;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_as_statement_version", columnList = "financial_statement_id, assessment_version"),
        @Index(name = "idx_as_statement_id", columnList = "financial_statement_id"),
        @Index(name = "idx_as_company_id", columnList = "company_id"),
        @Index(name = "idx_as_assessed_at", columnList = "assessed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "assessment_version", nullable = false)
    private Integer assessmentVersion;

    @Column(name = "confirmed_data_version", length = 100)
    private String confirmedDataVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_calculation_id", nullable = false)
    private FinancialCalculation financialCalculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_execution_id")
    private ValidationExecution validationExecution;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_status", nullable = false, length = 30)
    private AssessmentStatus assessmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_status", nullable = false, length = 30)
    private AssessmentScoringStatus scoringStatus;

    @Column(name = "overall_score", precision = 10, scale = 2)
    private BigDecimal overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_classification", nullable = false, length = 30)
    private HealthClassification healthClassification;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_quality_status", nullable = false, length = 30)
    private DataQualityStatus dataQualityStatus;

    @Column(name = "total_ratios", nullable = false)
    @Builder.Default
    private Integer totalRatios = 14;

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

    @Column(name = "assessed_by_user_id")
    private Long assessedByUserId;

    @Column(name = "assessment_notes", length = 1000)
    private String assessmentNotes;

    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AssessmentDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AssessmentCategoryScore> categoryScores = new ArrayList<>();

    public void addDetail(AssessmentDetail detail) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(detail);
        detail.setAssessment(this);
    }

    public void addCategoryScore(AssessmentCategoryScore categoryScore) {
        if (categoryScores == null) {
            categoryScores = new ArrayList<>();
        }
        categoryScores.add(categoryScore);
        categoryScore.setAssessment(this);
    }
}

