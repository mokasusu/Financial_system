package com.financial.modules.aianalysis.entity;

import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import com.financial.modules.aianalysis.enums.AIReviewStatus;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.validation.entity.ValidationExecution;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_analyses", indexes = {
        @Index(name = "idx_aia_assessment_version", columnList = "assessment_id, analysis_version"),
        @Index(name = "idx_aia_assessment_id", columnList = "assessment_id"),
        @Index(name = "idx_aia_statement_id", columnList = "financial_statement_id"),
        @Index(name = "idx_aia_company_id", columnList = "company_id"),
        @Index(name = "idx_aia_status", columnList = "status"),
        @Index(name = "idx_aia_review_status", columnList = "review_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_calculation_id")
    private FinancialCalculation financialCalculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_execution_id")
    private ValidationExecution validationExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_analysis_id")
    private RiskAnalysis riskAnalysis;

    @Column(name = "analysis_version", nullable = false)
    @Builder.Default
    private Integer analysisVersion = 1;

    @Column(name = "confirmed_data_version", length = 100)
    private String confirmedDataVersion;

    @Column(name = "provider", length = 100)
    @Builder.Default
    private String provider = "OPENROUTER";

    @Column(name = "model", length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AIAnalysisStatus status = AIAnalysisStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    @Builder.Default
    private AIReviewStatus reviewStatus = AIReviewStatus.PENDING;

    @Lob
    @Column(name = "executive_summary", columnDefinition = "TEXT")
    private String executiveSummary;

    @Lob
    @Column(name = "ai_generated_comment", columnDefinition = "LONGTEXT")
    private String aiGeneratedComment;

    @Lob
    @Column(name = "final_comment", columnDefinition = "LONGTEXT")
    private String finalComment;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Lob
    @Column(name = "raw_provider_response", columnDefinition = "LONGTEXT")
    private String rawProviderResponse;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "aiAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AIAnalysisStrength> strengths = new ArrayList<>();

    @OneToMany(mappedBy = "aiAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AIAnalysisRisk> risks = new ArrayList<>();

    @OneToMany(mappedBy = "aiAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AIAnalysisRecommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "aiAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AIAnalysisLimitation> limitations = new ArrayList<>();

    public void addStrength(AIAnalysisStrength strength) {
        if (strengths == null) {
            strengths = new ArrayList<>();
        }
        strengths.add(strength);
        strength.setAiAnalysis(this);
    }

    public void addRisk(AIAnalysisRisk risk) {
        if (risks == null) {
            risks = new ArrayList<>();
        }
        risks.add(risk);
        risk.setAiAnalysis(this);
    }

    public void addRecommendation(AIAnalysisRecommendation recommendation) {
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }
        recommendations.add(recommendation);
        recommendation.setAiAnalysis(this);
    }

    public void addLimitation(AIAnalysisLimitation limitation) {
        if (limitations == null) {
            limitations = new ArrayList<>();
        }
        limitations.add(limitation);
        limitation.setAiAnalysis(this);
    }
}

