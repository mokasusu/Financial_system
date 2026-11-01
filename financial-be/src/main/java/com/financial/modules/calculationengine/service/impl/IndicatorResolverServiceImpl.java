package com.financial.modules.calculationengine.service.impl;

import com.financial.modules.calculationengine.service.IndicatorResolverService;
import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.MappingStatus;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorResolverServiceImpl implements IndicatorResolverService {

    private final AccountingIndicatorMappingRepository mappingRepository;
    private final FinancialStatementItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveValue(FinancialStatement statement, FinancialIndicator indicator, String alias, PeriodReference periodReference) {
        String code = indicator != null ? indicator.getIndicatorCode() : null;
        if (code == null && alias != null) {
            code = alias;
        }
        if (code == null) {
            log.warn("Cannot resolve indicator value: both indicator and alias are null");
            return null;
        }

        return resolveValueByCode(statement, code, periodReference);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resolveValueByCode(FinancialStatement statement, String indicatorCode, PeriodReference periodReference) {
        if (statement == null || indicatorCode == null) {
            return null;
        }

        List<FinancialStatementItem> items = statement.getItems();
        if (items == null || items.isEmpty()) {
            items = itemRepository.findByFinancialStatementId(statement.getId());
        }
        if (items == null || items.isEmpty()) {
            log.warn("No items found for financial statement ID {}", statement.getId());
            return null;
        }

        FinancialStatementItem matchedItem = findMatchingItem(statement, items, indicatorCode);
        if (matchedItem == null) {
            log.debug("No statement item matched indicator code '{}' for statement ID {}", indicatorCode, statement.getId());
            return null;
        }

        return extractBalanceByPeriod(matchedItem, indicatorCode, periodReference);
    }

    @Override
    public Map<String, BigDecimal> getEffectiveBalancesByItem(FinancialStatementItem item) {
        if (item == null) {
            return Collections.emptyMap();
        }

        FinancialStatementItemDraft draft = item.getDraft();
        BigDecimal current = (draft != null && draft.getCurrentValue() != null)
                ? draft.getCurrentValue()
                : item.getCurrentValue();

        BigDecimal previous = (draft != null && draft.getPreviousValue() != null)
                ? draft.getPreviousValue()
                : item.getPreviousValue();

        Map<String, BigDecimal> balances = new HashMap<>();
        if (current != null) balances.put("current", current);
        if (previous != null) balances.put("previous", previous);
        return balances;
    }

    private BigDecimal extractBalanceByPeriod(FinancialStatementItem item, String indicatorCode, PeriodReference periodReference) {
        Map<String, BigDecimal> balances = getEffectiveBalancesByItem(item);
        BigDecimal current = balances.get("current");
        BigDecimal previous = balances.get("previous");

        // Specific suffix heuristics if indicatorCode explicitly denotes beginning/ending
        String upperCode = indicatorCode.toUpperCase();
        if (upperCode.endsWith("_BEGINNING")) {
            return previous;
        }
        if (upperCode.endsWith("_ENDING")) {
            return current;
        }

        PeriodReference ref = periodReference != null ? periodReference : PeriodReference.CURRENT;
        return switch (ref) {
            case CURRENT, ENDING -> current;
            case BEGINNING -> previous;
            case AVERAGE -> {
                // Rule: If beginning or ending balance is missing, return null (do not substitute ending balance or assume zero)
                if (current == null || previous == null) {
                    yield null;
                }
                yield previous.add(current).divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
            }
        };
    }

    private FinancialStatementItem findMatchingItem(FinancialStatement statement, List<FinancialStatementItem> items, String indicatorCode) {
        String target = indicatorCode.trim().toUpperCase();

        // 1. Direct key match
        for (FinancialStatementItem item : items) {
            if (item.getKey() != null && item.getKey().equalsIgnoreCase(target)) {
                return item;
            }
        }

        // 2. Database accounting indicator mapping lookup
        AccountingStandard standard = parseAccountingStandard(statement.getAccountingStandard());
        if (standard != null && mappingRepository != null) {
            List<AccountingIndicatorMapping> mappings = mappingRepository.findByAccountingStandardAndMappingStatus(standard, MappingStatus.ACTIVE);
            for (AccountingIndicatorMapping m : mappings) {
                if (m.getIndicator() != null && m.getIndicator().getIndicatorCode().equalsIgnoreCase(target)) {
                    for (FinancialStatementItem item : items) {
                        if (m.getSourceCode() != null && m.getSourceCode().equals(item.getCode())) {
                            return item;
                        }
                        if (m.getSourceKey() != null && m.getSourceKey().equalsIgnoreCase(item.getKey())) {
                            return item;
                        }
                    }
                }
            }
        }

        // 3. Fallback standard accounting code / alias heuristics
        return findByKnownAliases(items, target);
    }

    private FinancialStatementItem findByKnownAliases(List<FinancialStatementItem> items, String target) {
        return switch (target) {
            case "CURRENT_ASSETS" -> findByCodeOrKey(items, "100", "CURRENT_ASSETS");
            case "CASH_AND_EQUIVALENTS" -> findByCodeOrKey(items, "110", "CASH_AND_EQUIVALENTS");
            case "INVENTORY", "INVENTORIES", "INVENTORY_BEGINNING", "INVENTORY_ENDING", "AVERAGE_INVENTORY" ->
                    findByCodeOrKey(items, "140", "INVENTORY", "INVENTORIES");
            case "RECEIVABLES", "SHORT_TERM_RECEIVABLES", "RECEIVABLES_BEGINNING", "RECEIVABLES_ENDING", "AVERAGE_RECEIVABLES" ->
                    findByCodeOrKey(items, "130", "SHORT_TERM_RECEIVABLES", "RECEIVABLES");
            case "TOTAL_ASSETS", "TOTAL_ASSETS_BEGINNING" -> findByCodeOrKey(items, "280", "270", "TOTAL_ASSETS");
            case "TOTAL_LIABILITIES" -> findByCodeOrKey(items, "300", "TOTAL_LIABILITIES");
            case "CURRENT_LIABILITIES" -> findByCodeOrKey(items, "310", "CURRENT_LIABILITIES");
            case "PAYABLES", "SHORT_TERM_PAYABLES", "PAYABLES_BEGINNING", "PAYABLES_ENDING", "AVERAGE_PAYABLES" ->
                    findByCodeOrKey(items, "311", "SHORT_TERM_PAYABLES", "PAYABLES");
            case "EQUITY", "TOTAL_EQUITY_BEGINNING" -> findByCodeOrKey(items, "400", "EQUITY");
            case "NET_REVENUE" -> findByCodeOrKey(items, "10", "NET_REVENUE");
            case "COST_OF_GOODS_SOLD" -> findByCodeOrKey(items, "11", "COST_OF_GOODS_SOLD");
            case "GROSS_PROFIT" -> findByCodeOrKey(items, "20", "GROSS_PROFIT");
            case "NET_PROFIT" -> findByCodeOrKey(items, "60", "NET_PROFIT");
            default -> null;
        };
    }

    private FinancialStatementItem findByCodeOrKey(List<FinancialStatementItem> items, String... candidates) {
        Set<String> set = new HashSet<>();
        for (String c : candidates) {
            set.add(c.trim().toUpperCase());
        }

        for (FinancialStatementItem item : items) {
            if (item.getCode() != null && set.contains(item.getCode().trim().toUpperCase())) {
                return item;
            }
            if (item.getKey() != null && set.contains(item.getKey().trim().toUpperCase())) {
                return item;
            }
        }
        return null;
    }

    private AccountingStandard parseAccountingStandard(String stdStr) {
        if (stdStr == null || stdStr.isBlank()) {
            return AccountingStandard.TT200;
        }
        try {
            return AccountingStandard.valueOf(stdStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

