package com.financial.modules.financialstatement.service.impl;

import com.financial.common.config.OcrProperties;
import com.financial.common.exception.*;
import com.financial.common.response.PageResponse;
import com.financial.modules.aianalysis.entity.AIAnalysis;
import com.financial.modules.aianalysis.repository.AIAnalysisRepository;
import com.financial.modules.assessment.entity.Assessment;
import com.financial.modules.assessment.repository.AssessmentRepository;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.calculationengine.repository.FinancialCalculationRepository;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.dto.*;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.mapper.FinancialStatementMapper;
import com.financial.modules.financialstatement.repository.FinancialStatementItemDraftRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.financialstatement.service.FinancialStatementService;
import com.financial.modules.financialstatement.service.OcrIntegrationService;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.repository.RiskAnalysisRepository;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.repository.UserRepository;
import com.financial.modules.validation.entity.ValidationExecution;
import com.financial.modules.validation.repository.ValidationExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FinancialStatementServiceImpl implements FinancialStatementService {

    private static final Logger log = LoggerFactory.getLogger(FinancialStatementServiceImpl.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpg", "jpeg", "tiff", "tif", "bmp", "webp", "txt"
    );

    private final FinancialStatementRepository statementRepository;
    private final FinancialStatementItemRepository itemRepository;
    private final FinancialStatementItemDraftRepository draftRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final OcrIntegrationService ocrIntegrationService;
    private final FinancialStatementMapper statementMapper;
    private final OcrProperties ocrProperties;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final AssessmentRepository assessmentRepository;
    private final RiskAnalysisRepository riskAnalysisRepository;
    private final FinancialCalculationRepository calculationRepository;
    private final ValidationExecutionRepository validationExecutionRepository;

    public FinancialStatementServiceImpl(
            FinancialStatementRepository statementRepository,
            FinancialStatementItemRepository itemRepository,
            FinancialStatementItemDraftRepository draftRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            OcrIntegrationService ocrIntegrationService,
            FinancialStatementMapper statementMapper,
            OcrProperties ocrProperties,
            AIAnalysisRepository aiAnalysisRepository,
            AssessmentRepository assessmentRepository,
            RiskAnalysisRepository riskAnalysisRepository,
            FinancialCalculationRepository calculationRepository,
            ValidationExecutionRepository validationExecutionRepository) {
        this.statementRepository = statementRepository;
        this.itemRepository = itemRepository;
        this.draftRepository = draftRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.ocrIntegrationService = ocrIntegrationService;
        this.statementMapper = statementMapper;
        this.ocrProperties = ocrProperties;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.assessmentRepository = assessmentRepository;
        this.riskAnalysisRepository = riskAnalysisRepository;
        this.calculationRepository = calculationRepository;
        this.validationExecutionRepository = validationExecutionRepository;
    }

    @Override
    @Transactional
    public FinancialStatementUploadResponse uploadFinancialStatement(
            Long userId,
            MultipartFile file,
            String period,
            Boolean enableAi,
            Boolean storeImages) {

        // 1. Verify User and Company ownership
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("No active company found for user ID: " + userId + ". Please create a company profile first."));

        // 2. Validate File
        validateFile(file);

        // 3. Sanitize and store file
        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        String storagePath = storeFileLocally(company.getId(), file, sanitizedFilename);

        // 4. Create initial statement record in QUEUED status
        FinancialStatement statement = FinancialStatement.builder()
                .company(company)
                .uploadedBy(user)
                .fileName(sanitizedFilename)
                .filePath(storagePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .period(period != null && !period.isBlank() ? period : "current")
                .processingStatus(FinancialStatementStatus.QUEUED)
                .uploadedAt(LocalDateTime.now())
                .build();

        FinancialStatement savedStatement = statementRepository.save(statement);
        log.info("Financial statement record {} created in QUEUED status for company {}", savedStatement.getId(), company.getId());

        // 5. Trigger asynchronous background OCR processing
        ocrIntegrationService.processFinancialStatementAsync(savedStatement.getId());

        // 6. Return immediate 202 Accepted response
        return FinancialStatementUploadResponse.builder()
                .id(savedStatement.getId())
                .status(FinancialStatementStatus.QUEUED)
                .message("Financial statement has been received and queued for processing.")
                .build();
    }

    @Override
    @Transactional
    public FinancialStatementStatusResponse triggerExtraction(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        statement.setProcessingStatus(FinancialStatementStatus.QUEUED);
        statement.setErrorMessage(null);
        statement.setErrorCode(null);
        FinancialStatement saved = statementRepository.save(statement);

        log.info("Triggering OCR extraction for statement ID: {}", statementId);
        ocrIntegrationService.processFinancialStatementAsync(saved.getId());

        return statementMapper.toStatusResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialStatementStatusResponse getFinancialStatementStatus(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);
        return statementMapper.toStatusResponse(statement);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialStatementResponse getFinancialStatement(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);
        return statementMapper.toResponse(statement);
    }

    @Override
    @Transactional(readOnly = true)
    public StatementFileDownload getStatementFile(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);
        if (statement.getFilePath() == null || statement.getFilePath().isBlank()) {
            throw new StatementNotFoundException("Document file path not recorded for statement ID: " + statementId);
        }

        File file = new File(statement.getFilePath());
        if (!file.exists()) {
            throw new StatementNotFoundException("Document file not found on disk at: " + statement.getFilePath());
        }

        Resource resource = new FileSystemResource(file);
        String contentType = statement.getContentType() != null && !statement.getContentType().isBlank()
                ? statement.getContentType()
                : "application/pdf";

        return new StatementFileDownload(resource, statement.getFileName(), contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancialStatementItemResponse> getFinancialStatementItems(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);
        List<FinancialStatementItem> items = itemRepository.findByFinancialStatementId(statement.getId());
        return items.stream().map(statementMapper::toItemResponse).toList();
    }

    @Override
    @Transactional
    public List<FinancialStatementItemResponse> updateFinancialStatementItems(
            Long userId,
            Long statementId,
            FinancialStatementItemUpdateRequest request) {

        FinancialStatement statement = getOwnedStatement(userId, statementId);

        if (statement.getProcessingStatus() == FinancialStatementStatus.DATA_CONFIRMED) {
            throw new InvalidStatementStatusException("Financial data has already been confirmed and cannot be edited.");
        }

        List<FinancialStatementItem> statementItems = itemRepository.findByFinancialStatementId(statement.getId());
        Map<Long, FinancialStatementItem> itemMap = new HashMap<>();
        for (FinancialStatementItem item : statementItems) {
            itemMap.put(item.getId(), item);
        }

        List<FinancialStatementItemDraft> draftsToSave = new ArrayList<>();

        for (FinancialStatementItemUpdateRequest.ItemUpdate update : request.getItems()) {
            FinancialStatementItem item = itemMap.get(update.getId());
            if (item == null) {
                throw new StatementNotFoundException("Indicator item ID " + update.getId() + " does not belong to financial statement ID: " + statementId);
            }

            FinancialStatementItemDraft draft = item.getDraft();
            if (draft == null) {
                draft = FinancialStatementItemDraft.builder()
                        .financialStatementItem(item)
                        .currentValue(update.getCurrentValue())
                        .previousValue(update.getPreviousValue())
                        .edited(true)
                        .editedAt(LocalDateTime.now())
                        .build();
                item.setDraft(draft);
            } else {
                draft.setCurrentValue(update.getCurrentValue());
                draft.setPreviousValue(update.getPreviousValue());
                draft.setEdited(true);
                draft.setEditedAt(LocalDateTime.now());
            }

            draftsToSave.add(draft);
        }

        draftRepository.saveAll(draftsToSave);
        log.info("Updated {} indicator drafts for statement ID {}", draftsToSave.size(), statementId);

        // Re-read items with drafts
        return itemRepository.findByFinancialStatementId(statementId).stream()
                .map(statementMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<FinancialStatementItemResponse> resetFinancialStatementItems(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        if (statement.getProcessingStatus() == FinancialStatementStatus.DATA_CONFIRMED) {
            throw new InvalidStatementStatusException("Financial data has already been confirmed and cannot be reset.");
        }

        List<FinancialStatementItem> items = itemRepository.findByFinancialStatementId(statement.getId());
        List<FinancialStatementItemDraft> draftsToSave = new ArrayList<>();

        for (FinancialStatementItem item : items) {
            FinancialStatementItemDraft draft = item.getDraft();
            if (draft != null) {
                draft.setCurrentValue(item.getCurrentValue());
                draft.setPreviousValue(item.getPreviousValue());
                draft.setEdited(false);
                draft.setEditedAt(null);
                draftsToSave.add(draft);
            }
        }

        draftRepository.saveAll(draftsToSave);
        log.info("Reset {} indicator drafts back to original OCR values for statement ID {}", draftsToSave.size(), statementId);

        return itemRepository.findByFinancialStatementId(statementId).stream()
                .map(statementMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public FinancialStatementResponse confirmFinancialData(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        if (statement.getProcessingStatus() != FinancialStatementStatus.COMPLETED &&
            statement.getProcessingStatus() != FinancialStatementStatus.REVIEWING_DATA) {
            throw new InvalidStatementStatusException("Cannot confirm statement with status: " + statement.getProcessingStatus()
                    + ". Only COMPLETED or REVIEWING_DATA statements can be confirmed.");
        }

        statement.setProcessingStatus(FinancialStatementStatus.DATA_CONFIRMED);
        FinancialStatement saved = statementRepository.save(statement);
        log.info("Financial statement ID {} confirmed successfully by user {}", statementId, userId);

        return statementMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FinancialStatementStatusResponse cancelProcessing(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        FinancialStatementStatus currentStatus = statement.getProcessingStatus();

        if (currentStatus == FinancialStatementStatus.QUEUED) {
            statement.setProcessingStatus(FinancialStatementStatus.CANCELLED);
            statement.setErrorMessage("Statement processing was cancelled by user.");
            FinancialStatement saved = statementRepository.save(statement);
            return statementMapper.toStatusResponse(saved);
        } else if (currentStatus == FinancialStatementStatus.PROCESSING) {
            statement.setProcessingStatus(FinancialStatementStatus.CANCEL_REQUESTED);
            statement.setErrorMessage("Cancellation requested. Background OCR job will not persist results.");
            FinancialStatement saved = statementRepository.save(statement);
            return statementMapper.toStatusResponse(saved);
        } else {
            throw new InvalidStatementStatusException("Cannot cancel statement in status: " + currentStatus
                    + ". Only QUEUED or PROCESSING requests can be cancelled.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FinancialStatementResponse> getFinancialStatements(
            Long userId,
            Integer fiscalYear,
            String period,
            FinancialStatementStatus status,
            Pageable pageable) {

        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("No active company found for user ID: " + userId));

        Page<FinancialStatement> page = statementRepository.findByCompanyIdWithFilters(
                company.getId(), fiscalYear, period, status, pageable
        );

        List<FinancialStatementResponse> responses = page.getContent().stream()
                .map(statementMapper::toResponse)
                .toList();

        return PageResponse.<FinancialStatementResponse>builder()
                .items(responses)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    private FinancialStatement getOwnedStatement(Long userId, Long statementId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("No active company found for user ID: " + userId));

        return statementRepository.findByIdAndCompanyId(statementId, company.getId())
                .orElseThrow(() -> new StatementNotFoundException("Financial statement not found with ID: " + statementId + " for your company."));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file cannot be null or empty");
        }

        long maxBytes = (long) ocrProperties.getMaxUploadMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(ocrProperties.getMaxUploadMb());
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileException("Filename is missing");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException("Unsupported file type: ." + extension + ". Supported types are: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "statement.pdf";
        // Remove path traversal and illegal characters
        String clean = Paths.get(filename).getFileName().toString();
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.isEmpty() ? "statement.pdf" : clean;
    }

    private String storeFileLocally(Long companyId, MultipartFile file, String sanitizedFilename) {
        String uploadDir = "uploads" + File.separator + "statements" + File.separator + companyId;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedFilename;
        Path targetPath = Paths.get(uploadDir, uniqueFileName);

        try {
            Files.write(targetPath, file.getBytes());
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded financial statement file locally: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteFinancialStatement(Long userId, Long statementId) {
        FinancialStatement statement = getOwnedStatement(userId, statementId);

        log.info("Deleting financial statement session ID {} and associated downstream entities for user ID {}", statementId, userId);

        // 1. Delete AI Analysis
        List<AIAnalysis> aiAnalyses = aiAnalysisRepository.findByFinancialStatementId(statementId);
        if (!aiAnalyses.isEmpty()) {
            aiAnalysisRepository.deleteAll(aiAnalyses);
        }

        // 2. Delete Assessments
        List<Assessment> assessments = assessmentRepository.findByFinancialStatementIdOrderByAssessmentVersionDesc(statementId);
        if (!assessments.isEmpty()) {
            assessmentRepository.deleteAll(assessments);
        }

        // 3. Delete Risk Analyses
        List<RiskAnalysis> riskAnalyses = riskAnalysisRepository.findByFinancialStatementIdOrderByAnalysisVersionDesc(statementId);
        if (!riskAnalyses.isEmpty()) {
            riskAnalysisRepository.deleteAll(riskAnalyses);
        }

        // 4. Delete Calculations
        List<FinancialCalculation> calculations = calculationRepository.findByFinancialStatementIdOrderByCalculationVersionDesc(statementId);
        if (!calculations.isEmpty()) {
            calculationRepository.deleteAll(calculations);
        }

        // 5. Delete Validation Executions
        List<ValidationExecution> validations = validationExecutionRepository.findByFinancialStatementIdOrderByExecutedAtDesc(statementId);
        if (!validations.isEmpty()) {
            validationExecutionRepository.deleteAll(validations);
        }

        // 6. Delete physical document file on disk if exists
        if (statement.getFilePath() != null && !statement.getFilePath().isBlank()) {
            try {
                File file = new File(statement.getFilePath());
                if (file.exists() && file.isFile()) {
                    boolean deleted = file.delete();
                    log.info("Deleted physical file for statement ID {}: {}", statementId, deleted);
                }
            } catch (Exception e) {
                log.warn("Failed to delete physical file on disk for statement ID {}: {}", statementId, e.getMessage());
            }
        }

        // 7. Delete Statement (cascades to items and item drafts)
        statementRepository.delete(statement);
        log.info("Financial statement session ID {} deleted successfully", statementId);
    }
}

