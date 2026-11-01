package com.financial.modules.calculationengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.config.CalculationProperties;
import com.financial.modules.calculationengine.entity.FinancialCalculationResult;
import com.financial.modules.calculationengine.enums.CalculationStatus;
import com.financial.modules.calculationengine.service.impl.FormulaExecutionServiceImpl;
import com.financial.modules.calculationengine.service.impl.IndicatorResolverServiceImpl;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.EvaluationDirection;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialUnit;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormulaExecutionServiceTest {

    @Mock
    private AccountingIndicatorMappingRepository mappingRepository;

    @Mock
    private FinancialStatementItemRepository itemRepository;

    @Mock
    private FinancialRatioInputRepository ratioInputRepository;

    private IndicatorResolverService indicatorResolverService;
    private FormulaExecutionService formulaExecutionService;
    private CalculationProperties properties;
    private ObjectMapper objectMapper;

    private FinancialStatement statement;
    private List<FinancialStatementItem> statementItems;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        properties = new CalculationProperties();
        properties.setDefaultScale(4);
        properties.setRoundingMode(RoundingMode.HALF_UP);

        indicatorResolverService = new IndicatorResolverServiceImpl(mappingRepository, itemRepository);
        formulaExecutionService = new FormulaExecutionServiceImpl(
                indicatorResolverService,
                ratioInputRepository,
                properties,
                objectMapper
        );

        // Standard test financial statement with confirmed data
        statement = FinancialStatement.builder()
                .id(100L)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        statementItems = new ArrayList<>();
        // Balance Sheet Items (Current period = ending, Previous period = beginning)
        // 100: Current Assets (Ending: 50B, Beginning: 40B)
        statementItems.add(createItem("100", "CURRENT_ASSETS", FinancialStatementType.BALANCE_SHEET, "50000000000", "40000000000"));
        // 110: Cash and Cash Equivalents (Ending: 10B, Beginning: 8B)
        statementItems.add(createItem("110", "CASH_AND_EQUIVALENTS", FinancialStatementType.BALANCE_SHEET, "10000000000", "8000000000"));
        // 130: Short-term Receivables (Ending: 20B, Beginning: 16B)
        statementItems.add(createItem("130", "SHORT_TERM_RECEIVABLES", FinancialStatementType.BALANCE_SHEET, "20000000000", "16000000000"));
        // 140: Inventories (Ending: 15B, Beginning: 10B)
        statementItems.add(createItem("140", "INVENTORIES", FinancialStatementType.BALANCE_SHEET, "15000000000", "10000000000"));
        // 280: Total Assets (Ending: 100B, Beginning: 90B)
        statementItems.add(createItem("280", "TOTAL_ASSETS", FinancialStatementType.BALANCE_SHEET, "100000000000", "90000000000"));
        // 300: Total Liabilities (Ending: 40B, Beginning: 35B)
        statementItems.add(createItem("300", "TOTAL_LIABILITIES", FinancialStatementType.BALANCE_SHEET, "40000000000", "35000000000"));
        // 310: Current Liabilities (Ending: 25B, Beginning: 20B)
        statementItems.add(createItem("310", "CURRENT_LIABILITIES", FinancialStatementType.BALANCE_SHEET, "25000000000", "20000000000"));
        // 311: Short-term Payables (Ending: 12B, Beginning: 8B)
        statementItems.add(createItem("311", "SHORT_TERM_PAYABLES", FinancialStatementType.BALANCE_SHEET, "12000000000", "8000000000"));
        // 400: Equity (Ending: 60B, Beginning: 55B)
        statementItems.add(createItem("400", "EQUITY", FinancialStatementType.BALANCE_SHEET, "60000000000", "55000000000"));

        // Income Statement Items
        // 10: Net Revenue (Current: 120B)
        statementItems.add(createItem("10", "NET_REVENUE", FinancialStatementType.INCOME_STATEMENT, "120000000000", null));
        // 11: Cost of Goods Sold (Current: 75B)
        statementItems.add(createItem("11", "COST_OF_GOODS_SOLD", FinancialStatementType.INCOME_STATEMENT, "75000000000", null));
        // 20: Gross Profit (Current: 45B)
        statementItems.add(createItem("20", "GROSS_PROFIT", FinancialStatementType.INCOME_STATEMENT, "45000000000", null));
        // 60: Net Profit After Tax (Current: 12B)
        statementItems.add(createItem("60", "NET_PROFIT", FinancialStatementType.INCOME_STATEMENT, "12000000000", null));

        statement.setItems(statementItems);
    }

    private FinancialStatementItem createItem(String code, String key, FinancialStatementType type, String current, String previous) {
        return FinancialStatementItem.builder()
                .financialStatement(statement)
                .code(code)
                .key(key)
                .statementType(type)
                .currentValue(current != null ? new BigDecimal(current) : null)
                .previousValue(previous != null ? new BigDecimal(previous) : null)
                .mappingStatus(MappingStatus.MATCHED)
                .unit("VND")
                .build();
    }

    private FinancialRatioDefinition buildRatioDef(
            Long id, String code, String name, FinancialRatioCategory cat,
            String formula, FinancialUnit unit, boolean isCoreKpi
    ) {
        return FinancialRatioDefinition.builder()
                .id(id)
                .ratioCode(code)
                .ratioName(name)
                .category(cat)
                .formulaExpression(formula)
                .unit(unit)
                .evaluationDirection(EvaluationDirection.HIGHER_IS_BETTER)
                .decimalScale(4)
                .isCoreKpi(isCoreKpi)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("1. Calculate CURRENT_RATIO (Core KPI): 50B / 25B = 2.0000")
    void testCurrentRatio() {
        FinancialRatioDefinition def = buildRatioDef(1L, "CURRENT_RATIO", "Chỉ số thanh toán hiện hành",
                FinancialRatioCategory.LIQUIDITY, "CURRENT_ASSETS / CURRENT_LIABILITIES", FinancialUnit.TIMES, true);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("2.0000"));
        assertThat(res.getIsCoreKpi()).isTrue();
        assertThat(res.getFormulaSnapshot()).isEqualTo("CURRENT_ASSETS / CURRENT_LIABILITIES");
        assertThat(res.getInputSnapshot()).contains("CURRENT_ASSETS").contains("CURRENT_LIABILITIES");
    }

    @Test
    @DisplayName("2. Calculate QUICK_RATIO: (50B - 15B) / 25B = 1.4000")
    void testQuickRatio() {
        FinancialRatioDefinition def = buildRatioDef(2L, "QUICK_RATIO", "Chỉ số thanh toán nhanh",
                FinancialRatioCategory.LIQUIDITY, "(CURRENT_ASSETS - INVENTORY) / CURRENT_LIABILITIES", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("1.4000"));
        assertThat(res.getIsCoreKpi()).isFalse();
    }

    @Test
    @DisplayName("3. Calculate CASH_RATIO: 10B / 25B = 0.4000")
    void testCashRatio() {
        FinancialRatioDefinition def = buildRatioDef(3L, "CASH_RATIO", "Chỉ số thanh toán tiền mặt",
                FinancialRatioCategory.LIQUIDITY, "CASH_AND_EQUIVALENTS / CURRENT_LIABILITIES", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("0.4000"));
    }

    @Test
    @DisplayName("4. Calculate DEBT_TO_EQUITY (Core KPI): 40B / 60B = 0.6667")
    void testDebtToEquity() {
        FinancialRatioDefinition def = buildRatioDef(4L, "DEBT_TO_EQUITY", "Hệ số nợ trên vốn chủ sở hữu",
                FinancialRatioCategory.SOLVENCY, "TOTAL_LIABILITIES / EQUITY", FinancialUnit.TIMES, true);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("0.6667"));
        assertThat(res.getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("5. Calculate DEBT_RATIO: 40B / 100B = 0.4000")
    void testDebtRatio() {
        FinancialRatioDefinition def = buildRatioDef(5L, "DEBT_RATIO", "Hệ số nợ",
                FinancialRatioCategory.SOLVENCY, "TOTAL_LIABILITIES / TOTAL_ASSETS", FinancialUnit.PERCENT, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("0.4000"));
    }

    @Test
    @DisplayName("6. Calculate EQUITY_RATIO: 60B / 100B = 0.6000")
    void testEquityRatio() {
        FinancialRatioDefinition def = buildRatioDef(6L, "EQUITY_RATIO", "Hệ số tự tài trợ",
                FinancialRatioCategory.SOLVENCY, "EQUITY / TOTAL_ASSETS", FinancialUnit.PERCENT, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("0.6000"));
    }

    @Test
    @DisplayName("7. Calculate ROA (Core KPI): 12B / 100B (ending) * 100 = 12.0000%")
    void testRoaUsesEndingTotalAssets() {
        FinancialRatioDefinition def = buildRatioDef(7L, "ROA", "Tỷ suất sinh lời trên tổng tài sản",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / TOTAL_ASSETS * 100", FinancialUnit.PERCENT, true);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        // Ending total assets is 100B, not beginning 90B: 12B / 100B * 100 = 12.0000%
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("12.0000"));
        assertThat(res.getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("8. Calculate ROE (Core KPI): 12B / 60B (ending) * 100 = 20.0000%")
    void testRoeUsesEndingEquity() {
        FinancialRatioDefinition def = buildRatioDef(8L, "ROE", "Tỷ suất sinh lời trên vốn chủ sở hữu",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / EQUITY * 100", FinancialUnit.PERCENT, true);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        // Ending equity is 60B, not beginning 55B: 12B / 60B * 100 = 20.0000%
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("20.0000"));
        assertThat(res.getIsCoreKpi()).isTrue();
    }

    @Test
    @DisplayName("9. Calculate NET_PROFIT_MARGIN: 12B / 120B * 100 = 10.0000%")
    void testNetProfitMargin() {
        FinancialRatioDefinition def = buildRatioDef(9L, "NET_PROFIT_MARGIN", "Tỷ suất lợi nhuận ròng",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / NET_REVENUE * 100", FinancialUnit.PERCENT, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("10.0000"));
    }

    @Test
    @DisplayName("10. Calculate GROSS_PROFIT_MARGIN: 45B / 120B * 100 = 37.5000%")
    void testGrossProfitMargin() {
        FinancialRatioDefinition def = buildRatioDef(10L, "GROSS_PROFIT_MARGIN", "Tỷ suất lợi nhuận gộp",
                FinancialRatioCategory.PROFITABILITY, "GROSS_PROFIT / NET_REVENUE * 100", FinancialUnit.PERCENT, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("37.5000"));
    }

    @Test
    @DisplayName("11. Calculate ASSET_TURNOVER: 120B / 100B (ending) = 1.2000")
    void testAssetTurnoverUsesEndingTotalAssets() {
        FinancialRatioDefinition def = buildRatioDef(11L, "ASSET_TURNOVER", "Vòng quay tổng tài sản",
                FinancialRatioCategory.EFFICIENCY, "NET_REVENUE / TOTAL_ASSETS", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("1.2000"));
    }

    @Test
    @DisplayName("12. Calculate INVENTORY_TURNOVER with average inventory: 75B / ((10B + 15B)/2) = 6.0000")
    void testInventoryTurnover() {
        FinancialRatioDefinition def = buildRatioDef(12L, "INVENTORY_TURNOVER", "Vòng quay hàng tồn kho",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_INVENTORY", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        // Average inventory: (10B + 15B) / 2 = 12.5B; 75B / 12.5B = 6.0000
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("6.0000"));
        assertThat(res.getInputSnapshot()).contains("AVERAGE_INVENTORY");
    }

    @Test
    @DisplayName("13. Calculate RECEIVABLES_TURNOVER with proxy note: 120B / ((16B + 20B)/2) = 6.6667")
    void testReceivablesTurnover() {
        FinancialRatioDefinition def = buildRatioDef(13L, "RECEIVABLES_TURNOVER", "Vòng quay các khoản phải thu",
                FinancialRatioCategory.EFFICIENCY, "NET_REVENUE / AVERAGE_RECEIVABLES", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        // Average receivables: (16B + 20B) / 2 = 18B; 120B / 18B = 6.6667
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("6.6667"));
        assertThat(res.getCalculationNote()).contains("proxy for net credit sales");
    }

    @Test
    @DisplayName("14. Calculate PAYABLES_TURNOVER: 75B / ((8B + 12B)/2) = 7.5000")
    void testPayablesTurnover() {
        FinancialRatioDefinition def = buildRatioDef(14L, "PAYABLES_TURNOVER", "Vòng quay các khoản phải trả",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_PAYABLES", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.SUCCESS);
        // Average payables: (8B + 12B) / 2 = 10B; 75B / 10B = 7.5000
        assertThat(res.getResultValue()).isEqualByComparingTo(new BigDecimal("7.5000"));
    }

    @Test
    @DisplayName("Missing beginning inventory returns INSUFFICIENT_DATA (no silent fallback)")
    void testMissingBeginningBalance_ReturnsInsufficientData() {
        // Clear previousValue of inventory (code 140)
        statementItems.stream()
                .filter(i -> "140".equals(i.getCode()))
                .findFirst()
                .ifPresent(i -> i.setPreviousValue(null));

        FinancialRatioDefinition def = buildRatioDef(12L, "INVENTORY_TURNOVER", "Vòng quay hàng tồn kho",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_INVENTORY", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.INSUFFICIENT_DATA);
        assertThat(res.getResultValue()).isNull();
        assertThat(res.getCalculationNote()).contains("inventory balance for average calculation");
    }

    @Test
    @DisplayName("Missing ending inventory returns INSUFFICIENT_DATA (no silent fallback)")
    void testMissingEndingBalance_ReturnsInsufficientData() {
        // Clear currentValue of inventory (code 140)
        statementItems.stream()
                .filter(i -> "140".equals(i.getCode()))
                .findFirst()
                .ifPresent(i -> i.setCurrentValue(null));

        FinancialRatioDefinition def = buildRatioDef(12L, "INVENTORY_TURNOVER", "Vòng quay hàng tồn kho",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_INVENTORY", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.INSUFFICIENT_DATA);
        assertThat(res.getResultValue()).isNull();
        assertThat(res.getCalculationNote()).contains("inventory balance for average calculation");
    }

    @Test
    @DisplayName("Division by zero returns DIVISION_BY_ZERO with null resultValue")
    void testDivisionByZero_ReturnsDivisionByZero() {
        // Set Current Liabilities to 0
        statementItems.stream()
                .filter(i -> "310".equals(i.getCode()))
                .findFirst()
                .ifPresent(i -> i.setCurrentValue(BigDecimal.ZERO));

        FinancialRatioDefinition def = buildRatioDef(1L, "CURRENT_RATIO", "Chỉ số thanh toán hiện hành",
                FinancialRatioCategory.LIQUIDITY, "CURRENT_ASSETS / CURRENT_LIABILITIES", FinancialUnit.TIMES, true);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.DIVISION_BY_ZERO);
        assertThat(res.getResultValue()).isNull();
        assertThat(res.getCalculationNote()).contains("Division by zero");
    }

    @Test
    @DisplayName("Unknown ratio code returns NOT_CONFIGURED status")
    void testUnconfiguredRatioCode_ReturnsNotConfigured() {
        FinancialRatioDefinition def = buildRatioDef(99L, "CUSTOM_UNKNOWN_RATIO", "Chỉ số tùy chỉnh chưa cấu hình",
                FinancialRatioCategory.EFFICIENCY, "X / Y", FinancialUnit.TIMES, false);

        FinancialCalculationResult res = formulaExecutionService.executeRatioCalculation(statement, def, null);

        assertThat(res.getStatus()).isEqualTo(CalculationStatus.NOT_CONFIGURED);
        assertThat(res.getResultValue()).isNull();
    }
}

