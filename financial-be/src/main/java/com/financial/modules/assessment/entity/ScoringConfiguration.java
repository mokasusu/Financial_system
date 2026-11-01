package com.financial.modules.assessment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scoring_configurations", indexes = {
        @Index(name = "idx_sc_is_active", columnList = "is_active"),
        @Index(name = "idx_sc_is_approved", columnList = "is_approved")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_name", nullable = false, unique = true, length = 100)
    private String configName;

    @Column(name = "description", length = 500)
    private String description;

    @Lob
    @Column(name = "ratio_weights_json", columnDefinition = "TEXT")
    private String ratioWeightsJson;

    @Lob
    @Column(name = "category_weights_json", columnDefinition = "TEXT")
    private String categoryWeightsJson;

    @Column(name = "score_scale", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal scoreScale = new BigDecimal("100.00");

    @Lob
    @Column(name = "classification_rules_json", columnDefinition = "TEXT")
    private String classificationRulesJson;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

