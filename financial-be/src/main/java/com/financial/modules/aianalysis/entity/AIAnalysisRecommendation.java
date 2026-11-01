package com.financial.modules.aianalysis.entity;

import com.financial.modules.aianalysis.enums.RecommendationPriority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis_recommendations", indexes = {
        @Index(name = "idx_aiarc_analysis_id", columnList = "ai_analysis_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_analysis_id", nullable = false)
    private AIAnalysis aiAnalysis;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private RecommendationPriority priority;

    @Column(name = "related_risk", length = 255)
    private String relatedRisk;

    @Column(name = "related_risk_id", length = 255)
    private String relatedRiskId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

