package com.financial.modules.financialindicator.entity;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.MappingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "accounting_indicator_mappings", indexes = {
        @Index(name = "idx_aim_lookup_code", columnList = "accounting_standard, statement_type, source_code"),
        @Index(name = "idx_aim_lookup_key", columnList = "accounting_standard, statement_type, source_key"),
        @Index(name = "idx_aim_standard_status", columnList = "accounting_standard, mapping_status"),
        @Index(name = "idx_aim_indicator_id", columnList = "indicator_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingIndicatorMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private FinancialIndicator indicator;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_standard", nullable = false, length = 50)
    private AccountingStandard accountingStandard;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", nullable = false, length = 50)
    private FinancialStatementType statementType;

    @Column(name = "source_code", length = 50)
    private String sourceCode;

    @Column(name = "source_key", length = 100)
    private String sourceKey;

    @Column(name = "source_label", length = 255)
    private String sourceLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", nullable = false, length = 50)
    @Builder.Default
    private MappingStatus mappingStatus = MappingStatus.ACTIVE;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @PrePersist
    protected void onCreate() {
        if (mappingStatus == null) {
            mappingStatus = MappingStatus.ACTIVE;
        }
    }
}

