package com.financial.modules.assessment.service;

import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;

import java.util.List;

public interface CategoryAggregationService {

    List<AssessmentCategoryScore> aggregateCategories(List<AssessmentDetail> details);
}

