package com.financial.modules.aianalysis.repository;

import com.financial.modules.aianalysis.entity.AIAnalysisRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisRiskRepository extends JpaRepository<AIAnalysisRisk, Long> {
    List<AIAnalysisRisk> findByAiAnalysisId(Long aiAnalysisId);
}

