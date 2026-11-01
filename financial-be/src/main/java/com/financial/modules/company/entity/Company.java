package com.financial.modules.company.entity;

import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.company.enums.CompanyType;
import com.financial.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_companies_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_companies_tax_code", columnNames = "tax_code")
        },
        indexes = {
                @Index(name = "idx_companies_user_id", columnList = "user_id"),
                @Index(name = "idx_companies_tax_code", columnList = "tax_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "tax_code", nullable = false, unique = true, length = 20)
    private String taxCode;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_name_en", length = 255)
    private String companyNameEn;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false, length = 50)
    private CompanyType companyType;

    @Column(name = "main_business_line", nullable = false, length = 255)
    private String mainBusinessLine;

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @Column(name = "legal_representative", length = 255)
    private String legalRepresentative;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "ward", nullable = false, length = 100)
    private String ward;

    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = CompanyStatus.ACTIVE;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
}

