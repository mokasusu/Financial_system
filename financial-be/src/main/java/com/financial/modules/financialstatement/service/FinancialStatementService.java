package com.financial.modules.financialstatement.service;

import com.financial.common.response.PageResponse;
import com.financial.modules.financialstatement.dto.*;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FinancialStatementService {

    FinancialStatementUploadResponse uploadFinancialStatement(
            Long userId,
            MultipartFile file,
            String period,
            Boolean enableAi,
            Boolean storeImages);

    FinancialStatementStatusResponse getFinancialStatementStatus(Long userId, Long statementId);

    FinancialStatementResponse getFinancialStatement(Long userId, Long statementId);

    List<FinancialStatementItemResponse> getFinancialStatementItems(Long userId, Long statementId);

    List<FinancialStatementItemResponse> updateFinancialStatementItems(
            Long userId,
            Long statementId,
            FinancialStatementItemUpdateRequest request);

    List<FinancialStatementItemResponse> resetFinancialStatementItems(Long userId, Long statementId);

    FinancialStatementResponse confirmFinancialData(Long userId, Long statementId);

    FinancialStatementStatusResponse cancelProcessing(Long userId, Long statementId);

    PageResponse<FinancialStatementResponse> getFinancialStatements(
            Long userId,
            Integer fiscalYear,
            String period,
            FinancialStatementStatus status,
            Pageable pageable);
}

