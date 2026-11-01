package com.financial.modules.validation.initializer;

import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ToleranceType;
import com.financial.modules.validation.enums.ValidationCategory;
import com.financial.modules.validation.enums.ValidationSeverity;
import com.financial.modules.validation.repository.ValidationRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@Order(3)
public class ValidationRuleDataInitializer implements ApplicationRunner {

    private final ValidationRuleRepository ruleRepository;

    public ValidationRuleDataInitializer(ValidationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting Validation Engine Module initial rule seeding...");

        // 1. TT200 Rules
        seedRule(
                "BS_EQ_TT200_01",
                "Cân đối Bảng cân đối kế toán TT200",
                "Kiểm tra phương trình kế toán cơ bản: Tổng tài sản = Tổng nợ phải trả + Vốn chủ sở hữu theo Thông tư 200/2014/TT-BTC",
                AccountingStandard.TT200,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.ACCOUNTING_EQUATION,
                ValidationSeverity.CRITICAL,
                null,
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "BS_SUB_TT200_01",
                "Tổng tài sản = Tài sản ngắn hạn + Tài sản dài hạn (TT200)",
                "Kiểm tra tổng phụ: Tổng tài sản (280) bằng Tài sản ngắn hạn (100) cộng Tài sản dài hạn (200)",
                AccountingStandard.TT200,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.BALANCE_SHEET_SUBTOTAL,
                ValidationSeverity.HIGH,
                "{\"parent\": \"TOTAL_ASSETS\", \"components\": [\"CURRENT_ASSETS\", \"NON_CURRENT_ASSETS\"]}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "IS_GP_TT200_01",
                "Lợi nhuận gộp = Doanh thu thuần - Giá vốn (TT200)",
                "Kiểm tra quan hệ kết quả kinh doanh: Lợi nhuận gộp (20) = Doanh thu thuần (10) - Giá vốn hàng bán (11)",
                AccountingStandard.TT200,
                FinancialStatementType.INCOME_STATEMENT,
                ValidationCategory.INCOME_STATEMENT,
                ValidationSeverity.HIGH,
                "{\"result\": \"GROSS_PROFIT\", \"minuend\": \"NET_REVENUE\", \"subtrahend\": \"COST_OF_GOODS_SOLD\"}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "CP_NEG_TT200_01",
                "Kiểm tra giá trị âm bất thường (TT200)",
                "Phát hiện giá trị âm bất thường trong các chỉ tiêu tài sản, tồn kho và doanh thu",
                AccountingStandard.TT200,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.CROSS_PERIOD,
                ValidationSeverity.HIGH,
                "{\"checkType\": \"NEGATIVE_CHECK\", \"indicators\": [\"TOTAL_ASSETS\", \"CURRENT_ASSETS\", \"INVENTORY\", \"CASH_AND_EQUIVALENTS\", \"NET_REVENUE\"]}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "CP_FLUC_TT200_01",
                "Cảnh báo biến động mạnh liên kỳ (TT200)",
                "Cảnh báo khi các chỉ tiêu tài sản và doanh thu có mức biến động liên kỳ vượt quá 200%",
                AccountingStandard.TT200,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.CROSS_PERIOD,
                ValidationSeverity.MEDIUM,
                "{\"checkType\": \"FLUCTUATION_CHECK\", \"indicators\": [\"TOTAL_ASSETS\", \"NET_REVENUE\", \"CURRENT_ASSETS\"], \"maxChangePercent\": 200.0}",
                new BigDecimal("200.0"),
                ToleranceType.PERCENTAGE
        );

        // 2. TT133 Rules
        seedRule(
                "BS_EQ_TT133_01",
                "Cân đối Bảng cân đối kế toán TT133",
                "Kiểm tra phương trình kế toán cơ bản: Tổng tài sản = Tổng nợ phải trả + Vốn chủ sở hữu theo Thông tư 133/2016/TT-BTC",
                AccountingStandard.TT133,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.ACCOUNTING_EQUATION,
                ValidationSeverity.CRITICAL,
                null,
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "IS_GP_TT133_01",
                "Lợi nhuận gộp = Doanh thu thuần - Giá vốn (TT133)",
                "Kiểm tra Lợi nhuận gộp = Doanh thu thuần - Giá vốn theo Thông tư 133",
                AccountingStandard.TT133,
                FinancialStatementType.INCOME_STATEMENT,
                ValidationCategory.INCOME_STATEMENT,
                ValidationSeverity.HIGH,
                "{\"result\": \"GROSS_PROFIT\", \"minuend\": \"NET_REVENUE\", \"subtrahend\": \"COST_OF_GOODS_SOLD\"}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        // 3. TT99 Rules (Circular 99/2025/TT-BTC)
        seedRule(
                "BS_EQ_TT99_01",
                "Cân đối Bảng cân đối kế toán TT99",
                "Kiểm tra phương trình cân đối kế toán: Tổng tài sản = Tổng nợ phải trả + Vốn chủ sở hữu theo Thông tư 99/2025/TT-BTC",
                AccountingStandard.TT99,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.ACCOUNTING_EQUATION,
                ValidationSeverity.CRITICAL,
                null,
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "BS_SUB_TT99_01",
                "Tổng tài sản = Tài sản ngắn hạn + Tài sản dài hạn (TT99)",
                "Kiểm tra tổng phụ: Tổng tài sản (280) bằng Tài sản ngắn hạn (100) cộng Tài sản dài hạn (200) theo TT99",
                AccountingStandard.TT99,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.BALANCE_SHEET_SUBTOTAL,
                ValidationSeverity.HIGH,
                "{\"parent\": \"TOTAL_ASSETS\", \"components\": [\"CURRENT_ASSETS\", \"NON_CURRENT_ASSETS\"]}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "IS_GP_TT99_01",
                "Lợi nhuận gộp = Doanh thu thuần - Giá vốn (TT99)",
                "Kiểm tra quan hệ kết quả kinh doanh: Lợi nhuận gộp (20) = Doanh thu thuần (10) - Giá vốn hàng bán (11) theo TT99",
                AccountingStandard.TT99,
                FinancialStatementType.INCOME_STATEMENT,
                ValidationCategory.INCOME_STATEMENT,
                ValidationSeverity.HIGH,
                "{\"result\": \"GROSS_PROFIT\", \"minuend\": \"NET_REVENUE\", \"subtrahend\": \"COST_OF_GOODS_SOLD\"}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "CP_NEG_TT99_01",
                "Kiểm tra giá trị âm bất thường (TT99)",
                "Phát hiện giá trị âm bất thường trong các chỉ tiêu tài sản, tồn kho và doanh thu theo TT99",
                AccountingStandard.TT99,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.CROSS_PERIOD,
                ValidationSeverity.HIGH,
                "{\"checkType\": \"NEGATIVE_CHECK\", \"indicators\": [\"TOTAL_ASSETS\", \"CURRENT_ASSETS\", \"INVENTORY\", \"CASH_AND_EQUIVALENTS\", \"NET_REVENUE\"]}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "CP_FLUC_TT99_01",
                "Cảnh báo biến động mạnh liên kỳ (TT99)",
                "Cảnh báo khi các chỉ tiêu tài sản và doanh thu có mức biến động liên kỳ vượt quá 200% theo TT99",
                AccountingStandard.TT99,
                FinancialStatementType.BALANCE_SHEET,
                ValidationCategory.CROSS_PERIOD,
                ValidationSeverity.MEDIUM,
                "{\"checkType\": \"FLUCTUATION_CHECK\", \"indicators\": [\"TOTAL_ASSETS\", \"NET_REVENUE\", \"CURRENT_ASSETS\"], \"maxChangePercent\": 200.0}",
                new BigDecimal("200.0"),
                ToleranceType.PERCENTAGE
        );

        // 4. Universal Rules
        seedRule(
                "CONF_REQ_01",
                "Đầy đủ các chỉ tiêu tài chính bắt buộc",
                "Kiểm tra báo cáo tài chính đã xác nhận có đầy đủ các chỉ tiêu trọng yếu: Tổng tài sản, Tài sản ngắn hạn, Nợ phải trả, Vốn chủ sở hữu, Doanh thu thuần, Lợi nhuận sau thuế",
                null,
                null,
                ValidationCategory.CONFIRMED_DATA,
                ValidationSeverity.HIGH,
                "{\"requiredIndicators\": [\"TOTAL_ASSETS\", \"CURRENT_ASSETS\", \"TOTAL_LIABILITIES\", \"EQUITY\", \"NET_REVENUE\", \"NET_PROFIT\"]}",
                BigDecimal.ZERO,
                ToleranceType.ABSOLUTE
        );

        seedRule(
                "OCR_QUAL_01",
                "Chất lượng và độ tin cậy trích xuất OCR",
                "Kiểm tra độ tin cậy trích xuất (tối thiểu 70%), trạng thái ánh xạ chỉ tiêu (không bị UNMAPPED) và các cảnh báo OCR gốc",
                null,
                null,
                ValidationCategory.OCR_DATA_QUALITY,
                ValidationSeverity.MEDIUM,
                null,
                new BigDecimal("0.70"),
                ToleranceType.ABSOLUTE
        );

        log.info("Validation Engine Module initial rule seeding completed successfully.");
    }

    private void seedRule(
            String code,
            String name,
            String description,
            AccountingStandard standard,
            FinancialStatementType statementType,
            ValidationCategory category,
            ValidationSeverity severity,
            String configuration,
            BigDecimal toleranceValue,
            ToleranceType toleranceType
    ) {
        if (ruleRepository.existsByRuleCode(code)) {
            log.debug("Validation rule '{}' already exists in database. Skipping seed.", code);
            return;
        }

        ValidationRule rule = ValidationRule.builder()
                .ruleCode(code)
                .ruleName(name)
                .description(description)
                .accountingStandard(standard)
                .statementType(statementType)
                .category(category)
                .severity(severity)
                .configuration(configuration)
                .toleranceValue(toleranceValue != null ? toleranceValue : BigDecimal.ZERO)
                .toleranceType(toleranceType != null ? toleranceType : ToleranceType.ABSOLUTE)
                .isActive(true)
                .build();

        ruleRepository.save(rule);
        log.info("Seeded initial validation rule: {}", code);
    }
}

