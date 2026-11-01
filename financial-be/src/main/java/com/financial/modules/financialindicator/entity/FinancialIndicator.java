package com.financial.modules.financialindicator.entity;

import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "financial_indicators", indexes = {
        @Index(name = "idx_fi_indicator_code", columnList = "indicator_code", unique = true),
        @Index(name = "idx_fi_statement_type", columnList = "statement_type"),
        @Index(name = "idx_fi_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "indicator_code", nullable = false, unique = true, length = 50)
    private String indicatorCode;

    @Column(name = "indicator_name", nullable = false, length = 255)
    private String indicatorName;

    @Column(name = "indicator_name_en", length = 255)
    private String indicatorNameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", nullable = false, length = 50)
    private FinancialStatementType statementType;

    @Column(name = "data_type", length = 50)
    @Builder.Default
    private String dataType = "DECIMAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 50)
    @Builder.Default
    private FinancialUnit unit = FinancialUnit.VND;

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
        if (dataType == null) {
            dataType = "DECIMAL";
        }
        if (unit == null) {
            unit = FinancialUnit.VND;
        }
    }
}

