package com.financial.modules.aianalysis.repository;

import com.financial.modules.aianalysis.entity.AIAnalysisStrength;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisStrengthRepository extends JpaRepository<AIAnalysisStrength, Long> {
    List<AIAnalysisStrength> findByAiAnalysisId(Long aiAnalysisId);
}

