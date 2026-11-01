package com.financial.modules.financialstatement.entity;

import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financial_statements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "accounting_standard")
    private String accountingStandard;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "period")
    private String period;

    @Column(name = "as_of_date")
    private LocalDate asOfDate;

    @Column(name = "current_period_label")
    private String currentPeriodLabel;

    @Column(name = "previous_period_label")
    private String previousPeriodLabel;

    @Column(name = "unit")
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 30)
    private FinancialStatementStatus processingStatus;

    @Column(name = "ocr_request_id")
    private String ocrRequestId;

    @Lob
    @Column(name = "original_ocr_response", columnDefinition = "LONGTEXT")
    private String originalOcrResponse;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "financialStatement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FinancialStatementItem> items = new ArrayList<>();
}

