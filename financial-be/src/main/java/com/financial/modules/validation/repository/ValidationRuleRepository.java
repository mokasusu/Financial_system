package com.financial.modules.validation.repository;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.validation.entity.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long>, JpaSpecificationExecutor<ValidationRule> {

    Optional<ValidationRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    boolean existsByRuleCodeAndIdNot(String ruleCode, Long id);

    List<ValidationRule> findByIsActiveTrue();

    @Query("SELECT r FROM ValidationRule r WHERE r.isActive = true AND (r.accountingStandard IS NULL OR r.accountingStandard = :standard)")
    List<ValidationRule> findApplicableRules(@Param("standard") AccountingStandard standard);
}

