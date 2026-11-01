package com.financial.modules.financialindicator.repository;

import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRatioDefinitionRepository extends JpaRepository<FinancialRatioDefinition, Long>, JpaSpecificationExecutor<FinancialRatioDefinition> {

    Optional<FinancialRatioDefinition> findByRatioCode(String ratioCode);

    boolean existsByRatioCode(String ratioCode);

    Page<FinancialRatioDefinition> findByIsActive(Boolean isActive, Pageable pageable);

    Page<FinancialRatioDefinition> findByCategory(FinancialRatioCategory category, Pageable pageable);

    Page<FinancialRatioDefinition> findByCategoryAndIsActive(FinancialRatioCategory category, Boolean isActive, Pageable pageable);

    List<FinancialRatioDefinition> findByCategoryAndIsActiveTrue(FinancialRatioCategory category);

    List<FinancialRatioDefinition> findByIsCoreKpiTrueAndIsActiveTrue();

    List<FinancialRatioDefinition> findByIsCoreKpiTrue();

    List<FinancialRatioDefinition> findByIsActiveTrue();
}

