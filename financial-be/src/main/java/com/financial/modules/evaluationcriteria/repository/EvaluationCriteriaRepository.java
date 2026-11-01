package com.financial.modules.evaluationcriteria.repository;

import com.financial.modules.evaluationcriteria.entity.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long>, JpaSpecificationExecutor<EvaluationCriteria> {

    List<EvaluationCriteria> findByRatioDefinitionRatioCode(String ratioCode);

    List<EvaluationCriteria> findByRatioDefinitionRatioCodeAndIsActiveTrue(String ratioCode);

    List<EvaluationCriteria> findByRatioDefinitionId(Long ratioDefinitionId);

    Optional<EvaluationCriteria> findFirstByRatioDefinitionIdAndIsActiveTrue(Long ratioDefinitionId);

    boolean existsByRatioDefinitionIdAndIsActiveTrue(Long ratioDefinitionId);

    boolean existsByRatioDefinitionIdAndIsActiveTrueAndIdNot(Long ratioDefinitionId, Long id);

    boolean existsByRatioDefinitionRatioCode(String ratioCode);
}

