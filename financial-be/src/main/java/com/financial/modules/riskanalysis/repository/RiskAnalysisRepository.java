package com.financial.modules.riskanalysis.repository;

import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, Long> {

    List<RiskAnalysis> findByFinancialStatementIdOrderByAnalysisVersionDesc(Long financialStatementId);

    Page<RiskAnalysis> findByFinancialStatementIdOrderByAnalysisVersionDesc(Long financialStatementId, Pageable pageable);

    Optional<RiskAnalysis> findTopByFinancialStatementIdOrderByAnalysisVersionDesc(Long financialStatementId);

    Optional<RiskAnalysis> findByIdAndFinancialStatementId(Long id, Long financialStatementId);

    int countByFinancialStatementId(Long financialStatementId);
}

