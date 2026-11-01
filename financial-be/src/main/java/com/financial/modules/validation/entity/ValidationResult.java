package com.financial.modules.validation.entity;

import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationStatus;
import com.financial.modules.validation.enums.ValidationSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_results", indexes = {
        @Index(name = "idx_vr_execution_id", columnList = "validation_execution_id"),
        @Index(name = "idx_vr_rule_code_snap", columnList = "rule_code_snapshot"),
        @Index(name = "idx_vr_status", columnList = "status"),
        @Index(name = "idx_vr_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_execution_id", nullable = false)
    private ValidationExecution validationExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_rule_id")
    private ValidationRule validationRule;

    @Column(name = "rule_code_snapshot", nullable = false, length = 100)
    private String ruleCodeSnapshot;

    @Column(name = "rule_name_snapshot", nullable = false, length = 255)
    private String ruleNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ValidationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ValidationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private ValidationSeverity severity;

    @Column(name = "expected_value", precision = 24, scale = 4)
    private BigDecimal expectedValue;

    @Column(name = "actual_value", precision = 24, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "difference_value", precision = 24, scale = 4)
    private BigDecimal differenceValue;

    @Column(name = "tolerance_value", precision = 24, scale = 4)
    private BigDecimal toleranceValue;

    @Column(name = "explanation", length = 1000)
    private String explanation;

    @Column(name = "affected_indicators", length = 500)
    private String affectedIndicators;

    @Column(name = "source_pages", length = 100)
    private String sourcePages;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

