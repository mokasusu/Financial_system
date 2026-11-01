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
        standardDefinitions.put(lookupKey, new StandardIndicatorDef(
                type, standard, code, key, label, Arrays.asList(keywords)
        ));
    }

    private String buildLookupKey(String standard, FinancialStatementType type, String code) {
        String std = standard != null ? standard.trim().toUpperCase() : "TT200";
        return std + ":" + type.name() + ":" + (code != null ? code.trim() : "");
    }

    private void initializeStandardDefinitions() {
        // Register for TT200 and TT133 (standard Vietnamese circulars)
        for (String standard : List.of("TT200", "TT133")) {
            // 9.1 Balance Sheet (13 indicators)
            register(FinancialStatementType.BALANCE_SHEET, standard, "100", "CURRENT_ASSETS", "Tài sản ngắn hạn", "ngắn hạn", "current asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "110", "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền", "tiền", "tương đương tiền", "cash");
            register(FinancialStatementType.BALANCE_SHEET, standard, "120", "SHORT_TERM_INVESTMENTS", "Các khoản đầu tư tài chính ngắn hạn", "đầu tư tài chính", "ngắn hạn", "investment");
            register(FinancialStatementType.BALANCE_SHEET, standard, "130", "SHORT_TERM_RECEIVABLES", "Các khoản phải thu ngắn hạn", "phải thu", "ngắn hạn", "receivable");
            register(FinancialStatementType.BALANCE_SHEET, standard, "140", "INVENTORIES", "Hàng tồn kho", "tồn kho", "inventory", "inventories");
            register(FinancialStatementType.BALANCE_SHEET, standard, "200", "NON_CURRENT_ASSETS", "Tài sản dài hạn", "dài hạn", "non-current asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "220", "FIXED_ASSETS", "Tài sản cố định", "cố định", "fixed asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "280", "TOTAL_ASSETS", "Tổng tài sản", "tổng tài sản", "tổng cộng tài sản", "total asset");
            register(FinancialStatementType.BALANCE_SHEET, standard, "300", "TOTAL_LIABILITIES", "Nợ phải trả", "nợ phải trả", "nợ", "liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "310", "CURRENT_LIABILITIES", "Nợ ngắn hạn", "ngắn hạn", "current liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "321", "SHORT_TERM_BORROWINGS", "Vay ngắn hạn", "vay", "thuê tài chính", "borrowing");
            register(FinancialStatementType.BALANCE_SHEET, standard, "330", "NON_CURRENT_LIABILITIES", "Nợ dài hạn", "dài hạn", "non-current liabilities");
            register(FinancialStatementType.BALANCE_SHEET, standard, "400", "EQUITY", "Vốn chủ sở hữu", "vốn chủ", "equity");

            // 9.2 Income Statement (7 indicators)
            register(FinancialStatementType.INCOME_STATEMENT, standard, "10", "NET_REVENUE", "Doanh thu thuần", "doanh thu thuần", "bán hàng", "net revenue");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "11", "COST_OF_GOODS_SOLD", "Giá vốn hàng bán", "giá vốn", "cost of goods");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "20", "GROSS_PROFIT", "Lợi nhuận gộp", "lợi nhuận gộp", "gross profit");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "23", "FINANCIAL_EXPENSES", "Chi phí tài chính", "chi phí tài chính", "financial expense");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "30", "OPERATING_PROFIT", "Lợi nhuận thuần từ hoạt động kinh doanh", "thuần từ hđkd", "hoạt động kinh doanh", "operating profit");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "50", "PROFIT_BEFORE_TAX", "Lợi nhuận kế toán trước thuế", "trước thuế", "profit before tax");
            register(FinancialStatementType.INCOME_STATEMENT, standard, "60", "NET_PROFIT", "Lợi nhuận sau thuế", "sau thuế", "net profit", "lợi nhuận sau thuế tndn");

            // 9.3 Cash Flow Statement (5 indicators)
            register(FinancialStatementType.CASH_FLOW, standard, "20", "NET_CASH_FROM_OPERATING", "Lưu chuyển tiền thuần từ hoạt động kinh doanh", "hoạt động kinh doanh", "operating activities");
            register(FinancialStatementType.CASH_FLOW, standard, "30", "NET_CASH_FROM_INVESTING", "Lưu chuyển tiền thuần từ hoạt động đầu tư", "hoạt động đầu tư", "investing activities");
            register(FinancialStatementType.CASH_FLOW, standard, "40", "NET_CASH_FROM_FINANCING", "Lưu chuyển tiền thuần từ hoạt động tài chính", "hoạt động tài chính", "financing activities");
            register(FinancialStatementType.CASH_FLOW, standard, "50", "NET_CHANGE_IN_CASH", "Lưu chuyển tiền thuần trong kỳ", "thuần trong kỳ", "net change in cash");
            register(FinancialStatementType.CASH_FLOW, standard, "70", "ENDING_CASH", "Tiền và các khoản tương đương tiền cuối kỳ", "tiền cuối kỳ", "cuối kỳ", "ending cash");
        }

        // TT99 (older accounting standard) - register matching codes
        register(FinancialStatementType.BALANCE_SHEET, "TT99", "100", "CURRENT_ASSETS", "Tài sản ngắn hạn", "ngắn hạn");
        register(FinancialStatementType.BALANCE_SHEET, "TT99", "110", "CASH_AND_EQUIVALENTS", "Tiền và các khoản tương đương tiền", "tiền");
        register(FinancialStatementType.BALANCE_SHEET, "TT99", "140", "INVENTORIES", "Hàng tồn kho", "tồn kho");
        register(FinancialStatementType.BALANCE_SHEET, "TT99", "280", "TOTAL_ASSETS", "Tổng tài sản", "tổng tài sản");
        register(FinancialStatementType.BALANCE_SHEET, "TT99", "400", "EQUITY", "Vốn chủ sở hữu", "vốn chủ");
        register(FinancialStatementType.INCOME_STATEMENT, "TT99", "10", "NET_REVENUE", "Doanh thu thuần", "doanh thu");
        register(FinancialStatementType.INCOME_STATEMENT, "TT99", "60", "NET_PROFIT", "Lợi nhuận sau thuế", "sau thuế");
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

            processStatementNode(statement, statementsNode, "balance_sheet", "balanceSheet", FinancialStatementType.BALANCE_SHEET, accountingStandard, resultItems);
            processStatementNode(statement, statementsNode, "income_statement", "incomeStatement", FinancialStatementType.INCOME_STATEMENT, accountingStandard, resultItems);
            processStatementNode(statement, statementsNode, "cash_flow", "cashFlow", FinancialStatementType.CASH_FLOW, accountingStandard, resultItems);

        } catch (Exception e) {
            log.error("Failed to parse and map OCR response for statement ID {}: {}", statement.getId(), e.getMessage(), e);
        }

        return resultItems;
    }

    public String extractAccountingStandard(JsonNode root) {
        JsonNode stdNode = root.path("accounting_standard");
        if (stdNode.isObject() && stdNode.has("code")) {
            return stdNode.get("code").asText();
        } else if (stdNode.isTextual()) {
            return stdNode.asText();
        }
        return "TT200";
    }

    private void extractDocumentMetadata(FinancialStatement statement, JsonNode root) {
        if (root.has("request_id")) {
            statement.setOcrRequestId(root.get("request_id").asText());
        }

        JsonNode doc = root.path("document");
        if (!doc.isMissingNode()) {
            if (doc.has("fiscal_year") && !doc.get("fiscal_year").isNull()) {
                statement.setFiscalYear(doc.get("fiscal_year").asInt());
            }
            if (doc.has("period") && !doc.get("period").isNull()) {
                statement.setPeriod(doc.get("period").asText());
            }
            if (doc.has("unit") && !doc.get("unit").isNull()) {
                statement.setUnit(doc.get("unit").asText());
            }
        }
    }

    private void processStatementNode(
            FinancialStatement statement,
            JsonNode statementsNode,
            String primaryKey,
            String camelKey,
            FinancialStatementType statementType,
            String accountingStandard,
            List<FinancialStatementItem> resultItems) {

        JsonNode itemsArray = statementsNode.path(primaryKey);
        if (itemsArray.isMissingNode() || itemsArray.isNull()) {
            itemsArray = statementsNode.path(camelKey);
        }

        if (itemsArray.isArray()) {
            for (JsonNode itemNode : itemsArray) {
                FinancialStatementItem item = mapSingleItem(statement, itemNode, statementType, accountingStandard);
                if (item != null) {
                    resultItems.add(item);
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
        if (code == null && rawLabel == null) {
            return null;
        }

        BigDecimal currentValue = extractBigDecimal(itemNode, "current_value", "currentValue", "current");
        BigDecimal previousValue = extractBigDecimal(itemNode, "previous_value", "previousValue", "previous");
        String unit = extractString(itemNode, "unit");
        Integer sourcePage = itemNode.has("source_page") ? itemNode.get("source_page").asInt() : null;
        BigDecimal confidence = extractBigDecimal(itemNode, "confidence");

        String lookupKey = buildLookupKey(accountingStandard, statementType, code);
        StandardIndicatorDef def = standardDefinitions.get(lookupKey);

        String indicatorKey = null;
        String standardLabel = null;
        MappingStatus mappingStatus;

        if (def != null) {
            indicatorKey = def.key();
            standardLabel = def.label();

            if (isLabelMatching(rawLabel, def.label(), def.keywords())) {
                mappingStatus = MappingStatus.MATCHED;
            } else {
                mappingStatus = MappingStatus.NEEDS_REVIEW;
            }
        } else {
            // Check if unknown standard
            if (!"TT200".equalsIgnoreCase(accountingStandard) && !"TT133".equalsIgnoreCase(accountingStandard)) {
                mappingStatus = MappingStatus.NEEDS_REVIEW;
            } else {
                mappingStatus = MappingStatus.UNMAPPED;
            }
        }

        FinancialStatementItem item = FinancialStatementItem.builder()
                .financialStatement(statement)
                .statementType(statementType)
                .accountingStandard(accountingStandard)
                .code(code != null ? code : "")
                .key(indicatorKey)
                .label(standardLabel != null ? standardLabel : rawLabel)
                .rawLabel(rawLabel)
                .currentValue(currentValue)
                .previousValue(previousValue)
                .unit(unit != null ? unit : "VND")
                .sourcePage(sourcePage)
                .confidence(confidence)
                .mappingStatus(mappingStatus)
                .build();

        // Create editable draft with original values initially
        FinancialStatementItemDraft draft = FinancialStatementItemDraft.builder()
                .financialStatementItem(item)
                .currentValue(currentValue)
                .previousValue(previousValue)
                .edited(false)
                .build();

        item.setDraft(draft);

        return item;
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

