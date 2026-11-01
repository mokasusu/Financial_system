package com.financial.modules.financialindicator.entity;

import com.financial.modules.financialindicator.enums.PeriodReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_ratio_inputs", indexes = {
        @Index(name = "idx_fri_ratio_order", columnList = "ratio_definition_id, input_order"),
        @Index(name = "idx_fri_indicator_id", columnList = "indicator_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRatioInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratio_definition_id", nullable = false)
    private FinancialRatioDefinition ratioDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private FinancialIndicator indicator;

    @Column(name = "input_alias", length = 50)
    private String inputAlias;

    @Column(name = "input_order", nullable = false)
    private Integer inputOrder;

    @Column(name = "required", nullable = false)
    @Builder.Default
    private Boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_reference", nullable = false, length = 50)
    @Builder.Default
    private PeriodReference periodReference = PeriodReference.CURRENT;

    @PrePersist
    protected void onCreate() {
        if (required == null) {
            required = true;
        }
        if (periodReference == null) {
            periodReference = PeriodReference.CURRENT;
        }
    }
}

