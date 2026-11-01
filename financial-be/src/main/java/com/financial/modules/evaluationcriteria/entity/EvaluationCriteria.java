package com.financial.modules.evaluationcriteria.entity;

import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_criteria", indexes = {
        @Index(name = "idx_ec_ratio_id", columnList = "ratio_definition_id"),
        @Index(name = "idx_ec_is_active", columnList = "is_active"),
        @Index(name = "idx_ec_operator", columnList = "comparison_operator")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratio_definition_id", nullable = false)
    private FinancialRatioDefinition ratioDefinition;

    @Column(name = "criterion_name", nullable = false, length = 255)
    private String criterionName;

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
    @Column(name = "unit", nullable = false, length = 50)
    private FinancialUnit unit;

    @Column(name = "description", length = 500)
    private String description;

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

