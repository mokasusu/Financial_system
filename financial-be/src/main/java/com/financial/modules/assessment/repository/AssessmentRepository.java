package com.financial.modules.assessment.repository;

import com.financial.modules.assessment.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByFinancialStatementIdOrderByAssessmentVersionDesc(Long financialStatementId);

    Page<Assessment> findByFinancialStatementIdOrderByAssessmentVersionDesc(Long financialStatementId, Pageable pageable);

    Optional<Assessment> findTopByFinancialStatementIdOrderByAssessmentVersionDesc(Long financialStatementId);

    Optional<Assessment> findByIdAndFinancialStatementId(Long id, Long financialStatementId);

    int countByFinancialStatementId(Long financialStatementId);
}

