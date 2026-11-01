package com.financial.modules.assessment.repository;

import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentDetailRepository extends JpaRepository<AssessmentDetail, Long> {

    List<AssessmentDetail> findByAssessmentIdOrderByIdAsc(Long assessmentId);

    List<AssessmentDetail> findByAssessmentIdAndCategoryOrderByIdAsc(Long assessmentId, FinancialRatioCategory category);
}

