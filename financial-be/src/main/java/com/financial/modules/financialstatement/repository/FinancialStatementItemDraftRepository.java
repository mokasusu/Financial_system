package com.financial.modules.financialstatement.repository;

import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialStatementItemDraftRepository extends JpaRepository<FinancialStatementItemDraft, Long> {

    Optional<FinancialStatementItemDraft> findByFinancialStatementItemId(Long financialStatementItemId);

    List<FinancialStatementItemDraft> findByFinancialStatementItemFinancialStatementId(Long statementId);
}

