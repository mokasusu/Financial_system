package com.financial.modules.financialstatement.mapper;

import com.financial.modules.financialstatement.dto.FinancialStatementItemResponse;
import com.financial.modules.financialstatement.dto.FinancialStatementResponse;
import com.financial.modules.financialstatement.dto.FinancialStatementStatusResponse;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class FinancialStatementMapper {

    public FinancialStatementResponse toResponse(FinancialStatement statement) {
        if (statement == null) return null;

        List<FinancialStatementItemResponse> itemResponses = statement.getItems() != null
                ? statement.getItems().stream().map(this::toItemResponse).toList()
                : Collections.emptyList();

        return FinancialStatementResponse.builder()
                .id(statement.getId())
                .companyId(statement.getCompany() != null ? statement.getCompany().getId() : null)
                .companyName(statement.getCompany() != null ? statement.getCompany().getCompanyName() : null)
                .fileName(statement.getFileName())
                .fileSize(statement.getFileSize())
                .contentType(statement.getContentType())
                .accountingStandard(statement.getAccountingStandard())
                .fiscalYear(statement.getFiscalYear())
                .period(statement.getPeriod())
                .asOfDate(statement.getAsOfDate())
                .currentPeriodLabel(statement.getCurrentPeriodLabel())
                .previousPeriodLabel(statement.getPreviousPeriodLabel())
                .fileUrl(statement.getId() != null ? "/api/financial-statements/" + statement.getId() + "/file" : null)
                .unit(statement.getUnit())
                .processingStatus(statement.getProcessingStatus())
                .statusDisplayName(statement.getProcessingStatus() != null ? statement.getProcessingStatus().getDisplayName() : null)
                .ocrRequestId(statement.getOcrRequestId())
                .errorCode(statement.getErrorCode())
                .errorMessage(statement.getErrorMessage())
                .uploadedAt(statement.getUploadedAt())
                .processingStartedAt(statement.getProcessingStartedAt())
                .processingCompletedAt(statement.getProcessingCompletedAt())
                .createdAt(statement.getCreatedAt())
                .updatedAt(statement.getUpdatedAt())
                .isDraft(statement.getIsDraft())
                .items(itemResponses)
                .build();
    }

    public FinancialStatementStatusResponse toStatusResponse(FinancialStatement statement) {
        if (statement == null) return null;

        return FinancialStatementStatusResponse.builder()
                .id(statement.getId())
                .status(statement.getProcessingStatus())
                .statusDisplayName(statement.getProcessingStatus() != null ? statement.getProcessingStatus().getDisplayName() : null)
                .uploadedAt(statement.getUploadedAt())
                .processingStartedAt(statement.getProcessingStartedAt())
                .processingCompletedAt(statement.getProcessingCompletedAt())
                .errorCode(statement.getErrorCode())
                .errorMessage(statement.getErrorMessage())
                .build();
    }

    public FinancialStatementItemResponse toItemResponse(FinancialStatementItem item) {
        if (item == null) return null;

        FinancialStatementItemDraft draft = item.getDraft();

        BigDecimal activeCurrent = (draft != null && draft.getCurrentValue() != null)
                ? draft.getCurrentValue()
                : item.getCurrentValue();

        BigDecimal activePrevious = (draft != null && draft.getPreviousValue() != null)
                ? draft.getPreviousValue()
                : item.getPreviousValue();

        Boolean edited = draft != null ? draft.getEdited() : false;

        return FinancialStatementItemResponse.builder()
                .id(item.getId())
                .statementType(item.getStatementType())
                .statementTypeDisplayName(item.getStatementType() != null ? item.getStatementType().getDisplayName() : null)
                .accountingStandard(item.getAccountingStandard())
                .code(item.getCode())
                .key(item.getKey())
                .label(item.getLabel())
                .rawLabel(item.getRawLabel())
                .current(activeCurrent)
                .previous(activePrevious)
                .originalCurrentValue(item.getCurrentValue())
                .originalPreviousValue(item.getPreviousValue())
                .unit(item.getUnit())
                .sourcePage(item.getSourcePage())
                .confidence(item.getConfidence())
                .mappingStatus(item.getMappingStatus())
                .mappingStatusDisplayName(item.getMappingStatus() != null ? item.getMappingStatus().getDisplayName() : null)
                .edited(edited)
                .editedAt(draft != null ? draft.getEditedAt() : null)
                .build();
    }
}

