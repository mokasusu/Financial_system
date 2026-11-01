package com.financial.modules.financialstatement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialIndicatorMappingServiceTest {

    private FinancialIndicatorMappingService mappingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private FinancialStatement statement;

    @BeforeEach
    void setUp() {
        mappingService = new FinancialIndicatorMappingService(objectMapper);
        statement = FinancialStatement.builder()
                .id(1L)
                .fileName("bctc_2023.pdf")
                .build();
    }

    @Test
    @DisplayName("Should correctly map all 25 core indicators under TT200")
    void mapAll25Indicators_TT200_Success() throws Exception {
        String json = """
        {
          "success": true,
          "request_id": "req-12345",
          "accounting_standard": {"code": "TT200"},
          "document": {
            "fiscal_year": 2023,
            "period": "current",
            "unit": "VND"
          },
          "statements": {
            "balance_sheet": [
              {"code": "100", "raw_label": "Tài sản ngắn hạn", "current_value": 29403000000000, "previous_value": 28150000000000},
              {"code": "110", "raw_label": "Tiền và các khoản tương đương tiền", "current_value": 5000000000, "previous_value": 4500000000},
              {"code": "120", "raw_label": "Các khoản đầu tư tài chính ngắn hạn", "current_value": 1000000000, "previous_value": 800000000},
              {"code": "130", "raw_label": "Các khoản phải thu ngắn hạn", "current_value": 3000000000, "previous_value": 2500000000},
              {"code": "140", "raw_label": "Hàng tồn kho", "current_value": 4000000000, "previous_value": 3800000000},
              {"code": "200", "raw_label": "Tài sản dài hạn", "current_value": 15000000000, "previous_value": 14000000000},
              {"code": "220", "raw_label": "Tài sản cố định", "current_value": 8000000000, "previous_value": 7500000000},
              {"code": "280", "raw_label": "Tổng tài sản", "current_value": 44403000000000, "previous_value": 42150000000000},
              {"code": "300", "raw_label": "Nợ phải trả", "current_value": 20000000000, "previous_value": 19000000000},
              {"code": "310", "raw_label": "Nợ ngắn hạn", "current_value": 12000000000, "previous_value": 11000000000},
              {"code": "321", "raw_label": "Vay ngắn hạn", "current_value": 5000000000, "previous_value": 4800000000},
              {"code": "330", "raw_label": "Nợ dài hạn", "current_value": 8000000000, "previous_value": 8000000000},
              {"code": "400", "raw_label": "Vốn chủ sở hữu", "current_value": 24403000000000, "previous_value": 23150000000000}
            ],
            "income_statement": [
              {"code": "10", "raw_label": "Doanh thu thuần", "current_value": 50000000000, "previous_value": 45000000000},
              {"code": "11", "raw_label": "Giá vốn hàng bán", "current_value": 35000000000, "previous_value": 32000000000},
              {"code": "20", "raw_label": "Lợi nhuận gộp", "current_value": 15000000000, "previous_value": 13000000000},
              {"code": "23", "raw_label": "Chi phí tài chính", "current_value": 2000000000, "previous_value": 1800000000},
              {"code": "30", "raw_label": "Lợi nhuận thuần từ hoạt động kinh doanh", "current_value": 8000000000, "previous_value": 7000000000},
              {"code": "50", "raw_label": "Lợi nhuận kế toán trước thuế", "current_value": 9000000000, "previous_value": 8000000000},
              {"code": "60", "raw_label": "Lợi nhuận sau thuế", "current_value": 7200000000, "previous_value": 6400000000}
            ],
            "cash_flow": [
              {"code": "20", "raw_label": "Lưu chuyển tiền thuần từ hoạt động kinh doanh", "current_value": 6000000000, "previous_value": 5000000000},
              {"code": "30", "raw_label": "Lưu chuyển tiền thuần từ hoạt động đầu tư", "current_value": -3000000000, "previous_value": -2500000000},
              {"code": "40", "raw_label": "Lưu chuyển tiền thuần từ hoạt động tài chính", "current_value": -1000000000, "previous_value": -800000000},
              {"code": "50", "raw_label": "Lưu chuyển tiền thuần trong kỳ", "current_value": 2000000000, "previous_value": 1700000000},
              {"code": "70", "raw_label": "Tiền và các khoản tương đương tiền cuối kỳ", "current_value": 5000000000, "previous_value": 4500000000}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(items).hasSize(25);
        assertThat(statement.getAccountingStandard()).isEqualTo("TT200");
        assertThat(statement.getFiscalYear()).isEqualTo(2023);
        assertThat(statement.getOcrRequestId()).isEqualTo("req-12345");

        // Verify each item has draft initialized
        for (FinancialStatementItem item : items) {
            assertThat(item.getDraft()).isNotNull();
            assertThat(item.getDraft().getCurrentValue()).isEqualTo(item.getCurrentValue());
            assertThat(item.getDraft().getPreviousValue()).isEqualTo(item.getPreviousValue());
            assertThat(item.getDraft().getEdited()).isFalse();
            assertThat(item.getMappingStatus()).isEqualTo(MappingStatus.MATCHED);
        }
    }

    @Test
    @DisplayName("Should disambiguate identical codes in different statement types correctly")
    void sameCode_DifferentStatementTypes_MappedDistinctly() {
        String json = """
        {
          "accounting_standard": "TT200",
          "statements": {
            "income_statement": [
              {"code": "20", "raw_label": "Lợi nhuận gộp", "current_value": 15000000000}
            ],
            "cash_flow": [
              {"code": "20", "raw_label": "Lưu chuyển tiền thuần từ hoạt động kinh doanh", "current_value": 6000000000}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(items).hasSize(2);

        FinancialStatementItem isItem = items.stream()
                .filter(i -> i.getStatementType() == FinancialStatementType.INCOME_STATEMENT)
                .findFirst().orElseThrow();
        assertThat(isItem.getCode()).isEqualTo("20");
        assertThat(isItem.getKey()).isEqualTo("GROSS_PROFIT");
        assertThat(isItem.getLabel()).isEqualTo("Lợi nhuận gộp");

        FinancialStatementItem cfItem = items.stream()
                .filter(i -> i.getStatementType() == FinancialStatementType.CASH_FLOW)
                .findFirst().orElseThrow();
        assertThat(cfItem.getCode()).isEqualTo("20");
        assertThat(cfItem.getKey()).isEqualTo("NET_CASH_FROM_OPERATING");
        assertThat(cfItem.getLabel()).isEqualTo("Lưu chuyển tiền thuần từ hoạt động kinh doanh");
    }

    @Test
    @DisplayName("Should preserve negative values and accounting parentheses format")
    void negativeValues_Preserved() {
        String json = """
        {
          "accounting_standard": "TT200",
          "statements": {
            "cash_flow": [
              {"code": "30", "raw_label": "Lưu chuyển tiền thuần từ hoạt động đầu tư", "current_value": -1500000, "previous_value": "(2000000)"}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(items).hasSize(1);
        FinancialStatementItem item = items.get(0);
        assertThat(item.getCurrentValue()).isEqualByComparingTo(new BigDecimal("-1500000"));
        assertThat(item.getPreviousValue()).isEqualByComparingTo(new BigDecimal("-2000000"));
    }

    @Test
    @DisplayName("Should keep missing indicators absent without fabricating values or zeros")
    void missingIndicators_RemainAbsent() {
        String json = """
        {
          "accounting_standard": "TT200",
          "statements": {
            "balance_sheet": [
              {"code": "100", "raw_label": "Tài sản ngắn hạn", "current_value": 1000000}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getCode()).isEqualTo("100");
    }

    @Test
    @DisplayName("Should mark item as NEEDS_REVIEW when code matches but label conflicts significantly")
    void labelMismatch_SetsNeedsReview() {
        String json = """
        {
          "accounting_standard": "TT200",
          "statements": {
            "balance_sheet": [
              {"code": "100", "raw_label": "Khoản chi phí bất thường không xác định", "current_value": 1000000}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getMappingStatus()).isEqualTo(MappingStatus.NEEDS_REVIEW);
    }

    @Test
    @DisplayName("Should handle TT99 accounting standard without inappropriately mixing with TT200")
    void tt99_AccountingStandard_Handled() {
        String json = """
        {
          "accounting_standard": {"code": "TT99"},
          "statements": {
            "balance_sheet": [
              {"code": "100", "raw_label": "Tài sản ngắn hạn", "current_value": 5000000},
              {"code": "999", "raw_label": "Mục phụ TT99 chưa chuẩn hóa", "current_value": 100000}
            ]
          }
        }
        """;

        List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, json);

        assertThat(statement.getAccountingStandard()).isEqualTo("TT99");
        assertThat(items).hasSize(2);

        FinancialStatementItem item100 = items.stream().filter(i -> "100".equals(i.getCode())).findFirst().orElseThrow();
        assertThat(item100.getAccountingStandard()).isEqualTo("TT99");
        assertThat(item100.getMappingStatus()).isEqualTo(MappingStatus.MATCHED);

        FinancialStatementItem item999 = items.stream().filter(i -> "999".equals(i.getCode())).findFirst().orElseThrow();
        assertThat(item999.getMappingStatus()).isEqualTo(MappingStatus.NEEDS_REVIEW);
    }
}

