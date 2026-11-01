package com.financial.modules.financialstatement.repository;

import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialStatementItemRepository extends JpaRepository<FinancialStatementItem, Long> {

    List<FinancialStatementItem> findByFinancialStatementId(Long financialStatementId);

    List<FinancialStatementItem> findByFinancialStatementIdAndStatementType(Long financialStatementId, FinancialStatementType statementType);
}

