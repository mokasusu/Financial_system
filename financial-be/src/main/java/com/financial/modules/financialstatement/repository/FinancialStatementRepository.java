package com.financial.modules.financialstatement.repository;

import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    Optional<FinancialStatement> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT fs FROM FinancialStatement fs WHERE fs.company.id = :companyId " +
           "AND (:fiscalYear IS NULL OR fs.fiscalYear = :fiscalYear) " +
           "AND (:period IS NULL OR fs.period = :period) " +
           "AND (:status IS NULL OR fs.processingStatus = :status)")
    Page<FinancialStatement> findByCompanyIdWithFilters(
            @Param("companyId") Long companyId,
            @Param("fiscalYear") Integer fiscalYear,
            @Param("period") String period,
            @Param("status") FinancialStatementStatus status,
            Pageable pageable);
}

