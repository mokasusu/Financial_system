package com.financial.modules.validation.entity;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_rules", indexes = {
        @Index(name = "idx_vr_rule_code", columnList = "rule_code", unique = true),
        @Index(name = "idx_vr_standard_cat", columnList = "accounting_standard, category"),
        @Index(name = "idx_vr_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_standard", length = 30)
    private AccountingStandard accountingStandard;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", length = 30)
    private FinancialStatementType statementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ValidationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private ValidationSeverity severity;

    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;

    @Column(name = "tolerance_value", precision = 24, scale = 4)
    @Builder.Default
    private BigDecimal toleranceValue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tolerance_type", length = 30)
    @Builder.Default
    private ToleranceType toleranceType = ToleranceType.ABSOLUTE;

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
        if (isActive == null) isActive = true;
        if (toleranceValue == null) toleranceValue = BigDecimal.ZERO;
        if (toleranceType == null) toleranceType = ToleranceType.ABSOLUTE;
    }
}

