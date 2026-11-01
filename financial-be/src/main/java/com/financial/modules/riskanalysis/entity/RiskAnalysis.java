package com.financial.modules.riskanalysis.entity;

import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.validation.entity.ValidationExecution;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_analyses", indexes = {
        @Index(name = "idx_ra_statement_version", columnList = "financial_statement_id, analysis_version"),
        @Index(name = "idx_ra_statement_id", columnList = "financial_statement_id"),
        @Index(name = "idx_ra_company_id", columnList = "company_id"),
        @Index(name = "idx_ra_analyzed_at", columnList = "analyzed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_calculation_id", nullable = false)
    private FinancialCalculation financialCalculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_execution_id")
    private ValidationExecution validationExecution;

    @Column(name = "analysis_version", nullable = false)
    private Integer analysisVersion;

    @Column(name = "confirmed_data_version", length = 100)
    private String confirmedDataVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 30)
    private RiskAnalysisStatus executionStatus;

    @Column(name = "total_risks", nullable = false)
    @Builder.Default
    private Integer totalRisks = 0;

    @Column(name = "low_risks", nullable = false)
    @Builder.Default
    private Integer lowRisks = 0;

    @Column(name = "medium_risks", nullable = false)
    @Builder.Default
    private Integer mediumRisks = 0;

    @Column(name = "high_risks", nullable = false)
    @Builder.Default
    private Integer highRisks = 0;

    @Column(name = "critical_risks", nullable = false)
    @Builder.Default
    private Integer criticalRisks = 0;

    @Column(name = "resolved_risks", nullable = false)
    @Builder.Default
    private Integer resolvedRisks = 0;

    @Column(name = "not_configured_count", nullable = false)
    @Builder.Default
    private Integer notConfiguredCount = 0;

    @Column(name = "insufficient_data_count", nullable = false)
    @Builder.Default
    private Integer insufficientDataCount = 0;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "analysis_notes", length = 1000)
    private String analysisNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "riskAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<IdentifiedRisk> identifiedRisks = new ArrayList<>();

    public void addIdentifiedRisk(IdentifiedRisk risk) {
        if (identifiedRisks == null) {
            identifiedRisks = new ArrayList<>();
        }
        identifiedRisks.add(risk);
        risk.setRiskAnalysis(this);
    }
}

