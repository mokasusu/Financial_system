package com.financial.modules.validation.entity;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.enums.ValidationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "validation_executions", indexes = {
        @Index(name = "idx_ve_statement_id", columnList = "financial_statement_id"),
        @Index(name = "idx_ve_executed_at", columnList = "executed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @Column(name = "confirmed_data_version", length = 100)
    private String confirmedDataVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_standard", length = 30)
    private AccountingStandard accountingStandard;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 30)
    private ValidationStatus executionStatus;

    @Column(name = "total_rules", nullable = false)
    @Builder.Default
    private Integer totalRules = 0;

    @Column(name = "passed_rules", nullable = false)
    @Builder.Default
    private Integer passedRules = 0;

    @Column(name = "warning_rules", nullable = false)
    @Builder.Default
    private Integer warningRules = 0;

    @Column(name = "failed_rules", nullable = false)
    @Builder.Default
    private Integer failedRules = 0;

    @Column(name = "review_required_rules", nullable = false)
    @Builder.Default
    private Integer reviewRequiredRules = 0;

    @Column(name = "error_rules", nullable = false)
    @Builder.Default
    private Integer errorRules = 0;

    @Column(name = "not_applicable_rules", nullable = false)
    @Builder.Default
    private Integer notApplicableRules = 0;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "validationExecution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ValidationResult> results = new ArrayList<>();

    public void addResult(ValidationResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        result.setValidationExecution(this);
    }
}

