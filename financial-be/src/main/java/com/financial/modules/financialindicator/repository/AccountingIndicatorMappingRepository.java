package com.financial.modules.financialindicator.repository;

import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.MappingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingIndicatorMappingRepository extends JpaRepository<AccountingIndicatorMapping, Long>, JpaSpecificationExecutor<AccountingIndicatorMapping> {

    List<AccountingIndicatorMapping> findByAccountingStandard(AccountingStandard standard);

    List<AccountingIndicatorMapping> findByAccountingStandardAndMappingStatus(AccountingStandard standard, MappingStatus status);

    Page<AccountingIndicatorMapping> findByAccountingStandard(AccountingStandard standard, Pageable pageable);

    Page<AccountingIndicatorMapping> findByAccountingStandardAndStatementType(AccountingStandard standard, FinancialStatementType statementType, Pageable pageable);

    Optional<AccountingIndicatorMapping> findFirstByAccountingStandardAndStatementTypeAndSourceCodeAndMappingStatus(
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceCode,
            MappingStatus status
    );

    Optional<AccountingIndicatorMapping> findFirstByAccountingStandardAndStatementTypeAndSourceKeyAndMappingStatus(
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceKey,
            MappingStatus status
    );

    boolean existsByAccountingStandardAndStatementTypeAndSourceCode(
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceCode
    );

    boolean existsByAccountingStandardAndStatementTypeAndSourceKey(
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceKey
    );
}

