package com.financial.modules.riskanalysis.entity;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_rules", indexes = {
        @Index(name = "idx_rr_rule_code", columnList = "rule_code", unique = true),
        @Index(name = "idx_rr_category", columnList = "risk_category"),
        @Index(name = "idx_rr_ratio_code", columnList = "ratio_code"),
        @Index(name = "idx_rr_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", nullable = false, length = 50)
    private RiskCategory riskCategory;

    @Column(name = "ratio_code", length = 50)
    private String ratioCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_operator", nullable = false, length = 50)
    private ComparisonOperator comparisonOperator;

    @Column(name = "threshold_value", precision = 19, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "minimum_value", precision = 19, scale = 4)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", precision = 19, scale = 4)
    private BigDecimal maximumValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private RiskSeverity severity;

    @Lob
    @Column(name = "explanation_template", columnDefinition = "TEXT")
    private String explanationTemplate;

    @Lob
    @Column(name = "recommendation_template", columnDefinition = "TEXT")
    private String recommendationTemplate;

    @Lob
    @Column(name = "potential_impact_template", columnDefinition = "TEXT")
    private String potentialImpactTemplate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }
}

