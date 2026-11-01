package com.financial.modules.financialindicator.service;

import com.financial.modules.financialindicator.dto.AccountingIndicatorMappingResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FinancialIndicatorService {

    // --- Financial Indicator Operations ---

    Page<FinancialIndicatorResponse> getIndicators(FinancialStatementType statementType, Boolean activeOnly, Pageable pageable);

    FinancialIndicatorResponse getIndicatorById(Long id);

    FinancialIndicatorResponse getIndicatorByCode(String code);

    List<FinancialIndicatorResponse> getIndicatorsByStandard(String standard);

    // --- Financial Ratio Operations ---

    Page<FinancialRatioResponse> getRatios(FinancialRatioCategory category, Boolean activeOnly, Pageable pageable);

    FinancialRatioResponse getRatioByCode(String code);

    List<FinancialRatioResponse> getRatiosByCategory(FinancialRatioCategory category);

    List<FinancialRatioResponse> getCoreKpis();

    List<FinancialRatioInputResponse> getRatioInputs(String ratioCode);

    // --- Accounting Mapping Operations ---

    Page<AccountingIndicatorMappingResponse> getMappings(AccountingStandard standard, FinancialStatementType statementType, Pageable pageable);

    List<AccountingIndicatorMappingResponse> getMappingsByStandard(AccountingStandard standard);

    FinancialIndicatorResponse resolveMapping(AccountingStandard standard, FinancialStatementType statementType, String sourceCode, String sourceKey);
}

