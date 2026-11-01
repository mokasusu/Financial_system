package com.financial.modules.riskanalysis.repository;

import com.financial.modules.riskanalysis.entity.RiskRule;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, Long>, JpaSpecificationExecutor<RiskRule> {

    Optional<RiskRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    boolean existsByRuleCodeAndIdNot(String ruleCode, Long id);

    List<RiskRule> findByIsActiveTrue();

    List<RiskRule> findByRiskCategoryAndIsActiveTrue(RiskCategory riskCategory);

    List<RiskRule> findByRatioCodeAndIsActiveTrue(String ratioCode);
}

