package com.financial.modules.validation.repository;

import com.financial.modules.validation.entity.ValidationExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationExecutionRepository extends JpaRepository<ValidationExecution, Long> {

    List<ValidationExecution> findByFinancialStatementIdOrderByExecutedAtDesc(Long statementId);

    Optional<ValidationExecution> findTopByFinancialStatementIdOrderByExecutedAtDesc(Long statementId);

    Optional<ValidationExecution> findByIdAndFinancialStatementId(Long id, Long statementId);

    int countByFinancialStatementId(Long statementId);
}

