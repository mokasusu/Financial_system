package com.financial.modules.evaluationcriteria.service.impl;

import com.financial.common.exception.CriteriaNotFoundException;
import com.financial.common.exception.DuplicateCriteriaException;
import com.financial.common.exception.InvalidCriteriaException;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaRequest;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaResponse;
import com.financial.modules.evaluationcriteria.dto.EvaluationCriteriaUpdateRequest;
import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import com.financial.modules.evaluationcriteria.enums.ComparisonOperator;
import com.financial.modules.evaluationcriteria.mapper.EvaluationCriteriaMapper;
import com.financial.modules.evaluationcriteria.repository.EvaluationCriteriaRepository;
import com.financial.modules.evaluationcriteria.service.EvaluationCriteriaService;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationCriteriaServiceImpl implements EvaluationCriteriaService {

    private final EvaluationCriteriaRepository criteriaRepository;
    private final FinancialRatioDefinitionRepository ratioRepository;
    private final EvaluationCriteriaMapper mapper;

    @Override
    public Page<EvaluationCriteriaResponse> getCriteria(String ratioCode, Boolean isActive, ComparisonOperator operator, Pageable pageable) {
        Specification<EvaluationCriteria> spec = (root, query, cb) -> cb.conjunction();

        if (ratioCode != null && !ratioCode.trim().isEmpty()) {
            String normalizedCode = ratioCode.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("ratioDefinition").get("ratioCode")), normalizedCode));
        }

        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }

        if (operator != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("comparisonOperator"), operator));
        }

        return criteriaRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    public EvaluationCriteriaResponse getCriteriaById(Long id) {
        EvaluationCriteria criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new CriteriaNotFoundException(id));
        return mapper.toResponse(criteria);
    }

    @Override
    public List<EvaluationCriteriaResponse> getCriteriaByRatioCode(String ratioCode) {
        if (ratioCode == null || ratioCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Ratio code must not be empty");
        }
        String normalizedCode = ratioCode.trim().toUpperCase();
        if (!ratioRepository.existsByRatioCode(normalizedCode)) {
            throw RatioNotFoundException.byCode(normalizedCode);
        }

        List<EvaluationCriteria> list = criteriaRepository.findByRatioDefinitionRatioCode(normalizedCode);
        return mapper.toResponseList(list);
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse createCriteria(EvaluationCriteriaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Evaluation criteria request must not be null");
        }

        String normalizedCode = request.getRatioCode().trim().toUpperCase();
        FinancialRatioDefinition ratio = ratioRepository.findByRatioCode(normalizedCode)
                .orElseThrow(() -> RatioNotFoundException.byCode(normalizedCode));

        validateThresholdValues(request.getComparisonOperator(), request.getThresholdValue(), request.getMinimumValue(), request.getMaximumValue());

        boolean isActive = request.getIsActive() == null || request.getIsActive();

        if (isActive && criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrue(ratio.getId())) {
            throw DuplicateCriteriaException.forRatio(normalizedCode);
        }

        BigDecimal threshold = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? null : request.getThresholdValue();
        BigDecimal min = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? request.getMinimumValue() : null;
        BigDecimal max = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? request.getMaximumValue() : null;

        EvaluationCriteria entity = EvaluationCriteria.builder()
                .ratioDefinition(ratio)
                .criterionName(request.getCriterionName().trim())
                .comparisonOperator(request.getComparisonOperator())
                .thresholdValue(threshold)
                .minimumValue(min)
                .maximumValue(max)
                .unit(request.getUnit())
                .description(request.getDescription())
                .isActive(isActive)
                .build();

        EvaluationCriteria saved = criteriaRepository.save(entity);
        log.info("Created evaluation criterion '{}' with ID {} for ratio {}", saved.getCriterionName(), saved.getId(), normalizedCode);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse updateCriteria(Long id, EvaluationCriteriaUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Evaluation criteria update request must not be null");
        }

        EvaluationCriteria criterion = criteriaRepository.findById(id)
                .orElseThrow(() -> new CriteriaNotFoundException(id));

        validateThresholdValues(request.getComparisonOperator(), request.getThresholdValue(), request.getMinimumValue(), request.getMaximumValue());

        boolean newIsActive = request.getIsActive() != null ? request.getIsActive() : criterion.getIsActive();

        if (newIsActive && criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(criterion.getRatioDefinition().getId(), id)) {
            throw DuplicateCriteriaException.forRatio(criterion.getRatioDefinition().getRatioCode());
        }

        BigDecimal threshold = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? null : request.getThresholdValue();
        BigDecimal min = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? request.getMinimumValue() : null;
        BigDecimal max = request.getComparisonOperator() == ComparisonOperator.BETWEEN ? request.getMaximumValue() : null;

        criterion.setCriterionName(request.getCriterionName().trim());
        criterion.setComparisonOperator(request.getComparisonOperator());
        criterion.setThresholdValue(threshold);
        criterion.setMinimumValue(min);
        criterion.setMaximumValue(max);
        criterion.setUnit(request.getUnit());
        criterion.setDescription(request.getDescription());
        criterion.setIsActive(newIsActive);

        EvaluationCriteria updated = criteriaRepository.save(criterion);
        log.info("Updated evaluation criterion ID {}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse updateCriteriaStatus(Long id, Boolean isActive) {
        if (isActive == null) {
            throw new IllegalArgumentException("Active status must not be null");
        }

        EvaluationCriteria criterion = criteriaRepository.findById(id)
                .orElseThrow(() -> new CriteriaNotFoundException(id));

        if (isActive && criteriaRepository.existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(criterion.getRatioDefinition().getId(), id)) {
            throw DuplicateCriteriaException.forRatio(criterion.getRatioDefinition().getRatioCode());
        }

        criterion.setIsActive(isActive);
        EvaluationCriteria updated = criteriaRepository.save(criterion);
        log.info("Updated active status for evaluation criterion ID {} to {}", id, isActive);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCriteria(Long id) {
        EvaluationCriteria criterion = criteriaRepository.findById(id)
                .orElseThrow(() -> new CriteriaNotFoundException(id));

        // Soft deletion by setting inactive to preserve assessment history integrity
        criterion.setIsActive(false);
        criteriaRepository.save(criterion);
        log.info("Deactivated evaluation criterion ID {} on delete request", id);
    }

    private void validateThresholdValues(ComparisonOperator operator, BigDecimal threshold, BigDecimal min, BigDecimal max) {
        if (operator == null) {
            throw new InvalidCriteriaException("Comparison operator must not be null");
        }

        if (operator == ComparisonOperator.BETWEEN) {
            if (min == null || max == null) {
                throw new InvalidCriteriaException("BETWEEN operator requires both minimumValue and maximumValue");
            }
            if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidCriteriaException("Threshold values cannot be negative");
            }
            if (min.compareTo(max) > 0) {
                throw new InvalidCriteriaException("Minimum value must be less than or equal to maximum value");
            }
        } else {
            if (threshold == null) {
                throw new InvalidCriteriaException("Threshold value is required for comparison operator " + operator);
            }
            if (threshold.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidCriteriaException("Threshold value cannot be negative");
            }
        }
    }
}

