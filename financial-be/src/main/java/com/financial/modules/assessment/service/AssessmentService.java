package com.financial.modules.assessment.service;

import com.financial.modules.assessment.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssessmentService {

    AssessmentResponse createAssessment(Long userId, Long statementId, AssessmentRequest request);

    Page<AssessmentHistoryResponse> getAssessmentHistory(Long userId, Long statementId, Pageable pageable);

    AssessmentResponse getLatestAssessment(Long userId, Long statementId);

    AssessmentResponse getAssessmentById(Long userId, Long statementId, Long assessmentId);

    List<AssessmentDetailResponse> getAssessmentDetails(Long userId, Long statementId, Long assessmentId);

    List<AssessmentCategoryResponse> getAssessmentCategories(Long userId, Long statementId, Long assessmentId);

    AssessmentSummaryResponse getAssessmentSummary(Long userId, Long statementId, Long assessmentId);
}

