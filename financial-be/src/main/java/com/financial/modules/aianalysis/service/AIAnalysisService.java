package com.financial.modules.aianalysis.service;

import com.financial.modules.aianalysis.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AIAnalysisService {

    AIAnalysisResponse generateAIAnalysis(Long userId, Long assessmentId, AIAnalysisGenerateRequest request);

    AIAnalysisResponse generateAIAnalysisForStatement(Long userId, Long statementId, AIAnalysisGenerateRequest request);

    AIAnalysisResponse getLatestAIAnalysis(Long userId, Long assessmentId);

    AIAnalysisResponse getAIAnalysisById(Long userId, Long analysisId);

    Page<AIAnalysisHistoryResponse> getAIAnalysisHistory(Long userId, Long assessmentId, Pageable pageable);

    AIAnalysisResponse updateFinalComment(Long userId, Long analysisId, AIAnalysisFinalCommentRequest request);

    AIAnalysisResponse updateFinalCommentForStatement(Long userId, Long statementId, AIAnalysisFinalCommentRequest request);

    AIAnalysisResponse approveAIAnalysis(Long userId, Long analysisId);

    AIAnalysisResponse rejectAIAnalysis(Long userId, Long analysisId);
}

