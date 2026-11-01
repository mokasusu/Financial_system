package com.financial.modules.calculationengine.repository;

import com.financial.modules.calculationengine.entity.FinancialCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialCalculationRepository extends JpaRepository<FinancialCalculation, Long> {

    List<FinancialCalculation> findByFinancialStatementIdOrderByCalculationVersionDesc(Long financialStatementId);

    Optional<FinancialCalculation> findTopByFinancialStatementIdOrderByCalculationVersionDesc(Long financialStatementId);

    Optional<FinancialCalculation> findByIdAndFinancialStatementId(Long id, Long financialStatementId);

    int countByFinancialStatementId(Long financialStatementId);
}

