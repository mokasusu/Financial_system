package com.financial.modules.financialstatement.service;

import com.financial.common.config.AsyncConfig;
import com.financial.common.exception.OcrApiException;
import com.financial.common.exception.OcrTimeoutException;
import com.financial.modules.financialstatement.client.OcrApiClient;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OcrIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(OcrIntegrationService.class);

    private final FinancialStatementRepository statementRepository;
    private final FinancialStatementItemRepository itemRepository;
    private final OcrApiClient ocrApiClient;
    private final FinancialIndicatorMappingService mappingService;

    public OcrIntegrationService(
            FinancialStatementRepository statementRepository,
            FinancialStatementItemRepository itemRepository,
            OcrApiClient ocrApiClient,
            FinancialIndicatorMappingService mappingService) {
        this.statementRepository = statementRepository;
        this.itemRepository = itemRepository;
        this.ocrApiClient = ocrApiClient;
        this.mappingService = mappingService;
    }

    /**
     * Executes background OCR extraction on the dedicated ocrTaskExecutor thread pool.
     * Never blocks the HTTP client thread.
     */
    @Async(AsyncConfig.OCR_TASK_EXECUTOR)
    public void processFinancialStatementAsync(Long statementId) {
        log.info("Starting background OCR job for financial statement ID: {}", statementId);

        FinancialStatement statement = statementRepository.findById(statementId).orElse(null);
        if (statement == null) {
            log.error("Statement ID {} not found for background OCR processing", statementId);
            return;
        }

        // Check if cancellation was already requested
        if (statement.getProcessingStatus() == FinancialStatementStatus.CANCEL_REQUESTED ||
            statement.getProcessingStatus() == FinancialStatementStatus.CANCELLED) {
            log.info("Statement ID {} was cancelled before OCR processing started", statementId);
            statement.setProcessingStatus(FinancialStatementStatus.CANCELLED);
            statement.setErrorMessage("Statement processing was cancelled by user.");
            statementRepository.save(statement);
            return;
        }

        // Update status to PROCESSING
        statement.setProcessingStatus(FinancialStatementStatus.PROCESSING);
        statement.setProcessingStartedAt(LocalDateTime.now());
        statementRepository.save(statement);

        try {
            // Read stored file
            File file = new File(statement.getFilePath());
            if (!file.exists()) {
                throw new IllegalStateException("Uploaded file not found on disk at: " + statement.getFilePath());
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // Call external OCR API (enforces 600s timeout)
            String ocrResponseJson = ocrApiClient.analyzeFinancialStatement(
                    fileBytes,
                    statement.getFileName(),
                    statement.getPeriod(),
                    true,
                    true
            );

            // Re-fetch statement to check if cancelled while waiting for OCR
            statement = statementRepository.findById(statementId).orElse(null);
            if (statement == null) return;

            if (statement.getProcessingStatus() == FinancialStatementStatus.CANCEL_REQUESTED ||
                statement.getProcessingStatus() == FinancialStatementStatus.CANCELLED) {
                log.warn("Statement ID {} was cancelled while waiting for OCR response. Discarding results.", statementId);
                statement.setProcessingStatus(FinancialStatementStatus.CANCELLED);
                statement.setErrorMessage("Statement processing was cancelled by user.");
                statementRepository.save(statement);
                return;
            }

            // Persist full original OCR JSON response
            statement.setOriginalOcrResponse(ocrResponseJson);

            // Map standard indicators
            List<FinancialStatementItem> items = mappingService.mapOcrResponse(statement, ocrResponseJson);

            // Persist items & drafts
            itemRepository.saveAll(items);

            // Determine if review is needed
            boolean hasNeedsReview = items.stream()
                    .anyMatch(i -> i.getMappingStatus() == MappingStatus.NEEDS_REVIEW);

            statement.setProcessingStatus(hasNeedsReview
                    ? FinancialStatementStatus.REVIEWING_DATA
                    : FinancialStatementStatus.COMPLETED);
            statement.setProcessingCompletedAt(LocalDateTime.now());
            statement.setErrorCode(null);
            statement.setErrorMessage(null);

            statementRepository.save(statement);
            log.info("Successfully completed OCR processing for statement ID: {}", statementId);

        } catch (OcrTimeoutException ex) {
            log.error("OCR timeout encountered for statement ID {}: {}", statementId, ex.getMessage());
            updateFailureStatus(statementId, ex.getErrorCode(), ex.getMessage());
        } catch (OcrApiException ex) {
            log.error("OCR API error for statement ID {}: [{}] {}", statementId, ex.getErrorCode(), ex.getMessage());
            updateFailureStatus(statementId, ex.getErrorCode(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during OCR processing for statement ID {}: {}", statementId, ex.getMessage(), ex);
            updateFailureStatus(statementId, "OCR_ANALYSIS_FAILED", "OCR processing failed: " + ex.getMessage());
        }
    }

    @Transactional
    public void updateFailureStatus(Long statementId, String errorCode, String errorMessage) {
        FinancialStatement statement = statementRepository.findById(statementId).orElse(null);
        if (statement != null) {
            // Do not overwrite CANCELLED status
            if (statement.getProcessingStatus() != FinancialStatementStatus.CANCELLED) {
                statement.setProcessingStatus(FinancialStatementStatus.FAILED);
                statement.setErrorCode(errorCode);
                statement.setErrorMessage(errorMessage);
                statement.setProcessingCompletedAt(LocalDateTime.now());
                statementRepository.save(statement);
            }
        }
    }
}

