package com.financial.modules.calculationengine.service;

import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;

import java.math.BigDecimal;
import java.util.Map;

public interface IndicatorResolverService {

    /**
     * Resolves a numeric balance from confirmed financial statement data for a given indicator and period reference.
     * Returns null if the data is missing or insufficient (no silent default to zero or ending balance).
     */
    BigDecimal resolveValue(FinancialStatement statement, FinancialIndicator indicator, String alias, PeriodReference periodReference);

    /**
     * Resolves a numeric balance using indicator code and period reference.
     */
    BigDecimal resolveValueByCode(FinancialStatement statement, String indicatorCode, PeriodReference periodReference);

    /**
     * Extracts active confirmed values from statement items, taking user-confirmed drafts into account.
     */
    Map<String, BigDecimal> getEffectiveBalancesByItem(FinancialStatementItem item);
}

