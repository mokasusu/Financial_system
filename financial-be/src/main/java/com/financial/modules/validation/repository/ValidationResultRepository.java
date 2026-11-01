package com.financial.modules.validation.repository;

import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResult, Long> {

    List<ValidationResult> findByValidationExecutionId(Long executionId);

    List<ValidationResult> findByValidationExecutionIdAndStatus(Long executionId, ValidationStatus status);

    List<ValidationResult> findByValidationExecutionIdAndCategory(Long executionId, ValidationCategory category);
}

