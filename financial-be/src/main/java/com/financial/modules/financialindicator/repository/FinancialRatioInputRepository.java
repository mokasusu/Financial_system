package com.financial.modules.financialindicator.repository;

import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialRatioInputRepository extends JpaRepository<FinancialRatioInput, Long> {

    List<FinancialRatioInput> findByRatioDefinitionIdOrderByInputOrderAsc(Long ratioDefinitionId);

    List<FinancialRatioInput> findByRatioDefinitionRatioCodeOrderByInputOrderAsc(String ratioCode);

    void deleteByRatioDefinitionId(Long ratioDefinitionId);
}

