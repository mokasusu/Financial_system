package com.financial.modules.aianalysis.repository;

import com.financial.modules.aianalysis.entity.AIAnalysisRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisRecommendationRepository extends JpaRepository<AIAnalysisRecommendation, Long> {
    List<AIAnalysisRecommendation> findByAiAnalysisId(Long aiAnalysisId);
}

