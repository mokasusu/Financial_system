package com.financial.modules.assessment.service;

import com.financial.modules.assessment.dto.ScoringResult;
import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;

import java.util.List;

public interface AssessmentScoringService {

    ScoringResult evaluateScoring(List<AssessmentDetail> details, List<AssessmentCategoryScore> categoryScores);
}

