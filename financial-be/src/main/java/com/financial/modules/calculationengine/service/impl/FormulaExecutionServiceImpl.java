package com.financial.modules.calculationengine.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.config.CalculationProperties;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.service.FormulaExecutionService;
import com.financial.modules.calculationengine.service.IndicatorResolverService;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaExecutionServiceImpl implements FormulaExecutionService {

    private final IndicatorResolverService indicatorResolver;
    private final FinancialRatioInputRepository ratioInputRepository;
    private final CalculationProperties calculationProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public FinancialCalculationResult executeRatioCalculation(
            FinancialStatement statement,
            FinancialRatioDefinition ratioDefinition,
            Integer scaleOverride
    ) {
        if (ratioDefinition == null) {
            throw new IllegalArgumentException("Ratio definition cannot be null");
        }

        String ratioCode = ratioDefinition.getRatioCode();
        int scale = scaleOverride != null ? scaleOverride
                : (ratioDefinition.getDecimalScale() != null ? ratioDefinition.getDecimalScale() : calculationProperties.getDefaultScale());
        RoundingMode roundingMode = calculationProperties.getRoundingMode() != null
                ? calculationProperties.getRoundingMode()
                : RoundingMode.HALF_UP;

        // Fetch inputs from DB or use relation if loaded
        List<FinancialRatioInput> inputs = ratioInputRepository.findByRatioDefinitionIdOrderByInputOrderAsc(ratioDefinition.getId());
        if ((inputs == null || inputs.isEmpty()) && ratioDefinition.getInputs() != null) {
            inputs = ratioDefinition.getInputs();
        }

        Map<String, Object> inputSnapshotMap = new LinkedHashMap<>();
        Map<String, BigDecimal> resolvedInputs = new HashMap<>();

        // Resolve inputs from repository/relation if configured
        if (inputs != null && !inputs.isEmpty()) {
            for (FinancialRatioInput in : inputs) {
                String alias = in.getInputAlias() != null ? in.getInputAlias()
                        : (in.getIndicator() != null ? in.getIndicator().getIndicatorCode() : "INPUT_" + in.getInputOrder());
                BigDecimal val = indicatorResolver.resolveValue(statement, in.getIndicator(), in.getInputAlias(), in.getPeriodReference());

                inputSnapshotMap.put(alias, val);
                if (val != null) {
                    resolvedInputs.put(alias.toUpperCase(), val);
                    if (in.getIndicator() != null) {
                        resolvedInputs.put(in.getIndicator().getIndicatorCode().toUpperCase(), val);
                    }
                }
            }
        }

        // Execute specific formula logic
        CalculationOutcome outcome = calculateRatio(ratioCode, statement, resolvedInputs, inputSnapshotMap, scale, roundingMode);

        String inputJson = serializeInputSnapshot(inputSnapshotMap);

        return FinancialCalculationResult.builder()
                .ratioDefinition(ratioDefinition)
                .ratioCode(ratioCode)
                .ratioName(ratioDefinition.getRatioName())
                .category(ratioDefinition.getCategory())
                .unit(ratioDefinition.getUnit())
                .isCoreKpi(Boolean.TRUE.equals(ratioDefinition.getIsCoreKpi()))
                .formulaSnapshot(ratioDefinition.getFormulaExpression())
                .inputSnapshot(inputJson)
                .status(outcome.status())
                .resultValue(outcome.resultValue())
                .calculationNote(outcome.note())
                .build();
    }

    private CalculationOutcome calculateRatio(
            String ratioCode,
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> inputSnapshotMap,
            int scale,
            RoundingMode roundingMode
    ) {
        String code = ratioCode != null ? ratioCode.trim().toUpperCase() : "";

        return switch (code) {
            // --- 1. LIQUIDITY ---
            case "CURRENT_RATIO" -> calculateCurrentRatio(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "QUICK_RATIO" -> calculateQuickRatio(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "CASH_RATIO" -> calculateCashRatio(statement, inputs, inputSnapshotMap, scale, roundingMode);

            // --- 2. SOLVENCY ---
            case "DEBT_TO_EQUITY" -> calculateDebtToEquity(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "DEBT_RATIO" -> calculateDebtRatio(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "EQUITY_RATIO" -> calculateEquityRatio(statement, inputs, inputSnapshotMap, scale, roundingMode);

            // --- 3. PROFITABILITY ---
            case "ROA" -> calculateRoa(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "ROE" -> calculateRoe(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "NET_PROFIT_MARGIN" -> calculateNetProfitMargin(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "GROSS_PROFIT_MARGIN" -> calculateGrossProfitMargin(statement, inputs, inputSnapshotMap, scale, roundingMode);

            // --- 4. EFFICIENCY ---
            case "ASSET_TURNOVER" -> calculateAssetTurnover(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "INVENTORY_TURNOVER" -> calculateInventoryTurnover(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "RECEIVABLES_TURNOVER" -> calculateReceivablesTurnover(statement, inputs, inputSnapshotMap, scale, roundingMode);
            case "PAYABLES_TURNOVER" -> calculatePayablesTurnover(statement, inputs, inputSnapshotMap, scale, roundingMode);

            default -> new CalculationOutcome(CalculationStatus.NOT_CONFIGURED, null, "Formula not configured for ratio code: " + ratioCode);
        };
    }

    // 1. CURRENT_RATIO: CURRENT_ASSETS / CURRENT_LIABILITIES
    private CalculationOutcome calculateCurrentRatio(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal ca = getOrResolve(inputs, snapshot, statement, "CURRENT_ASSETS", PeriodReference.CURRENT);
        BigDecimal cl = getOrResolve(inputs, snapshot, statement, "CURRENT_LIABILITIES", PeriodReference.CURRENT);

        if (ca == null || cl == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Current Assets or Current Liabilities");
        }
        if (cl.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Current Liabilities is 0");
        }
        BigDecimal res = ca.divide(cl, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 2. QUICK_RATIO: (CURRENT_ASSETS - INVENTORY) / CURRENT_LIABILITIES
    private CalculationOutcome calculateQuickRatio(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal ca = getOrResolve(inputs, snapshot, statement, "CURRENT_ASSETS", PeriodReference.CURRENT);
        BigDecimal inv = getOrResolve(inputs, snapshot, statement, "INVENTORY", PeriodReference.CURRENT);
        BigDecimal cl = getOrResolve(inputs, snapshot, statement, "CURRENT_LIABILITIES", PeriodReference.CURRENT);

        if (ca == null || inv == null || cl == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Current Assets, Inventory, or Current Liabilities");
        }
        if (cl.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Current Liabilities is 0");
        }
        BigDecimal res = ca.subtract(inv).divide(cl, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 3. CASH_RATIO: CASH_AND_EQUIVALENTS / CURRENT_LIABILITIES
    private CalculationOutcome calculateCashRatio(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal cash = getOrResolve(inputs, snapshot, statement, "CASH_AND_EQUIVALENTS", PeriodReference.CURRENT);
        BigDecimal cl = getOrResolve(inputs, snapshot, statement, "CURRENT_LIABILITIES", PeriodReference.CURRENT);

        if (cash == null || cl == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Cash and Equivalents or Current Liabilities");
        }
        if (cl.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Current Liabilities is 0");
        }
        BigDecimal res = cash.divide(cl, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 4. DEBT_TO_EQUITY: TOTAL_LIABILITIES / EQUITY
    private CalculationOutcome calculateDebtToEquity(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal tl = getOrResolve(inputs, snapshot, statement, "TOTAL_LIABILITIES", PeriodReference.CURRENT);
        BigDecimal eq = getOrResolve(inputs, snapshot, statement, "EQUITY", PeriodReference.CURRENT);

        if (tl == null || eq == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Total Liabilities or Equity");
        }
        if (eq.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Equity is 0");
        }
        BigDecimal res = tl.divide(eq, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 5. DEBT_RATIO: TOTAL_LIABILITIES / TOTAL_ASSETS
    private CalculationOutcome calculateDebtRatio(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal tl = getOrResolve(inputs, snapshot, statement, "TOTAL_LIABILITIES", PeriodReference.CURRENT);
        BigDecimal ta = getOrResolve(inputs, snapshot, statement, "TOTAL_ASSETS", PeriodReference.CURRENT);

        if (tl == null || ta == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Total Liabilities or Total Assets");
        }
        if (ta.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Total Assets is 0");
        }
        BigDecimal res = tl.divide(ta, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 6. EQUITY_RATIO: EQUITY / TOTAL_ASSETS
    private CalculationOutcome calculateEquityRatio(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal eq = getOrResolve(inputs, snapshot, statement, "EQUITY", PeriodReference.CURRENT);
        BigDecimal ta = getOrResolve(inputs, snapshot, statement, "TOTAL_ASSETS", PeriodReference.CURRENT);

        if (eq == null || ta == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Equity or Total Assets");
        }
        if (ta.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Total Assets is 0");
        }
        BigDecimal res = eq.divide(ta, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 7. ROA: NET_PROFIT / TOTAL_ASSETS * 100 (Uses ending total assets)
    private CalculationOutcome calculateRoa(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal np = getOrResolve(inputs, snapshot, statement, "NET_PROFIT", PeriodReference.CURRENT);
        BigDecimal taEnding = getOrResolve(inputs, snapshot, statement, "TOTAL_ASSETS", PeriodReference.ENDING);

        if (np == null || taEnding == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Net Profit or Total Assets (Ending balance)");
        }
        if (taEnding.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Ending Total Assets is 0");
        }
        BigDecimal res = np.divide(taEnding, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 8. ROE: NET_PROFIT / EQUITY * 100 (Uses ending equity)
    private CalculationOutcome calculateRoe(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal np = getOrResolve(inputs, snapshot, statement, "NET_PROFIT", PeriodReference.CURRENT);
        BigDecimal eqEnding = getOrResolve(inputs, snapshot, statement, "EQUITY", PeriodReference.ENDING);

        if (np == null || eqEnding == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Net Profit or Equity (Ending balance)");
        }
        if (eqEnding.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Ending Equity is 0");
        }
        BigDecimal res = np.divide(eqEnding, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 9. NET_PROFIT_MARGIN: NET_PROFIT / NET_REVENUE * 100
    private CalculationOutcome calculateNetProfitMargin(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal np = getOrResolve(inputs, snapshot, statement, "NET_PROFIT", PeriodReference.CURRENT);
        BigDecimal nr = getOrResolve(inputs, snapshot, statement, "NET_REVENUE", PeriodReference.CURRENT);

        if (np == null || nr == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Net Profit or Net Revenue");
        }
        if (nr.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Net Revenue is 0");
        }
        BigDecimal res = np.divide(nr, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 10. GROSS_PROFIT_MARGIN: GROSS_PROFIT / NET_REVENUE * 100
    private CalculationOutcome calculateGrossProfitMargin(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal gp = getOrResolve(inputs, snapshot, statement, "GROSS_PROFIT", PeriodReference.CURRENT);
        BigDecimal nr = getOrResolve(inputs, snapshot, statement, "NET_REVENUE", PeriodReference.CURRENT);

        if (gp == null || nr == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Gross Profit or Net Revenue");
        }
        if (nr.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Net Revenue is 0");
        }
        BigDecimal res = gp.divide(nr, MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 11. ASSET_TURNOVER: NET_REVENUE / TOTAL_ASSETS (Ending balance)
    private CalculationOutcome calculateAssetTurnover(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal nr = getOrResolve(inputs, snapshot, statement, "NET_REVENUE", PeriodReference.CURRENT);
        BigDecimal taEnding = getOrResolve(inputs, snapshot, statement, "TOTAL_ASSETS", PeriodReference.ENDING);

        if (nr == null || taEnding == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Net Revenue or Total Assets (Ending balance)");
        }
        if (taEnding.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Ending Total Assets is 0");
        }
        BigDecimal res = nr.divide(taEnding, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 12. INVENTORY_TURNOVER: COST_OF_GOODS_SOLD / AVERAGE_INVENTORY
    private CalculationOutcome calculateInventoryTurnover(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal cogs = getOrResolve(inputs, snapshot, statement, "COST_OF_GOODS_SOLD", PeriodReference.CURRENT);
        BigDecimal invBeg = getOrResolve(inputs, snapshot, statement, "INVENTORY_BEGINNING", PeriodReference.BEGINNING);
        BigDecimal invEnd = getOrResolve(inputs, snapshot, statement, "INVENTORY_ENDING", PeriodReference.ENDING);

        // Fallbacks if not mapped separately
        if (invBeg == null) {
            invBeg = indicatorResolver.resolveValueByCode(statement, "INVENTORY", PeriodReference.BEGINNING);
            if (invBeg != null) snapshot.put("INVENTORY_BEGINNING", invBeg);
        }
        if (invEnd == null) {
            invEnd = indicatorResolver.resolveValueByCode(statement, "INVENTORY", PeriodReference.ENDING);
            if (invEnd != null) snapshot.put("INVENTORY_ENDING", invEnd);
        }

        if (cogs == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Cost of Goods Sold");
        }
        // Rule: If beginning or ending balance is missing, return INSUFFICIENT_DATA
        if (invBeg == null || invEnd == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing beginning or ending inventory balance for average calculation");
        }

        BigDecimal avgInv = invBeg.add(invEnd).divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
        snapshot.put("AVERAGE_INVENTORY", avgInv);

        if (avgInv.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Average Inventory is 0");
        }
        BigDecimal res = cogs.divide(avgInv, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    // 13. RECEIVABLES_TURNOVER: NET_REVENUE / AVERAGE_RECEIVABLES
    private CalculationOutcome calculateReceivablesTurnover(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal nr = getOrResolve(inputs, snapshot, statement, "NET_REVENUE", PeriodReference.CURRENT);
        BigDecimal recBeg = getOrResolve(inputs, snapshot, statement, "RECEIVABLES_BEGINNING", PeriodReference.BEGINNING);
        BigDecimal recEnd = getOrResolve(inputs, snapshot, statement, "RECEIVABLES_ENDING", PeriodReference.ENDING);

        if (recBeg == null) {
            recBeg = indicatorResolver.resolveValueByCode(statement, "SHORT_TERM_RECEIVABLES", PeriodReference.BEGINNING);
            if (recBeg != null) snapshot.put("RECEIVABLES_BEGINNING", recBeg);
        }
        if (recEnd == null) {
            recEnd = indicatorResolver.resolveValueByCode(statement, "SHORT_TERM_RECEIVABLES", PeriodReference.ENDING);
            if (recEnd != null) snapshot.put("RECEIVABLES_ENDING", recEnd);
        }

        if (nr == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Net Revenue");
        }
        // Rule: If beginning or ending balance is missing, return INSUFFICIENT_DATA
        if (recBeg == null || recEnd == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing beginning or ending receivables balance for average calculation");
        }

        BigDecimal avgRec = recBeg.add(recEnd).divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
        snapshot.put("AVERAGE_RECEIVABLES", avgRec);

        if (avgRec.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Average Receivables is 0");
        }
        BigDecimal res = nr.divide(avgRec, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, "Using Net Revenue as a proxy for net credit sales");
    }

    // 14. PAYABLES_TURNOVER: COST_OF_GOODS_SOLD / AVERAGE_PAYABLES
    private CalculationOutcome calculatePayablesTurnover(
            FinancialStatement statement,
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            int scale,
            RoundingMode rm
    ) {
        BigDecimal cogs = getOrResolve(inputs, snapshot, statement, "COST_OF_GOODS_SOLD", PeriodReference.CURRENT);
        BigDecimal payBeg = getOrResolve(inputs, snapshot, statement, "PAYABLES_BEGINNING", PeriodReference.BEGINNING);
        BigDecimal payEnd = getOrResolve(inputs, snapshot, statement, "PAYABLES_ENDING", PeriodReference.ENDING);

        if (payBeg == null) {
            payBeg = indicatorResolver.resolveValueByCode(statement, "SHORT_TERM_PAYABLES", PeriodReference.BEGINNING);
            if (payBeg != null) snapshot.put("PAYABLES_BEGINNING", payBeg);
        }
        if (payEnd == null) {
            payEnd = indicatorResolver.resolveValueByCode(statement, "SHORT_TERM_PAYABLES", PeriodReference.ENDING);
            if (payEnd != null) snapshot.put("PAYABLES_ENDING", payEnd);
        }

        if (cogs == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing required input: Cost of Goods Sold");
        }
        // Rule: If beginning or ending balance is missing, return INSUFFICIENT_DATA
        if (payBeg == null || payEnd == null) {
            return new CalculationOutcome(CalculationStatus.INSUFFICIENT_DATA, null, "Missing beginning or ending payables balance for average calculation");
        }

        BigDecimal avgPay = payBeg.add(payEnd).divide(BigDecimal.valueOf(2), MathContext.DECIMAL128);
        snapshot.put("AVERAGE_PAYABLES", avgPay);

        if (avgPay.compareTo(BigDecimal.ZERO) == 0) {
            return new CalculationOutcome(CalculationStatus.DIVISION_BY_ZERO, null, "Division by zero: Average Payables is 0");
        }
        BigDecimal res = cogs.divide(avgPay, MathContext.DECIMAL128).setScale(scale, rm);
        return new CalculationOutcome(CalculationStatus.SUCCESS, res, null);
    }

    private BigDecimal getOrResolve(
            Map<String, BigDecimal> inputs,
            Map<String, Object> snapshot,
            FinancialStatement statement,
            String alias,
            PeriodReference period
    ) {
        String key = alias.toUpperCase();
        if (inputs.containsKey(key) && inputs.get(key) != null) {
            return inputs.get(key);
        }
        BigDecimal resolved = indicatorResolver.resolveValueByCode(statement, alias, period);
        if (resolved != null) {
            inputs.put(key, resolved);
            snapshot.put(alias, resolved);
        }
        return resolved;
    }

    private String serializeInputSnapshot(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize input snapshot map to JSON: {}", e.getMessage());
            return snapshot.toString();
        }
    }

    private record CalculationOutcome(
            CalculationStatus status,
            BigDecimal resultValue,
            String note
    ) {}
}

