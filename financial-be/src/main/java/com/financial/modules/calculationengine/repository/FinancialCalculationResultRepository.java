package com.financial.modules.calculationengine.repository;

import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialCalculationResultRepository extends JpaRepository<FinancialCalculationResult, Long> {

    List<FinancialCalculationResult> findByFinancialCalculationId(Long calculationId);

    List<FinancialCalculationResult> findByFinancialCalculationIdAndIsCoreKpiTrue(Long calculationId);

    Optional<FinancialCalculationResult> findByFinancialCalculationIdAndRatioCode(Long calculationId, String ratioCode);
}

