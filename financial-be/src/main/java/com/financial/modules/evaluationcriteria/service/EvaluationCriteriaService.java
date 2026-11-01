package com.financial.modules.evaluationcriteria.service;

import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaUpdateRequest;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EvaluationCriteriaService {

    Page<EvaluationCriteriaResponse> getCriteria(String ratioCode, Boolean isActive, ComparisonOperator operator, Pageable pageable);

    EvaluationCriteriaResponse getCriteriaById(Long id);

    List<EvaluationCriteriaResponse> getCriteriaByRatioCode(String ratioCode);

    EvaluationCriteriaResponse createCriteria(EvaluationCriteriaRequest request);

    EvaluationCriteriaResponse updateCriteria(Long id, EvaluationCriteriaUpdateRequest request);

    EvaluationCriteriaResponse updateCriteriaStatus(Long id, Boolean isActive);

    void deleteCriteria(Long id);
}

