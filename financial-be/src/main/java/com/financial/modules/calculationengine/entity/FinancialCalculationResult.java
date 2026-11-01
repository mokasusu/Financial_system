package com.financial.modules.calculationengine.entity;

import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_calculation_results", indexes = {
        @Index(name = "idx_fcr_calc_id", columnList = "financial_calculation_id"),
        @Index(name = "idx_fcr_ratio_code", columnList = "ratio_code"),
        @Index(name = "idx_fcr_is_core_kpi", columnList = "is_core_kpi"),
        @Index(name = "idx_fcr_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialCalculationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_calculation_id", nullable = false)
    private FinancialCalculation financialCalculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratio_definition_id")
    private FinancialRatioDefinition ratioDefinition;

    @Column(name = "ratio_code", nullable = false, length = 50)
    private String ratioCode;

    @Column(name = "ratio_name", nullable = false, length = 255)
    private String ratioName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FinancialRatioCategory category;

    @Column(name = "result_value", precision = 24, scale = 4)
    private BigDecimal resultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 50)
    private FinancialUnit unit;

    @Column(name = "is_core_kpi", nullable = false)
    @Builder.Default
    private Boolean isCoreKpi = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CalculationStatus status;

    @Column(name = "formula_snapshot", length = 255)
    private String formulaSnapshot;

    @Lob
    @Column(name = "input_snapshot", columnDefinition = "TEXT")
    private String inputSnapshot;

    @Column(name = "calculation_note", length = 500)
    private String calculationNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

