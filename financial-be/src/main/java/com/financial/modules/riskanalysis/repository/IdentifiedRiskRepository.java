package com.financial.modules.riskanalysis.repository;

import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentifiedRiskRepository extends JpaRepository<IdentifiedRisk, Long>, JpaSpecificationExecutor<IdentifiedRisk> {

    List<IdentifiedRisk> findByRiskAnalysisIdOrderByIdAsc(Long riskAnalysisId);

    List<IdentifiedRisk> findByRiskAnalysisIdAndRiskCategoryOrderByIdAsc(Long riskAnalysisId, RiskCategory category);

    List<IdentifiedRisk> findByRiskAnalysisIdAndSeverityOrderByIdAsc(Long riskAnalysisId, RiskSeverity severity);

    List<IdentifiedRisk> findByRiskAnalysisIdAndRiskStatusOrderByIdAsc(Long riskAnalysisId, RiskStatus status);
}

