package com.financial.modules.calculationengine.entity;

import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financial_calculations", indexes = {
        @Index(name = "idx_fc_statement_version", columnList = "financial_statement_id, calculation_version"),
        @Index(name = "idx_fc_statement_id", columnList = "financial_statement_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @Column(name = "calculation_version", nullable = false)
    private Integer calculationVersion;

    @Column(name = "confirmed_data_version", length = 100)
    private String confirmedDataVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CalculationStatus status;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "financialCalculation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FinancialCalculationResult> results = new ArrayList<>();

    public void addResult(FinancialCalculationResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        result.setFinancialCalculation(this);
    }
}

