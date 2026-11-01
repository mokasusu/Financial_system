package com.financial.modules.validation.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.calculationengine.service.IndicatorResolverService;
import com.financial.modules.calculationengine.service.impl.IndicatorResolverServiceImpl;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.PeriodReference;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.enums.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidationEngineValidatorsTest {

    @Mock
    private AccountingIndicatorMappingRepository mappingRepository;

    @Mock
    private FinancialStatementItemRepository itemRepository;

    private IndicatorResolverService indicatorResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AccountingEquationValidator equationValidator;
    private BalanceSheetValidator balanceSheetValidator;
    private IncomeStatementValidator incomeStatementValidator;
    private CrossPeriodValidator crossPeriodValidator;
    private OcrDataQualityValidator ocrQualityValidator;
    private ConfirmedDataValidator confirmedDataValidator;

    private FinancialStatement statement;
    private List<FinancialStatementItem> statementItems;

    @BeforeEach
    void setUp() {
        indicatorResolver = new IndicatorResolverServiceImpl(mappingRepository, itemRepository);
        equationValidator = new AccountingEquationValidator(indicatorResolver);
        balanceSheetValidator = new BalanceSheetValidator(indicatorResolver, objectMapper);
        incomeStatementValidator = new IncomeStatementValidator(indicatorResolver, objectMapper);
        crossPeriodValidator = new CrossPeriodValidator(indicatorResolver, objectMapper);
        ocrQualityValidator = new OcrDataQualityValidator(objectMapper);
        confirmedDataValidator = new ConfirmedDataValidator(indicatorResolver, objectMapper);

        statement = FinancialStatement.builder()
                .id(100L)
                .accountingStandard("TT200")
                .processingStatus(FinancialStatementStatus.DATA_CONFIRMED)
                .build();

        statementItems = new ArrayList<>();
        // Balance sheet items: Total Assets (100B) = Liabilities (40B) + Equity (60B)
        statementItems.add(createItem("100", "CURRENT_ASSETS", FinancialStatementType.BALANCE_SHEET, "50000000000", "40000000000", "0.95", MappingStatus.MATCHED));
        statementItems.add(createItem("200", "NON_CURRENT_ASSETS", FinancialStatementType.BALANCE_SHEET, "50000000000", "50000000000", "0.90", MappingStatus.MATCHED));
        statementItems.add(createItem("280", "TOTAL_ASSETS", FinancialStatementType.BALANCE_SHEET, "100000000000", "90000000000", "0.98", MappingStatus.MATCHED));
        statementItems.add(createItem("300", "TOTAL_LIABILITIES", FinancialStatementType.BALANCE_SHEET, "40000000000", "35000000000", "0.92", MappingStatus.MATCHED));
        statementItems.add(createItem("400", "EQUITY", FinancialStatementType.BALANCE_SHEET, "60000000000", "55000000000", "0.96", MappingStatus.MATCHED));
        statementItems.add(createItem("140", "INVENTORY", FinancialStatementType.BALANCE_SHEET, "15000000000", "12000000000", "0.88", MappingStatus.MATCHED));
        statementItems.add(createItem("110", "CASH_AND_EQUIVALENTS", FinancialStatementType.BALANCE_SHEET, "10000000000", "8000000000", "0.94", MappingStatus.MATCHED));

        // Income statement items: Gross profit (45B) = Revenue (120B) - COGS (75B)
        statementItems.add(createItem("10", "NET_REVENUE", FinancialStatementType.INCOME_STATEMENT, "120000000000", null, "0.97", MappingStatus.MATCHED));
        statementItems.add(createItem("11", "COST_OF_GOODS_SOLD", FinancialStatementType.INCOME_STATEMENT, "75000000000", null, "0.93", MappingStatus.MATCHED));
        statementItems.add(createItem("20", "GROSS_PROFIT", FinancialStatementType.INCOME_STATEMENT, "45000000000", null, "0.95", MappingStatus.MATCHED));
        statementItems.add(createItem("60", "NET_PROFIT", FinancialStatementType.INCOME_STATEMENT, "12000000000", null, "0.96", MappingStatus.MATCHED));

        statement.setItems(statementItems);
    }

    private FinancialStatementItem createItem(
            String code, String key, FinancialStatementType type,
            String current, String previous, String confidence, MappingStatus mappingStatus
    ) {
        return FinancialStatementItem.builder()
                .financialStatement(statement)
                .code(code)
                .key(key)
                .statementType(type)
                .currentValue(current != null ? new BigDecimal(current) : null)
                .previousValue(previous != null ? new BigDecimal(previous) : null)
                .confidence(confidence != null ? new BigDecimal(confidence) : null)
                .mappingStatus(mappingStatus)
                .unit("VND")
                .sourcePage(1)
                .build();
    }

    @Test
    @DisplayName("1. Balance sheet accounting equation passes when Total Assets = Liabilities + Equity")
    void testAccountingEquation_Pass() {
        ValidationRule rule = ValidationRule.builder()
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("Cân đối BCTC TT200")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .toleranceValue(BigDecimal.ZERO)
                .toleranceType(ToleranceType.ABSOLUTE)
                .build();

        ValidationResult result = equationValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASS);
        assertThat(result.getDifferenceValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getExpectedValue()).isEqualByComparingTo(new BigDecimal("100000000000"));
        assertThat(result.getActualValue()).isEqualByComparingTo(new BigDecimal("100000000000"));
    }

    @Test
    @DisplayName("2. Balance sheet accounting equation fails when unbalanced")
    void testAccountingEquation_Fail() {
        // Change total assets to 105B (5B discrepancy)
        statementItems.stream()
                .filter(i -> "TOTAL_ASSETS".equals(i.getKey()))
                .findFirst()
                .ifPresent(i -> i.setCurrentValue(new BigDecimal("105000000000")));

        ValidationRule rule = ValidationRule.builder()
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("Cân đối BCTC TT200")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .toleranceValue(BigDecimal.ZERO)
                .toleranceType(ToleranceType.ABSOLUTE)
                .build();

        ValidationResult result = equationValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.FAIL);
        assertThat(result.getDifferenceValue()).isEqualByComparingTo(new BigDecimal("5000000000"));
        assertThat(result.getExplanation()).contains("không cân đối");
    }

    @Test
    @DisplayName("3. Accounting equation tolerance configuration works for absolute and percentage thresholds")
    void testAccountingEquation_Tolerance() {
        // Difference is 500 VND
        statementItems.stream()
                .filter(i -> "TOTAL_ASSETS".equals(i.getKey()))
                .findFirst()
                .ifPresent(i -> i.setCurrentValue(new BigDecimal("100000000500")));

        // Rule with absolute tolerance = 1000 VND
        ValidationRule ruleAbsolute = ValidationRule.builder()
                .ruleCode("BS_EQ_TOL_ABS")
                .ruleName("Dung sai tuyệt đối")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.HIGH)
                .toleranceValue(new BigDecimal("1000"))
                .toleranceType(ToleranceType.ABSOLUTE)
                .build();

        ValidationResult resAbs = equationValidator.validate(statement, ruleAbsolute);
        assertThat(resAbs.getStatus()).isEqualTo(ValidationStatus.PASS);

        // Rule with percentage tolerance = 0.001%
        ValidationRule rulePercent = ValidationRule.builder()
                .ruleCode("BS_EQ_TOL_PCT")
                .ruleName("Dung sai phần trăm")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.HIGH)
                .toleranceValue(new BigDecimal("0.001"))
                .toleranceType(ToleranceType.PERCENTAGE)
                .build();

        ValidationResult resPct = equationValidator.validate(statement, rulePercent);
        assertThat(resPct.getStatus()).isEqualTo(ValidationStatus.PASS);
    }

    @Test
    @DisplayName("4. Balance sheet subtotal passes when components equal parent total")
    void testBalanceSheetSubtotal_Pass() {
        ValidationRule rule = ValidationRule.builder()
                .ruleCode("BS_SUB_01")
                .ruleName("Tổng tài sản = Tài sản ngắn hạn + dài hạn")
                .category(ValidationCategory.BALANCE_SHEET_SUBTOTAL)
                .severity(ValidationSeverity.HIGH)
                .configuration("{\"parent\": \"TOTAL_ASSETS\", \"components\": [\"CURRENT_ASSETS\", \"NON_CURRENT_ASSETS\"]}")
                .toleranceValue(BigDecimal.ZERO)
                .toleranceType(ToleranceType.ABSOLUTE)
                .build();

        ValidationResult result = balanceSheetValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASS);
        assertThat(result.getDifferenceValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("5. Income statement subtotal passes when Gross Profit = Net Revenue - COGS")
    void testIncomeStatementSubtotal_Pass() {
        ValidationRule rule = ValidationRule.builder()
                .ruleCode("IS_GP_01")
                .ruleName("Lợi nhuận gộp = Doanh thu thuần - Giá vốn")
                .category(ValidationCategory.INCOME_STATEMENT)
                .severity(ValidationSeverity.HIGH)
                .configuration("{\"result\": \"GROSS_PROFIT\", \"minuend\": \"NET_REVENUE\", \"subtrahend\": \"COST_OF_GOODS_SOLD\"}")
                .toleranceValue(BigDecimal.ZERO)
                .toleranceType(ToleranceType.ABSOLUTE)
                .build();

        ValidationResult result = incomeStatementValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.PASS);
        assertThat(result.getDifferenceValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("6. Missing required indicator returns REVIEW_REQUIRED")
    void testMissingRequiredIndicator_ReturnsReviewRequired() {
        // Remove equity item
        statementItems.removeIf(i -> "EQUITY".equals(i.getKey()));

        ValidationRule rule = ValidationRule.builder()
                .ruleCode("BS_EQ_TT200_01")
                .ruleName("Cân đối BCTC TT200")
                .category(ValidationCategory.ACCOUNTING_EQUATION)
                .severity(ValidationSeverity.CRITICAL)
                .toleranceValue(BigDecimal.ZERO)
                .build();

        ValidationResult result = equationValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.REVIEW_REQUIRED);
        assertThat(result.getExplanation()).contains("thiếu chỉ tiêu");
    }

    @Test
    @DisplayName("7. Unmapped OCR items trigger WARNING in OCR quality validation")
    void testOcrDataQuality_UnmappedItems_ReturnsWarning() {
        // Add an UNMAPPED item
        statementItems.add(createItem("999", "UNKNOWN_ITEM", FinancialStatementType.BALANCE_SHEET, "1000000", null, "0.50", MappingStatus.UNMAPPED));

        ValidationRule rule = ValidationRule.builder()
                .ruleCode("OCR_QUAL_01")
                .ruleName("Chất lượng OCR")
                .category(ValidationCategory.OCR_DATA_QUALITY)
                .severity(ValidationSeverity.MEDIUM)
                .toleranceValue(new BigDecimal("0.70"))
                .build();

        ValidationResult result = ocrQualityValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.WARNING);
        assertThat(result.getExplanation()).contains("chưa ánh xạ chuẩn");
    }

    @Test
    @DisplayName("8. Low OCR confidence triggers WARNING in OCR quality validation")
    void testOcrDataQuality_LowConfidence_ReturnsWarning() {
        // Set confidence of Current Assets to 0.45 (< 0.70)
        statementItems.stream()
                .filter(i -> "CURRENT_ASSETS".equals(i.getKey()))
                .findFirst()
                .ifPresent(i -> i.setConfidence(new BigDecimal("0.45")));

        ValidationRule rule = ValidationRule.builder()
                .ruleCode("OCR_QUAL_01")
                .ruleName("Chất lượng OCR")
                .category(ValidationCategory.OCR_DATA_QUALITY)
                .severity(ValidationSeverity.MEDIUM)
                .toleranceValue(new BigDecimal("0.70"))
                .build();

        ValidationResult result = ocrQualityValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.WARNING);
        assertThat(result.getExplanation()).contains("độ tin cậy thấp");
    }

    @Test
    @DisplayName("9. Cross-period validation detects unexpected negative values")
    void testCrossPeriod_NegativeValue_ReturnsWarning() {
        // Set Inventory to negative -5B
        statementItems.stream()
                .filter(i -> "INVENTORY".equals(i.getKey()))
                .findFirst()
                .ifPresent(i -> i.setCurrentValue(new BigDecimal("-5000000000")));

        ValidationRule rule = ValidationRule.builder()
                .ruleCode("CP_NEG_01")
                .ruleName("Kiểm tra giá trị âm")
                .category(ValidationCategory.CROSS_PERIOD)
                .severity(ValidationSeverity.HIGH)
                .configuration("{\"checkType\": \"NEGATIVE_CHECK\", \"indicators\": [\"INVENTORY\", \"TOTAL_ASSETS\"]}")
                .build();

        ValidationResult result = crossPeriodValidator.validate(statement, rule);

        assertThat(result.getStatus()).isEqualTo(ValidationStatus.WARNING);
        assertThat(result.getExplanation()).contains("giá trị âm bất thường");
    }

    @Test
    @DisplayName("10. Confirmed data validator verifies required core indicators completeness")
    void testConfirmedDataValidator_Completeness() {
        ValidationRule rule = ValidationRule.builder()
                .ruleCode("CONF_REQ_01")
                .ruleName("Đầy đủ chỉ tiêu bắt buộc")
                .category(ValidationCategory.CONFIRMED_DATA)
                .severity(ValidationSeverity.HIGH)
                .configuration("{\"requiredIndicators\": [\"TOTAL_ASSETS\", \"CURRENT_ASSETS\", \"TOTAL_LIABILITIES\", \"EQUITY\", \"NET_REVENUE\", \"NET_PROFIT\"]}")
                .build();

        ValidationResult resPass = confirmedDataValidator.validate(statement, rule);
        assertThat(resPass.getStatus()).isEqualTo(ValidationStatus.PASS);

        // Remove Net Profit
        statementItems.removeIf(i -> "NET_PROFIT".equals(i.getKey()));
        ValidationResult resFail = confirmedDataValidator.validate(statement, rule);
        assertThat(resFail.getStatus()).isEqualTo(ValidationStatus.FAIL);
        assertThat(resFail.getAffectedIndicators()).contains("NET_PROFIT");
    }
}
