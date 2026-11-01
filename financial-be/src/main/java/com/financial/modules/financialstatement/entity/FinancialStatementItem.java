package com.financial.modules.financialstatement.entity;

import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_statement_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", nullable = false, length = 30)
    private FinancialStatementType statementType;

    @Column(name = "accounting_standard")
    private String accountingStandard;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "indicator_key")
    private String key;

    @Column(name = "label")
    private String label;

    @Column(name = "raw_label")
    private String rawLabel;

    @Column(name = "current_value", precision = 24, scale = 4)
    private BigDecimal currentValue;

    @Column(name = "previous_value", precision = 24, scale = 4)
    private BigDecimal previousValue;

    @Column(name = "unit")
    private String unit;

    @Column(name = "source_page")
    private Integer sourcePage;

    @Column(name = "confidence", precision = 6, scale = 4)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 30)
    private MappingStatus mappingStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "financialStatementItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private FinancialStatementItemDraft draft;
}

