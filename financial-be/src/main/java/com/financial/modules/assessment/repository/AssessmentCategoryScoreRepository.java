package com.financial.modules.assessment.repository;

import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentCategoryScoreRepository extends JpaRepository<AssessmentCategoryScore, Long> {

    List<AssessmentCategoryScore> findByAssessmentIdOrderByIdAsc(Long assessmentId);
}

