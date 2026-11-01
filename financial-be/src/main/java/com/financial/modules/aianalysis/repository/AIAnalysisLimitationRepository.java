package com.financial.modules.aianalysis.repository;

import com.financial.modules.aianalysis.entity.AIAnalysisLimitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIAnalysisLimitationRepository extends JpaRepository<AIAnalysisLimitation, Long> {
    List<AIAnalysisLimitation> findByAiAnalysisId(Long aiAnalysisId);
}

