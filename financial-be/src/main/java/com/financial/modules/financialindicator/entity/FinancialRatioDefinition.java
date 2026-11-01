package com.financial.modules.financialindicator.entity;

import com.financial.modules.financialindicator.enums.EvaluationDirection;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financial_ratio_definitions", indexes = {
        @Index(name = "idx_frd_ratio_code", columnList = "ratio_code", unique = true),
        @Index(name = "idx_frd_category", columnList = "category"),
        @Index(name = "idx_frd_is_core_kpi", columnList = "is_core_kpi"),
        @Index(name = "idx_frd_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRatioDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ratio_code", nullable = false, unique = true, length = 50)
    private String ratioCode;

    @Column(name = "ratio_name", nullable = false, length = 255)
    private String ratioName;

    @Column(name = "ratio_name_en", length = 255)
    private String ratioNameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FinancialRatioCategory category;

    @Column(name = "formula_expression", length = 255)
    private String formulaExpression;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 50)
    private FinancialUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_direction", nullable = false, length = 50)
    private EvaluationDirection evaluationDirection;

    @Column(name = "decimal_scale")
    @Builder.Default
    private Integer decimalScale = 2;

    @Column(name = "is_core_kpi", nullable = false)
    @Builder.Default
    private Boolean isCoreKpi = false;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "ratioDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("inputOrder ASC")
    private List<FinancialRatioInput> inputs = new ArrayList<>();

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
        if (isCoreKpi == null) {
            isCoreKpi = false;
        }
        if (decimalScale == null) {
            decimalScale = 2;
        }
    }

    public void addInput(FinancialRatioInput input) {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        inputs.add(input);
        input.setRatioDefinition(this);
    }
}

