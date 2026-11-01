package com.financial.modules.aianalysis.repository;

import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.enums.AIAnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, Long> {

    Optional<AIAnalysis> findTopByAssessmentIdOrderByAnalysisVersionDesc(Long assessmentId);

    Page<AIAnalysis> findByAssessmentIdOrderByAnalysisVersionDesc(Long assessmentId, Pageable pageable);

    List<AIAnalysis> findByAssessmentIdOrderByAnalysisVersionDesc(Long assessmentId);

    Optional<AIAnalysis> findByIdAndCompanyId(Long id, Long companyId);

    Optional<AIAnalysis> findFirstByAssessmentIdAndStatusIn(Long assessmentId, Collection<AIAnalysisStatus> statuses);

    List<AIAnalysis> findByFinancialStatementId(Long financialStatementId);

    int countByAssessmentId(Long assessmentId);
}

