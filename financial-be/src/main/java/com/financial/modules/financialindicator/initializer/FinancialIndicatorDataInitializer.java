package com.financial.modules.financialindicator.initializer;

import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.entity.FinancialRatioInput;
import com.financial.modules.financialindicator.enums.*;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialindicator.repository.FinancialIndicatorRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@org.springframework.core.annotation.Order(1)
@RequiredArgsConstructor
public class FinancialIndicatorDataInitializer implements ApplicationRunner {

    private final FinancialIndicatorRepository indicatorRepository;
    private final FinancialRatioDefinitionRepository ratioRepository;
    private final FinancialRatioInputRepository ratioInputRepository;
    private final AccountingIndicatorMappingRepository mappingRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting Financial Indicator Module data initialization...");
        Map<String, FinancialIndicator> indicators = initializeIndicators();
        initializeRatios(indicators);
        initializeAccountingMappings(indicators);
        log.info("Financial Indicator Module data initialization completed successfully.");
    }

    /**
     * Seeds the 19 initial semantic financial indicators.
     */
    private Map<String, FinancialIndicator> initializeIndicators() {
        Map<String, FinancialIndicator> map = new HashMap<>();

        // 1. CURRENT_ASSETS
        map.put("CURRENT_ASSETS", seedIndicator(
                "CURRENT_ASSETS", "Tài sản ngắn hạn", "Current Assets",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tổng tài sản ngắn hạn"
        ));

        // 2. INVENTORY
        map.put("INVENTORY", seedIndicator(
                "INVENTORY", "Hàng tồn kho", "Inventories",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Hàng tồn kho"
        ));

        // 3. CASH_AND_EQUIVALENTS
        map.put("CASH_AND_EQUIVALENTS", seedIndicator(
                "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền", "Cash and Cash Equivalents",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tiền và các khoản tương đương tiền"
        ));

        // 4. CURRENT_LIABILITIES
        map.put("CURRENT_LIABILITIES", seedIndicator(
                "CURRENT_LIABILITIES", "Nợ ngắn hạn", "Current Liabilities",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Nợ phải trả ngắn hạn"
        ));

        // 5. TOTAL_LIABILITIES
        map.put("TOTAL_LIABILITIES", seedIndicator(
                "TOTAL_LIABILITIES", "Nợ phải trả", "Total Liabilities",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tổng nợ phải trả"
        ));

        // 6. TOTAL_ASSETS
        map.put("TOTAL_ASSETS", seedIndicator(
                "TOTAL_ASSETS", "Tổng tài sản", "Total Assets",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tổng cộng tài sản"
        ));

        // 7. EQUITY
        map.put("EQUITY", seedIndicator(
                "EQUITY", "Vốn chủ sở hữu", "Total Equity",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tổng vốn chủ sở hữu"
        ));

        // 8. NET_PROFIT
        map.put("NET_PROFIT", seedIndicator(
                "NET_PROFIT", "Lợi nhuận sau thuế", "Net Profit After Tax",
                FinancialStatementType.INCOME_STATEMENT, "DECIMAL", FinancialUnit.VND,
                "Lợi nhuận sau thuế thu nhập doanh nghiệp"
        ));

        // 9. NET_REVENUE
        map.put("NET_REVENUE", seedIndicator(
                "NET_REVENUE", "Doanh thu thuần", "Net Revenue",
                FinancialStatementType.INCOME_STATEMENT, "DECIMAL", FinancialUnit.VND,
                "Doanh thu thuần về bán hàng và cung cấp dịch vụ"
        ));

        // 10. GROSS_PROFIT
        map.put("GROSS_PROFIT", seedIndicator(
                "GROSS_PROFIT", "Lợi nhuận gộp", "Gross Profit",
                FinancialStatementType.INCOME_STATEMENT, "DECIMAL", FinancialUnit.VND,
                "Lợi nhuận gộp về bán hàng và cung cấp dịch vụ"
        ));

        // 11. COST_OF_GOODS_SOLD
        map.put("COST_OF_GOODS_SOLD", seedIndicator(
                "COST_OF_GOODS_SOLD", "Giá vốn hàng bán", "Cost of Goods Sold",
                FinancialStatementType.INCOME_STATEMENT, "DECIMAL", FinancialUnit.VND,
                "Giá vốn hàng bán"
        ));

        // 12. INVENTORY_BEGINNING
        map.put("INVENTORY_BEGINNING", seedIndicator(
                "INVENTORY_BEGINNING", "Hàng tồn kho đầu kỳ", "Beginning Inventory",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Hàng tồn kho đầu kỳ báo cáo"
        ));

        // 13. INVENTORY_ENDING
        map.put("INVENTORY_ENDING", seedIndicator(
                "INVENTORY_ENDING", "Hàng tồn kho cuối kỳ", "Ending Inventory",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Hàng tồn kho cuối kỳ báo cáo"
        ));

        // 14. RECEIVABLES_BEGINNING
        map.put("RECEIVABLES_BEGINNING", seedIndicator(
                "RECEIVABLES_BEGINNING", "Phải thu ngắn hạn đầu kỳ", "Beginning Short-Term Receivables",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Các khoản phải thu ngắn hạn đầu kỳ"
        ));

        // 15. RECEIVABLES_ENDING
        map.put("RECEIVABLES_ENDING", seedIndicator(
                "RECEIVABLES_ENDING", "Phải thu ngắn hạn cuối kỳ", "Ending Short-Term Receivables",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Các khoản phải thu ngắn hạn cuối kỳ"
        ));

        // 16. PAYABLES_BEGINNING
        map.put("PAYABLES_BEGINNING", seedIndicator(
                "PAYABLES_BEGINNING", "Phải trả ngắn hạn đầu kỳ", "Beginning Short-Term Payables",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Các khoản phải trả ngắn hạn đầu kỳ"
        ));

        // 17. PAYABLES_ENDING
        map.put("PAYABLES_ENDING", seedIndicator(
                "PAYABLES_ENDING", "Phải trả ngắn hạn cuối kỳ", "Ending Short-Term Payables",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Các khoản phải trả ngắn hạn cuối kỳ"
        ));

        // 18. TOTAL_ASSETS_BEGINNING
        map.put("TOTAL_ASSETS_BEGINNING", seedIndicator(
                "TOTAL_ASSETS_BEGINNING", "Tổng tài sản đầu kỳ", "Beginning Total Assets",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Tổng tài sản đầu kỳ báo cáo"
        ));

        // 19. TOTAL_EQUITY_BEGINNING
        map.put("TOTAL_EQUITY_BEGINNING", seedIndicator(
                "TOTAL_EQUITY_BEGINNING", "Vốn chủ sở hữu đầu kỳ", "Beginning Total Equity",
                FinancialStatementType.BALANCE_SHEET, "DECIMAL", FinancialUnit.VND,
                "Vốn chủ sở hữu đầu kỳ báo cáo"
        ));

        return map;
    }

    private FinancialIndicator seedIndicator(
            String code, String name, String nameEn,
            FinancialStatementType statementType, String dataType, FinancialUnit unit,
            String description
    ) {
        return indicatorRepository.findByIndicatorCode(code).orElseGet(() -> {
            log.info("Seeding financial indicator: {}", code);
            FinancialIndicator entity = FinancialIndicator.builder()
                    .indicatorCode(code)
                    .indicatorName(name)
                    .indicatorNameEn(nameEn)
                    .statementType(statementType)
                    .dataType(dataType)
                    .unit(unit)
                    .description(description)
                    .isActive(true)
                    .build();
            return indicatorRepository.save(entity);
        });
    }

    /**
     * Seeds the 14 initial financial ratio definitions and their input dependencies.
     */
    private void initializeRatios(Map<String, FinancialIndicator> indicators) {
        // --- LIQUIDITY ---
        seedRatioWithInputs(
                "CURRENT_RATIO", "Chỉ số thanh toán hiện hành", "Current Ratio",
                FinancialRatioCategory.LIQUIDITY, "CURRENT_ASSETS / CURRENT_LIABILITIES",
                FinancialUnit.TIMES, EvaluationDirection.HIGHER_IS_BETTER, 2, true,
                "Đo lường khả năng thanh toán các khoản nợ ngắn hạn bằng tài sản ngắn hạn",
                List.of(
                        new InputDef(indicators.get("CURRENT_ASSETS"), "CURRENT_ASSETS", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("CURRENT_LIABILITIES"), "CURRENT_LIABILITIES", 2, true, PeriodReference.CURRENT)
                )
        );

        seedRatioWithInputs(
                "QUICK_RATIO", "Chỉ số thanh toán nhanh", "Quick Ratio",
                FinancialRatioCategory.LIQUIDITY, "(CURRENT_ASSETS - INVENTORY) / CURRENT_LIABILITIES",
                FinancialUnit.TIMES, EvaluationDirection.HIGHER_IS_BETTER, 2, false,
                "Đo lường khả năng thanh toán nhanh các khoản nợ ngắn hạn không phụ thuộc hàng tồn kho",
                List.of(
                        new InputDef(indicators.get("CURRENT_ASSETS"), "CURRENT_ASSETS", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("INVENTORY"), "INVENTORY", 2, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("CURRENT_LIABILITIES"), "CURRENT_LIABILITIES", 3, true, PeriodReference.CURRENT)
                )
        );

        seedRatioWithInputs(
                "CASH_RATIO", "Chỉ số thanh toán tiền mặt", "Cash Ratio",
                FinancialRatioCategory.LIQUIDITY, "CASH_AND_EQUIVALENTS / CURRENT_LIABILITIES",
                FinancialUnit.TIMES, EvaluationDirection.HIGHER_IS_BETTER, 2, false,
                "Đo lường khả năng thanh toán tức thời bằng tiền và tương đương tiền",
                List.of(
                        new InputDef(indicators.get("CASH_AND_EQUIVALENTS"), "CASH_AND_EQUIVALENTS", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("CURRENT_LIABILITIES"), "CURRENT_LIABILITIES", 2, true, PeriodReference.CURRENT)
                )
        );

        // --- SOLVENCY ---
        seedRatioWithInputs(
                "DEBT_TO_EQUITY", "Hệ số nợ trên vốn chủ sở hữu", "Debt to Equity Ratio",
                FinancialRatioCategory.SOLVENCY, "TOTAL_LIABILITIES / EQUITY",
                FinancialUnit.TIMES, EvaluationDirection.LOWER_IS_BETTER, 2, true,
                "Đo lường mức độ đòn bẩy tài chính và tỷ lệ tài trợ nợ so với vốn chủ",
                List.of(
                        new InputDef(indicators.get("TOTAL_LIABILITIES"), "TOTAL_LIABILITIES", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("EQUITY"), "EQUITY", 2, true, PeriodReference.CURRENT)
                )
        );

        seedRatioWithInputs(
                "DEBT_RATIO", "Hệ số nợ", "Debt Ratio",
                FinancialRatioCategory.SOLVENCY, "TOTAL_LIABILITIES / TOTAL_ASSETS",
                FinancialUnit.PERCENT, EvaluationDirection.LOWER_IS_BETTER, 2, false,
                "Tỷ trọng nợ phải trả trong tổng nguồn vốn/tài sản của doanh nghiệp",
                List.of(
                        new InputDef(indicators.get("TOTAL_LIABILITIES"), "TOTAL_LIABILITIES", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("TOTAL_ASSETS"), "TOTAL_ASSETS", 2, true, PeriodReference.CURRENT)
                )
        );

        seedRatioWithInputs(
                "EQUITY_RATIO", "Hệ số tự tài trợ", "Equity Ratio",
                FinancialRatioCategory.SOLVENCY, "EQUITY / TOTAL_ASSETS",
                FinancialUnit.PERCENT, EvaluationDirection.HIGHER_IS_BETTER, 2, false,
                "Tỷ trọng vốn chủ sở hữu trong tổng tài sản của doanh nghiệp",
                List.of(
                        new InputDef(indicators.get("EQUITY"), "EQUITY", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("TOTAL_ASSETS"), "TOTAL_ASSETS", 2, true, PeriodReference.CURRENT)
                )
        );

        // --- PROFITABILITY ---
        seedRatioWithInputs(
                "ROA", "Tỷ suất sinh lời trên tổng tài sản", "Return on Assets",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / TOTAL_ASSETS * 100",
                FinancialUnit.PERCENT, EvaluationDirection.HIGHER_IS_BETTER, 2, true,
                "Đo lường hiệu quả sinh lời trên mỗi đồng tài sản cuối kỳ",
                List.of(
                        new InputDef(indicators.get("NET_PROFIT"), "NET_PROFIT", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("TOTAL_ASSETS"), "TOTAL_ASSETS", 2, true, PeriodReference.ENDING)
                )
        );

        seedRatioWithInputs(
                "ROE", "Tỷ suất sinh lời trên vốn chủ sở hữu", "Return on Equity",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / EQUITY * 100",
                FinancialUnit.PERCENT, EvaluationDirection.HIGHER_IS_BETTER, 2, true,
                "Đo lường hiệu quả sinh lời trên mỗi đồng vốn chủ sở hữu cuối kỳ",
                List.of(
                        new InputDef(indicators.get("NET_PROFIT"), "NET_PROFIT", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("EQUITY"), "EQUITY", 2, true, PeriodReference.ENDING)
                )
        );

        seedRatioWithInputs(
                "NET_PROFIT_MARGIN", "Tỷ suất lợi nhuận ròng", "Net Profit Margin",
                FinancialRatioCategory.PROFITABILITY, "NET_PROFIT / NET_REVENUE * 100",
                FinancialUnit.PERCENT, EvaluationDirection.HIGHER_IS_BETTER, 2, false,
                "Tỷ lệ lợi nhuận sau thuế tạo ra trên mỗi đồng doanh thu thuần",
                List.of(
                        new InputDef(indicators.get("NET_PROFIT"), "NET_PROFIT", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("NET_REVENUE"), "NET_REVENUE", 2, true, PeriodReference.CURRENT)
                )
        );

        seedRatioWithInputs(
                "GROSS_PROFIT_MARGIN", "Tỷ suất lợi nhuận gộp", "Gross Profit Margin",
                FinancialRatioCategory.PROFITABILITY, "GROSS_PROFIT / NET_REVENUE * 100",
                FinancialUnit.PERCENT, EvaluationDirection.HIGHER_IS_BETTER, 2, false,
                "Tỷ lệ lợi nhuận gộp tạo ra trên mỗi đồng doanh thu thuần",
                List.of(
                        new InputDef(indicators.get("GROSS_PROFIT"), "GROSS_PROFIT", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("NET_REVENUE"), "NET_REVENUE", 2, true, PeriodReference.CURRENT)
                )
        );

        // --- EFFICIENCY ---
        seedRatioWithInputs(
                "ASSET_TURNOVER", "Vòng quay tổng tài sản", "Total Asset Turnover",
                FinancialRatioCategory.EFFICIENCY, "NET_REVENUE / TOTAL_ASSETS",
                FinancialUnit.TIMES, EvaluationDirection.CONTEXT_DEPENDENT, 2, false,
                "Đo lường số vòng quay của tổng tài sản cuối kỳ tạo ra doanh thu",
                List.of(
                        new InputDef(indicators.get("NET_REVENUE"), "NET_REVENUE", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("TOTAL_ASSETS"), "TOTAL_ASSETS", 2, true, PeriodReference.ENDING)
                )
        );

        seedRatioWithInputs(
                "INVENTORY_TURNOVER", "Vòng quay hàng tồn kho", "Inventory Turnover",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_INVENTORY",
                FinancialUnit.TIMES, EvaluationDirection.CONTEXT_DEPENDENT, 2, false,
                "Đo lường số lần hàng tồn kho bình quân được luân chuyển trong kỳ",
                List.of(
                        new InputDef(indicators.get("COST_OF_GOODS_SOLD"), "COST_OF_GOODS_SOLD", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("INVENTORY"), "INVENTORY_BEGINNING", 2, true, PeriodReference.BEGINNING),
                        new InputDef(indicators.get("INVENTORY"), "INVENTORY_ENDING", 3, true, PeriodReference.ENDING)
                )
        );

        seedRatioWithInputs(
                "RECEIVABLES_TURNOVER", "Vòng quay các khoản phải thu", "Receivables Turnover",
                FinancialRatioCategory.EFFICIENCY, "NET_REVENUE / AVERAGE_RECEIVABLES",
                FinancialUnit.TIMES, EvaluationDirection.CONTEXT_DEPENDENT, 2, false,
                "Đo lường tốc độ thu hồi các khoản phải thu khách hàng",
                List.of(
                        new InputDef(indicators.get("NET_REVENUE"), "NET_REVENUE", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("RECEIVABLES_BEGINNING"), "RECEIVABLES_BEGINNING", 2, true, PeriodReference.BEGINNING),
                        new InputDef(indicators.get("RECEIVABLES_ENDING"), "RECEIVABLES_ENDING", 3, true, PeriodReference.ENDING)
                )
        );

        seedRatioWithInputs(
                "PAYABLES_TURNOVER", "Vòng quay các khoản phải trả", "Payables Turnover",
                FinancialRatioCategory.EFFICIENCY, "COST_OF_GOODS_SOLD / AVERAGE_PAYABLES",
                FinancialUnit.TIMES, EvaluationDirection.CONTEXT_DEPENDENT, 2, false,
                "Đo lường tốc độ thanh toán các khoản phải trả người bán",
                List.of(
                        new InputDef(indicators.get("COST_OF_GOODS_SOLD"), "COST_OF_GOODS_SOLD", 1, true, PeriodReference.CURRENT),
                        new InputDef(indicators.get("PAYABLES_BEGINNING"), "PAYABLES_BEGINNING", 2, true, PeriodReference.BEGINNING),
                        new InputDef(indicators.get("PAYABLES_ENDING"), "PAYABLES_ENDING", 3, true, PeriodReference.ENDING)
                )
        );
    }

    private record InputDef(
            FinancialIndicator indicator,
            String alias,
            int order,
            boolean required,
            PeriodReference periodReference
    ) {}

    private void seedRatioWithInputs(
            String code, String name, String nameEn,
            FinancialRatioCategory category, String formula,
            FinancialUnit unit, EvaluationDirection direction,
            int decimalScale, boolean isCoreKpi, String description,
            List<InputDef> inputDefs
    ) {
        FinancialRatioDefinition ratio = ratioRepository.findByRatioCode(code).orElseGet(() -> {
            log.info("Seeding financial ratio: {}", code);
            FinancialRatioDefinition entity = FinancialRatioDefinition.builder()
                    .ratioCode(code)
                    .ratioName(name)
                    .ratioNameEn(nameEn)
                    .category(category)
                    .formulaExpression(formula)
                    .unit(unit)
                    .evaluationDirection(direction)
                    .decimalScale(decimalScale)
                    .isCoreKpi(isCoreKpi)
                    .description(description)
                    .isActive(true)
                    .build();
            return ratioRepository.save(entity);
        });

        // Seed inputs if not already present
        List<FinancialRatioInput> existingInputs = ratioInputRepository.findByRatioDefinitionIdOrderByInputOrderAsc(ratio.getId());
        if (existingInputs.isEmpty() && inputDefs != null) {
            for (InputDef def : inputDefs) {
                if (def.indicator() != null) {
                    FinancialRatioInput input = FinancialRatioInput.builder()
                            .ratioDefinition(ratio)
                            .indicator(def.indicator())
                            .inputAlias(def.alias())
                            .inputOrder(def.order())
                            .required(def.required())
                            .periodReference(def.periodReference())
                            .build();
                    ratioInputRepository.save(input);
                }
            }
        }
    }

    /**
     * Seeds verified accounting indicator mappings for TT200, TT133, and TT99.
     */
    private void initializeAccountingMappings(Map<String, FinancialIndicator> indicators) {
        // Register verified mappings for TT200 and TT133
        for (AccountingStandard standard : List.of(AccountingStandard.TT200, AccountingStandard.TT133)) {
            // Balance Sheet
            seedMapping(indicators.get("CURRENT_ASSETS"), standard, FinancialStatementType.BALANCE_SHEET, "100", "CURRENT_ASSETS", "Tài sản ngắn hạn");
            seedMapping(indicators.get("CASH_AND_EQUIVALENTS"), standard, FinancialStatementType.BALANCE_SHEET, "110", "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền");
            seedMapping(indicators.get("INVENTORY"), standard, FinancialStatementType.BALANCE_SHEET, "140", "INVENTORIES", "Hàng tồn kho");
            seedMapping(indicators.get("TOTAL_ASSETS"), standard, FinancialStatementType.BALANCE_SHEET, "280", "TOTAL_ASSETS", "Tổng cộng tài sản");
            seedMapping(indicators.get("TOTAL_ASSETS"), standard, FinancialStatementType.BALANCE_SHEET, "270", "TOTAL_ASSETS", "Tổng tài sản");
            seedMapping(indicators.get("TOTAL_LIABILITIES"), standard, FinancialStatementType.BALANCE_SHEET, "300", "TOTAL_LIABILITIES", "Nợ phải trả");
            seedMapping(indicators.get("CURRENT_LIABILITIES"), standard, FinancialStatementType.BALANCE_SHEET, "310", "CURRENT_LIABILITIES", "Nợ ngắn hạn");
            seedMapping(indicators.get("EQUITY"), standard, FinancialStatementType.BALANCE_SHEET, "400", "EQUITY", "Vốn chủ sở hữu");
            seedMapping(indicators.get("RECEIVABLES_ENDING"), standard, FinancialStatementType.BALANCE_SHEET, "130", "SHORT_TERM_RECEIVABLES", "Phải thu ngắn hạn");
            seedMapping(indicators.get("PAYABLES_ENDING"), standard, FinancialStatementType.BALANCE_SHEET, "311", "SHORT_TERM_PAYABLES", "Phải trả người bán ngắn hạn");

            // Income Statement
            seedMapping(indicators.get("NET_REVENUE"), standard, FinancialStatementType.INCOME_STATEMENT, "10", "NET_REVENUE", "Doanh thu thuần về bán hàng và cung cấp dịch vụ");
            seedMapping(indicators.get("COST_OF_GOODS_SOLD"), standard, FinancialStatementType.INCOME_STATEMENT, "11", "COST_OF_GOODS_SOLD", "Giá vốn hàng bán");
            seedMapping(indicators.get("GROSS_PROFIT"), standard, FinancialStatementType.INCOME_STATEMENT, "20", "GROSS_PROFIT", "Lợi nhuận gộp về bán hàng và cung cấp dịch vụ");
            seedMapping(indicators.get("NET_PROFIT"), standard, FinancialStatementType.INCOME_STATEMENT, "60", "NET_PROFIT", "Lợi nhuận sau thuế thu nhập doanh nghiệp");
        }

        // TT99 Standard
        AccountingStandard tt99 = AccountingStandard.TT99;
        seedMapping(indicators.get("CURRENT_ASSETS"), tt99, FinancialStatementType.BALANCE_SHEET, "100", "CURRENT_ASSETS", "Tài sản ngắn hạn");
        seedMapping(indicators.get("CASH_AND_EQUIVALENTS"), tt99, FinancialStatementType.BALANCE_SHEET, "110", "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền");
        seedMapping(indicators.get("INVENTORY"), tt99, FinancialStatementType.BALANCE_SHEET, "140", "INVENTORIES", "Hàng tồn kho");
        seedMapping(indicators.get("TOTAL_ASSETS"), tt99, FinancialStatementType.BALANCE_SHEET, "280", "TOTAL_ASSETS", "Tổng cộng tài sản");
        seedMapping(indicators.get("TOTAL_LIABILITIES"), tt99, FinancialStatementType.BALANCE_SHEET, "300", "TOTAL_LIABILITIES", "Nợ phải trả");
        seedMapping(indicators.get("CURRENT_LIABILITIES"), tt99, FinancialStatementType.BALANCE_SHEET, "310", "CURRENT_LIABILITIES", "Nợ ngắn hạn");
        seedMapping(indicators.get("EQUITY"), tt99, FinancialStatementType.BALANCE_SHEET, "400", "EQUITY", "Vốn chủ sở hữu");
        seedMapping(indicators.get("NET_REVENUE"), tt99, FinancialStatementType.INCOME_STATEMENT, "10", "NET_REVENUE", "Doanh thu thuần");
        seedMapping(indicators.get("COST_OF_GOODS_SOLD"), tt99, FinancialStatementType.INCOME_STATEMENT, "11", "COST_OF_GOODS_SOLD", "Giá vốn hàng bán");
        seedMapping(indicators.get("GROSS_PROFIT"), tt99, FinancialStatementType.INCOME_STATEMENT, "20", "GROSS_PROFIT", "Lợi nhuận gộp");
        seedMapping(indicators.get("NET_PROFIT"), tt99, FinancialStatementType.INCOME_STATEMENT, "60", "NET_PROFIT", "Lợi nhuận sau thuế");
    }

    private void seedMapping(
            FinancialIndicator indicator,
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceCode,
            String sourceKey,
            String sourceLabel
    ) {
        if (indicator == null) {
            return;
        }
        boolean exists = mappingRepository.existsByAccountingStandardAndStatementTypeAndSourceCode(standard, statementType, sourceCode);
        if (!exists) {
            log.info("Seeding mapping: standard={}, type={}, code={}, key={}", standard, statementType, sourceCode, sourceKey);
            AccountingIndicatorMapping mapping = AccountingIndicatorMapping.builder()
                    .indicator(indicator)
                    .accountingStandard(standard)
                    .statementType(statementType)
                    .sourceCode(sourceCode)
                    .sourceKey(sourceKey)
                    .sourceLabel(sourceLabel)
                    .mappingStatus(MappingStatus.ACTIVE)
                    .effectiveFrom(LocalDate.of(2015, 1, 1))
                    .build();
            mappingRepository.save(mapping);
        }
    }
}

