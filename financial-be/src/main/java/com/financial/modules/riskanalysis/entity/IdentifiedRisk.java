package com.financial.modules.riskanalysis.entity;

import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "identified_risks", indexes = {
        @Index(name = "idx_ir_analysis_id", columnList = "risk_analysis_id"),
        @Index(name = "idx_ir_category", columnList = "risk_category"),
        @Index(name = "idx_ir_severity", columnList = "severity"),
        @Index(name = "idx_ir_status", columnList = "risk_status"),
        @Index(name = "idx_ir_ratio_code", columnList = "related_ratio_code")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifiedRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_analysis_id", nullable = false)
    private RiskAnalysis riskAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_rule_id")
    private RiskRule riskRule;

    @Column(name = "rule_code_snapshot", length = 100)
    private String ruleCodeSnapshot;

    @Column(name = "risk_title", nullable = false, length = 255)
    private String riskTitle;

    @Lob
    @Column(name = "risk_description", columnDefinition = "TEXT")
    private String riskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false, length = 50)
    private RiskCategory riskCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 30)
    private RiskSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_status", nullable = false, length = 30)
    private RiskStatus riskStatus;

    @Column(name = "related_ratio_code", length = 50)
    private String relatedRatioCode;

    @Column(name = "actual_value", precision = 24, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "threshold_value", precision = 19, scale = 4)
    private BigDecimal thresholdValue;

    @Lob
    @Column(name = "potential_impact", columnDefinition = "TEXT")
    private String potentialImpact;

    @Lob
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

