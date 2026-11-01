package com.financial.modules.financialstatement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_statement_item_drafts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatementItemDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_item_id", nullable = false, unique = true)
    private FinancialStatementItem financialStatementItem;

    @Column(name = "current_value", precision = 24, scale = 4)
    private BigDecimal currentValue;

    @Column(name = "previous_value", precision = 24, scale = 4)
    private BigDecimal previousValue;

    @Column(name = "edited", nullable = false)
    @Builder.Default
    private Boolean edited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

