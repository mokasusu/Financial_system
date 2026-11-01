package com.financial.modules.financialindicator.repository;

import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialIndicatorRepository extends JpaRepository<FinancialIndicator, Long>, JpaSpecificationExecutor<FinancialIndicator> {

    Optional<FinancialIndicator> findByIndicatorCode(String indicatorCode);

    boolean existsByIndicatorCode(String indicatorCode);

    Page<FinancialIndicator> findByIsActive(Boolean isActive, Pageable pageable);

    Page<FinancialIndicator> findByStatementType(FinancialStatementType statementType, Pageable pageable);

    Page<FinancialIndicator> findByStatementTypeAndIsActive(FinancialStatementType statementType, Boolean isActive, Pageable pageable);

    List<FinancialIndicator> findByIsActiveTrue();

    @Query("SELECT DISTINCT m.indicator FROM AccountingIndicatorMapping m WHERE m.accountingStandard = :standard AND m.mappingStatus = 'ACTIVE'")
    List<FinancialIndicator> findDistinctByAccountingStandard(@Param("standard") AccountingStandard standard);
}

