package com.financial.modules.financialstatement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FinancialIndicatorMappingService {

    private static final Logger log = LoggerFactory.getLogger(FinancialIndicatorMappingService.class);

    private final ObjectMapper objectMapper;

    // Registry of standard definitions: key is accountingStandard + ":" + statementType + ":" + code
    private final Map<String, StandardIndicatorDef> standardDefinitions = new HashMap<>();
    private final Map<String, StandardIndicatorDef> standardDefinitionsByKey = new HashMap<>();

    @Autowired
    public FinancialIndicatorMappingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        initializeStandardDefinitions();
    }

    public FinancialIndicatorMappingService() {
        this(new ObjectMapper());
    }

    public record StandardIndicatorDef(
            FinancialStatementType statementType,
            String accountingStandard,
            String code,
            String key,
            String label,
            List<String> keywords
    ) {}

    private void register(FinancialStatementType type, String standard, String code, String key, String label, String... keywords) {
        String lookupKey = buildLookupKey(standard, type, code);
        StandardIndicatorDef def = new StandardIndicatorDef(
                type, standard, code, key, label, Arrays.asList(keywords)
        );
        standardDefinitions.put(lookupKey, def);
        standardDefinitionsByKey.put(type.name() + ":" + key, def);
    }

    private String buildLookupKey(String standard, FinancialStatementType type, String code) {
        String std = standard != null ? standard.trim().toUpperCase() : "TT200";
        return std + ":" + type.name() + ":" + (code != null ? code.trim() : "");
    }

    private void initializeStandardDefinitions() {
        // Register for TT200, TT133, and TT99 (Circular 99/2025/TT-BTC)
        for (String standard : List.of("TT200", "TT133", "TT99")) {
            // 9.1 Balance Sheet Core & Extended
            register(FinancialStatementType.BALANCE_SHEET, standard, "100", "CURRENT_ASSETS", "Tài sản ngắn hạn", "ngắn hạn", "current asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "110", "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền", "tiền", "tương đương tiền", "cash");
            register(FinancialStatementType.BALANCE_SHEET, standard, "120", "SHORT_TERM_INVESTMENTS", "Các khoản đầu tư tài chính ngắn hạn", "đầu tư tài chính", "ngắn hạn", "investment");
            register(FinancialStatementType.BALANCE_SHEET, standard, "123", "HELD_TO_MATURITY_INVESTMENTS", "Đầu tư nắm giữ đến ngày đáo hạn", "đáo hạn", "nắm giữ");
            register(FinancialStatementType.BALANCE_SHEET, standard, "130", "SHORT_TERM_RECEIVABLES", "Các khoản phải thu ngắn hạn", "phải thu", "ngắn hạn", "receivable");
            register(FinancialStatementType.BALANCE_SHEET, standard, "131", "TRADE_RECEIVABLES", "Phải thu ngắn hạn của khách hàng", "phải thu khách hàng", "khách hàng");
            register(FinancialStatementType.BALANCE_SHEET, standard, "140", "INVENTORIES", "Hàng tồn kho", "tồn kho", "inventory", "inventories");
            register(FinancialStatementType.BALANCE_SHEET, standard, "160", "OTHER_CURRENT_ASSETS", "Tài sản ngắn hạn khác", "ngắn hạn khác");
            register(FinancialStatementType.BALANCE_SHEET, standard, "200", "NON_CURRENT_ASSETS", "Tài sản dài hạn", "dài hạn", "non-current asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "210", "LONG_TERM_RECEIVABLES", "Các khoản phải thu dài hạn", "phải thu dài hạn");
            register(FinancialStatementType.BALANCE_SHEET, standard, "220", "FIXED_ASSETS", "Tài sản cố định", "cố định", "fixed asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "260", "LONG_TERM_INVESTMENTS", "Đầu tư tài chính dài hạn", "đầu tư dài hạn");
            register(FinancialStatementType.BALANCE_SHEET, standard, "280", "TOTAL_ASSETS", "Tổng tài sản", "tổng tài sản", "tổng cộng tài sản", "total asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "300", "TOTAL_LIABILITIES", "Nợ phải trả", "nợ phải trả", "nợ", "liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "310", "CURRENT_LIABILITIES", "Nợ ngắn hạn", "ngắn hạn", "current liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "311", "TRADE_PAYABLES", "Phải trả người bán ngắn hạn", "phải trả người bán", "người bán");
            register(FinancialStatementType.BALANCE_SHEET, standard, "321", "SHORT_TERM_BORROWINGS", "Vay ngắn hạn", "vay", "thuê tài chính", "borrowing");
            register(FinancialStatementType.BALANCE_SHEET, standard, "330", "NON_CURRENT_LIABILITIES", "Nợ dài hạn", "dài hạn", "non-current liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "400", "EQUITY", "Vốn chủ sở hữu", "vốn chủ", "equity");
            register(FinancialStatementType.BALANCE_SHEET, standard, "411", "OWNERS_CAPITAL", "Vốn góp của chủ sở hữu", "vốn góp");
            register(FinancialStatementType.BALANCE_SHEET, standard, "420", "RETAINED_EARNINGS", "Lợi nhuận sau thuế chưa phân phối", "chưa phân phối", "lợi nhuận sau thuế");
            register(FinancialStatementType.BALANCE_SHEET, standard, "440", "TOTAL_EQUITY_AND_LIABILITIES", "Tổng cộng nguồn vốn", "tổng nguồn vốn", "tổng cộng nguồn vốn");

            // 9.2 Income Statement Core & Extended
            register(FinancialStatementType.INCOME_STATEMENT, standard, "01", "GROSS_REVENUE", "Doanh thu bán hàng và cung cấp dịch vụ", "doanh thu bán hàng", "bán hàng và cung cấp dịch vụ");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "02", "REVENUE_DEDUCTIONS", "Các khoản giảm trừ doanh thu", "giảm trừ doanh thu");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "10", "NET_REVENUE", "Doanh thu thuần", "doanh thu thuần", "bán hàng", "net revenue");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "11", "COST_OF_GOODS_SOLD", "Giá vốn hàng bán", "giá vốn", "cost of goods");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "20", "GROSS_PROFIT", "Lợi nhuận gộp", "lợi nhuận gộp", "gross profit");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "22", "FINANCIAL_INCOME", "Doanh thu hoạt động tài chính", "doanh thu tài chính");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "23", "FINANCIAL_EXPENSES", "Chi phí tài chính", "chi phí tài chính", "financial expense");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "24", "INTEREST_EXPENSES", "Chi phí lãi vay", "chi phí đi vay", "lãi vay");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "25", "SELLING_EXPENSES", "Chi phí bán hàng", "chi phí bán hàng");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "26", "ADMINISTRATIVE_EXPENSES", "Chi phí quản lý doanh nghiệp", "chi phí quản lý");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "30", "OPERATING_PROFIT", "Lợi nhuận thuần từ hoạt động kinh doanh", "thuần từ hđkd", "hoạt động kinh doanh", "operating profit");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "31", "OTHER_INCOME", "Thu nhập khác", "thu nhập khác");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "32", "OTHER_EXPENSES", "Chi phí khác", "chi phí khác");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "40", "NET_OTHER_PROFIT", "Lợi nhuận khác", "lợi nhuận khác");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "50", "PROFIT_BEFORE_TAX", "Lợi nhuận kế toán trước thuế", "trước thuế", "profit before tax");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "51", "INCOME_TAX_EXPENSE", "Chi phí thuế thu nhập doanh nghiệp hiện hành", "thuế tndn hiện hành");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "52", "DEFERRED_TAX_EXPENSE", "Chi phí thuế thu nhập doanh nghiệp hoãn lại", "thuế tndn hoãn lại");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "60", "NET_PROFIT", "Lợi nhuận sau thuế", "sau thuế", "net profit", "lợi nhuận sau thuế tndn");

            // 9.3 Cash Flow Statement Core & Extended
            register(FinancialStatementType.CASH_FLOW, standard, "20", "NET_CASH_FROM_OPERATING", "Lưu chuyển tiền thuần từ hoạt động kinh doanh", "hoạt động kinh doanh", "operating activities");
            register(FinancialStatementType.CASH_FLOW, standard, "30", "NET_CASH_FROM_INVESTING", "Lưu chuyển tiền thuần từ hoạt động đầu tư", "hoạt động đầu tư", "investing activities");
            register(FinancialStatementType.CASH_FLOW, standard, "33", "PROCEEDS_FROM_BORROWINGS", "Tiền thu từ đi vay", "thu từ đi vay");
            register(FinancialStatementType.CASH_FLOW, standard, "34", "REPAYMENT_OF_BORROWINGS", "Tiền trả nợ gốc vay", "trả nợ gốc vay");
            register(FinancialStatementType.CASH_FLOW, standard, "36", "DIVIDENDS_PAID", "Cổ tức, lợi nhuận đã trả cho chủ sở hữu", "cổ tức");
            register(FinancialStatementType.CASH_FLOW, standard, "40", "NET_CASH_FROM_FINANCING", "Lưu chuyển tiền thuần từ hoạt động tài chính", "hoạt động tài chính", "financing activities");
            register(FinancialStatementType.CASH_FLOW, standard, "50", "NET_CHANGE_IN_CASH", "Lưu chuyển tiền thuần trong kỳ", "thuần trong kỳ", "net change in cash");
            register(FinancialStatementType.CASH_FLOW, standard, "60", "BEGINNING_CASH", "Tiền và tương đương tiền đầu kỳ", "tiền đầu kỳ", "đầu kỳ");
            register(FinancialStatementType.CASH_FLOW, standard, "61", "FX_EFFECT_ON_CASH", "Ảnh hưởng của thay đổi tỷ giá", "tỷ giá");
            register(FinancialStatementType.CASH_FLOW, standard, "70", "ENDING_CASH", "Tiền và các khoản tương đương tiền cuối kỳ", "tiền cuối kỳ", "cuối kỳ", "ending cash");
        }
    }

    /**
     * Maps the raw OCR response into mapped FinancialStatementItem and draft entities.
     */
    public List<FinancialStatementItem> mapOcrResponse(FinancialStatement statement, String rawOcrJson) {
        List<FinancialStatementItem> resultItems = new ArrayList<>();
        if (rawOcrJson == null || rawOcrJson.isBlank()) {
            return resultItems;
        }

        try {
            JsonNode root = objectMapper.readTree(rawOcrJson);

            // Extract accounting standard
            String accountingStandard = extractAccountingStandard(root);
            if (accountingStandard == null || accountingStandard.isBlank()) {
                accountingStandard = "TT200"; // Default standard
            }
            statement.setAccountingStandard(accountingStandard);

            // Extract document metadata if available
            extractDocumentMetadata(statement, root);

            // Extract statements
            JsonNode statementsNode = root.path("statements");
            if (statementsNode.isMissingNode() || statementsNode.isNull()) {
                // Check if directly at root
                statementsNode = root;
            }

            processStatementNode(statement, statementsNode, new String[]{"balance_sheet", "balanceSheet"}, FinancialStatementType.BALANCE_SHEET, accountingStandard, resultItems);
            processStatementNode(statement, statementsNode, new String[]{"income_statement", "incomeStatement"}, FinancialStatementType.INCOME_STATEMENT, accountingStandard, resultItems);
            processStatementNode(statement, statementsNode, new String[]{"cash_flow_statement", "cash_flow", "cashFlowStatement", "cashFlow"}, FinancialStatementType.CASH_FLOW, accountingStandard, resultItems);

        } catch (Exception e) {
            log.error("Failed to parse and map OCR response for statement ID {}: {}", statement.getId(), e.getMessage(), e);
        }

        return resultItems;
    }

    public String extractAccountingStandard(JsonNode root) {
        JsonNode stdNode = root.path("accounting_standard");
        if (stdNode.isObject() && stdNode.has("code")) {
            String code = stdNode.get("code").asText();
            if (code != null && !code.isBlank() && !"UNKNOWN".equalsIgnoreCase(code)) {
                return code;
            }
        } else if (stdNode.isTextual()) {
            String code = stdNode.asText();
            if (code != null && !code.isBlank() && !"UNKNOWN".equalsIgnoreCase(code)) {
                return code;
            }
        }
        return "TT200";
    }

    private void extractDocumentMetadata(FinancialStatement statement, JsonNode root) {
        if (root.has("request_id")) {
            statement.setOcrRequestId(root.get("request_id").asText());
        }

        JsonNode companyNode = root.path("company");
        if (!companyNode.isMissingNode()) {
            if (companyNode.has("fiscal_year") && !companyNode.get("fiscal_year").isNull()) {
                statement.setFiscalYear(companyNode.get("fiscal_year").asInt());
            }
            if (companyNode.has("period") && !companyNode.get("period").isNull()) {
                statement.setPeriod(companyNode.get("period").asText());
            }
            if (companyNode.has("unit") && !companyNode.get("unit").isNull()) {
                statement.setUnit(companyNode.get("unit").asText());
            }
            if (companyNode.has("as_of_date") && !companyNode.get("as_of_date").isNull()) {
                try {
                    statement.setAsOfDate(java.time.LocalDate.parse(companyNode.get("as_of_date").asText().trim()));
                } catch (Exception ignored) {}
            }
        }

        JsonNode doc = root.path("document");
        if (!doc.isMissingNode()) {
            if (statement.getFiscalYear() == null && doc.has("fiscal_year") && !doc.get("fiscal_year").isNull()) {
                statement.setFiscalYear(doc.get("fiscal_year").asInt());
            }
            if (statement.getPeriod() == null && doc.has("period") && !doc.get("period").isNull()) {
                statement.setPeriod(doc.get("period").asText());
            }
            if (statement.getUnit() == null && doc.has("unit") && !doc.get("unit").isNull()) {
                statement.setUnit(doc.get("unit").asText());
            }
        }
    }

    private void processStatementNode(
            FinancialStatement statement,
            JsonNode statementsNode,
            String[] candidateKeys,
            FinancialStatementType statementType,
            String accountingStandard,
            List<FinancialStatementItem> resultItems) {

        JsonNode container = null;
        for (String key : candidateKeys) {
            JsonNode node = statementsNode.path(key);
            if (!node.isMissingNode() && !node.isNull()) {
                container = node;
                break;
            }
        }

        if (container == null) return;

        if (container.isArray()) {
            for (JsonNode itemNode : container) {
                FinancialStatementItem item = mapSingleItem(statement, itemNode, statementType, accountingStandard);
                if (item != null) {
                    resultItems.add(item);
                }
            }
        } else if (container.isObject()) {
            // Extract actual period dates/labels from OCR (e.g. "31/03/2026", "01/01/2026")
            JsonNode cols = container.path("columns");
            if (cols.isObject()) {
                if ((statement.getCurrentPeriodLabel() == null || statement.getCurrentPeriodLabel().isBlank())
                        && cols.has("current") && !cols.get("current").isNull()) {
                    statement.setCurrentPeriodLabel(cols.get("current").asText().trim());
                }
                if ((statement.getPreviousPeriodLabel() == null || statement.getPreviousPeriodLabel().isBlank())
                        && cols.has("previous") && !cols.get("previous").isNull()) {
                    statement.setPreviousPeriodLabel(cols.get("previous").asText().trim());
                }
            }

            // 1. Process mapped items array
            JsonNode itemsArray = container.path("items");
            if (itemsArray.isArray()) {
                for (JsonNode itemNode : itemsArray) {
                    FinancialStatementItem item = mapSingleItem(statement, itemNode, statementType, accountingStandard);
                    if (item != null) {
                        resultItems.add(item);
                    }
                }
            }

            // 2. Process unmapped items array so users can review and map them in Step 3
            JsonNode unmappedArray = container.path("unmapped_items");
            if (unmappedArray.isArray()) {
                for (JsonNode itemNode : unmappedArray) {
                    FinancialStatementItem item = mapSingleItem(statement, itemNode, statementType, accountingStandard);
                    if (item != null) {
                        resultItems.add(item);
                    }
                }
            }
        }
    }

    public FinancialStatementItem mapSingleItem(
            FinancialStatement statement,
            JsonNode itemNode,
            FinancialStatementType statementType,
            String accountingStandard) {

        String code = extractString(itemNode, "code");
        String rawLabel = extractString(itemNode, "raw_label", "rawLabel", "label");
        String explicitKey = extractString(itemNode, "key");
        String explicitLabel = extractString(itemNode, "label");
        String explicitStatus = extractString(itemNode, "mapping_status", "mappingStatus");
        boolean needsReviewFlag = itemNode.has("needs_review") && itemNode.get("needs_review").asBoolean(false);

        if (code == null && rawLabel == null && explicitKey == null && explicitLabel == null) {
            return null;
        }

        BigDecimal currentValue = extractBigDecimal(itemNode, "current_value", "currentValue", "current");
        BigDecimal previousValue = extractBigDecimal(itemNode, "previous_value", "previousValue", "previous");
        String unit = extractString(itemNode, "unit");
        Integer sourcePage = itemNode.has("source_page") ? itemNode.get("source_page").asInt() : null;
        BigDecimal confidence = extractBigDecimal(itemNode, "confidence");

        StandardIndicatorDef def = null;
        if (code != null && !code.isBlank()) {
            String lookupKey = buildLookupKey(accountingStandard, statementType, code);
            def = standardDefinitions.get(lookupKey);
        }
        if (def == null && explicitKey != null && !explicitKey.isBlank()) {
            def = standardDefinitionsByKey.get(statementType.name() + ":" + explicitKey);
        }

        String indicatorKey = explicitKey;
        String standardLabel = explicitLabel;
        MappingStatus mappingStatus;

        if (def != null) {
            if (indicatorKey == null || indicatorKey.isBlank()) {
                indicatorKey = def.key();
            }
            if (standardLabel == null || standardLabel.isBlank()) {
                standardLabel = def.label();
            }
            if (code == null || code.isBlank()) {
                code = def.code();
            }

            if (needsReviewFlag) {
                mappingStatus = MappingStatus.NEEDS_REVIEW;
            } else if (explicitStatus != null) {
                mappingStatus = parseMappingStatus(explicitStatus, MappingStatus.MATCHED);
            } else if (isLabelMatching(rawLabel, def.label(), def.keywords())) {
                mappingStatus = MappingStatus.MATCHED;
            } else {
                mappingStatus = MappingStatus.NEEDS_REVIEW;
            }
        } else {
            if (explicitStatus != null) {
                mappingStatus = parseMappingStatus(explicitStatus, MappingStatus.UNMAPPED);
            } else if (needsReviewFlag) {
                mappingStatus = MappingStatus.NEEDS_REVIEW;
            } else {
                mappingStatus = MappingStatus.UNMAPPED;
            }
        }

        String finalLabel = standardLabel != null ? standardLabel : (rawLabel != null ? rawLabel : "Khoản mục chưa đặt tên");
        String safeLabel = finalLabel.length() > 250 ? finalLabel.substring(0, 250) : finalLabel;
        String safeRawLabel = rawLabel != null && rawLabel.length() > 250 ? rawLabel.substring(0, 250) : rawLabel;

        FinancialStatementItem item = FinancialStatementItem.builder()
                .financialStatement(statement)
                .statementType(statementType)
                .accountingStandard(accountingStandard)
                .code(code != null ? code : "")
                .key(indicatorKey)
                .label(safeLabel)
                .rawLabel(safeRawLabel)
                .currentValue(currentValue)
                .previousValue(previousValue)
                .unit(unit != null ? unit : "VND")
                .sourcePage(sourcePage)
                .confidence(confidence)
                .mappingStatus(mappingStatus)
                .build();

        FinancialStatementItemDraft draft = FinancialStatementItemDraft.builder()
                .financialStatementItem(item)
                .currentValue(currentValue)
                .previousValue(previousValue)
                .edited(false)
                .build();

        item.setDraft(draft);
        return item;
    }

    private MappingStatus parseMappingStatus(String statusStr, MappingStatus defaultStatus) {
        if (statusStr == null) return defaultStatus;
        try {
            return MappingStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultStatus;
        }
    }

    private boolean isLabelMatching(String rawLabel, String standardLabel, List<String> keywords) {
        if (rawLabel == null || standardLabel == null) return false;

        String normRaw = normalize(rawLabel);
        String normStd = normalize(standardLabel);

        if (normRaw.contains(normStd) || normStd.contains(normRaw)) {
            return true;
        }

        for (String keyword : keywords) {
            String normKey = normalize(keyword);
            if (normRaw.contains(normKey)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replaceAll("[^a-z0-9]", " ");
    }

    private String extractString(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                return node.get(key).asText().trim();
            }
        }
        return null;
    }

    private BigDecimal extractBigDecimal(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                JsonNode valNode = node.get(key);
                if (valNode.isNumber()) {
                    return BigDecimal.valueOf(valNode.asDouble());
                } else if (valNode.isTextual()) {
                    String text = valNode.asText().trim();
                    if (text.isEmpty()) return null;
                    // Handle accounting negative format "(123.45)"
                    if (text.startsWith("(") && text.endsWith(")")) {
                        text = "-" + text.substring(1, text.length() - 1);
                    }
                    try {
                        return new BigDecimal(text.replace(",", ""));
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}

