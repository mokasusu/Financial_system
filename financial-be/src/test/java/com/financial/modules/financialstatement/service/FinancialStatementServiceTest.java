package com.financial.modules.financialstatement.service;

import com.financial.common.config.OcrProperties;
import com.financial.common.exception.*;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.financialstatement.dto.*;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.entity.FinancialStatementItem;
import com.financial.modules.financialstatement.entity.FinancialStatementItemDraft;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.financialstatement.mapper.FinancialStatementMapper;
import com.financial.modules.financialstatement.repository.FinancialStatementItemDraftRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementItemRepository;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.financialstatement.service.impl.FinancialStatementServiceImpl;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialStatementServiceTest {

    @Mock
    private FinancialStatementRepository statementRepository;

    @Mock
    private FinancialStatementItemRepository itemRepository;

    @Mock
    private FinancialStatementItemDraftRepository draftRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OcrIntegrationService ocrIntegrationService;

    @Spy
    private FinancialStatementMapper statementMapper = new FinancialStatementMapper();

    private OcrProperties ocrProperties;
    private FinancialStatementServiceImpl statementService;

    private User sampleUser;
    private Company sampleCompany;
    private FinancialStatement sampleStatement;
    private FinancialStatementItem sampleItem;
    private FinancialStatementItemDraft sampleDraft;

    @BeforeEach
    void setUp() {
        ocrProperties = new OcrProperties();
        ocrProperties.setMaxUploadMb(25);

        statementService = new FinancialStatementServiceImpl(
                statementRepository,
                itemRepository,
                draftRepository,
                companyRepository,
                userRepository,
                ocrIntegrationService,
                statementMapper,
                ocrProperties
        );

        sampleUser = User.builder().id(1L).fullName("Test User").email("test@example.com").build();
        sampleCompany = Company.builder().id(10L).companyName("Công ty ABC").taxCode("0101234567").user(sampleUser).build();

        sampleStatement = FinancialStatement.builder()
                .id(100L)
                .company(sampleCompany)
                .uploadedBy(sampleUser)
                .fileName("bctc_2023.pdf")
                .filePath("uploads/statements/10/bctc_2023.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .processingStatus(FinancialStatementStatus.COMPLETED)
                .build();

        sampleDraft = FinancialStatementItemDraft.builder()
                .id(501L)
                .currentValue(new BigDecimal("1000000"))
                .previousValue(new BigDecimal("900000"))
                .edited(false)
                .build();

        sampleItem = FinancialStatementItem.builder()
                .id(201L)
                .financialStatement(sampleStatement)
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .code("100")
                .key("CURRENT_ASSETS")
                .label("Tài sản ngắn hạn")
                .currentValue(new BigDecimal("1000000"))
                .previousValue(new BigDecimal("900000"))
                .mappingStatus(MappingStatus.MATCHED)
                .draft(sampleDraft)
                .build();

        sampleDraft.setFinancialStatementItem(sampleItem);
    }

    @Test
    @DisplayName("Upload valid PDF statement should save QUEUED statement and return HTTP 202 response")
    void uploadFinancialStatement_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bctc_2023.pdf", "application/pdf", "dummy pdf content".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.save(any(FinancialStatement.class))).thenAnswer(invocation -> {
            FinancialStatement s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        FinancialStatementUploadResponse response = statementService.uploadFinancialStatement(
                1L, file, "current", true, true
        );

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(FinancialStatementStatus.QUEUED);

        verify(statementRepository).save(any(FinancialStatement.class));
        verify(ocrIntegrationService).processFinancialStatementAsync(100L);
    }

    @Test
    @DisplayName("Upload empty file should throw InvalidFileException")
    void uploadFinancialStatement_EmptyFile_ThrowsInvalidFileException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));

        assertThatThrownBy(() -> statementService.uploadFinancialStatement(1L, emptyFile, "current", true, true))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("Upload unsupported file extension should throw UnsupportedFileTypeException")
    void uploadFinancialStatement_UnsupportedExtension_ThrowsUnsupportedFileTypeException() {
        MockMultipartFile badFile = new MockMultipartFile(
                "file", "document.exe", "application/octet-stream", "dummy binary".getBytes()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));

        assertThatThrownBy(() -> statementService.uploadFinancialStatement(1L, badFile, "current", true, true))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    @DisplayName("Upload file exceeding 25MB should throw FileTooLargeException")
    void uploadFinancialStatement_FileTooLarge_ThrowsFileTooLargeException() {
        byte[] largeBytes = new byte[26 * 1024 * 1024]; // 26 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeBytes
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));

        assertThatThrownBy(() -> statementService.uploadFinancialStatement(1L, largeFile, "current", true, true))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    @DisplayName("Upload when user does not have a company should throw CompanyNotFoundException")
    void uploadFinancialStatement_NoCompany_ThrowsCompanyNotFoundException() {
        MockMultipartFile file = new MockMultipartFile("file", "bctc.pdf", "application/pdf", "data".getBytes());

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.uploadFinancialStatement(1L, file, "current", true, true))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    @DisplayName("Accessing statement of another company should throw StatementNotFoundException")
    void getFinancialStatement_OtherCompany_ThrowsStatementNotFoundException() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.getFinancialStatement(1L, 999L))
                .isInstanceOf(StatementNotFoundException.class);
    }

    @Test
    @DisplayName("Editing indicator drafts should update draft values and keep original values immutable")
    void updateFinancialStatementItems_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));
        when(itemRepository.findByFinancialStatementId(100L)).thenReturn(List.of(sampleItem));

        FinancialStatementItemUpdateRequest request = FinancialStatementItemUpdateRequest.builder()
                .items(List.of(
                        FinancialStatementItemUpdateRequest.ItemUpdate.builder()
                                .id(201L)
                                .currentValue(new BigDecimal("1200000"))
                                .previousValue(new BigDecimal("950000"))
                                .build()
                ))
                .build();

        List<FinancialStatementItemResponse> responses = statementService.updateFinancialStatementItems(1L, 100L, request);

        assertThat(responses).hasSize(1);
        FinancialStatementItemResponse res = responses.get(0);

        // Active values updated from draft
        assertThat(res.getCurrent()).isEqualByComparingTo(new BigDecimal("1200000"));
        assertThat(res.getPrevious()).isEqualByComparingTo(new BigDecimal("950000"));
        assertThat(res.getEdited()).isTrue();

        // Original OCR values remain unchanged
        assertThat(res.getOriginalCurrentValue()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(res.getOriginalPreviousValue()).isEqualByComparingTo(new BigDecimal("900000"));
        assertThat(sampleItem.getCurrentValue()).isEqualByComparingTo(new BigDecimal("1000000"));
    }

    @Test
    @DisplayName("Editing indicators on DATA_CONFIRMED statement should throw InvalidStatementStatusException")
    void updateFinancialStatementItems_AlreadyConfirmed_ThrowsInvalidStatementStatusException() {
        sampleStatement.setProcessingStatus(FinancialStatementStatus.DATA_CONFIRMED);

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));

        FinancialStatementItemUpdateRequest request = FinancialStatementItemUpdateRequest.builder()
                .items(List.of(new FinancialStatementItemUpdateRequest.ItemUpdate(201L, BigDecimal.ONE, BigDecimal.ZERO)))
                .build();

        assertThatThrownBy(() -> statementService.updateFinancialStatementItems(1L, 100L, request))
                .isInstanceOf(InvalidStatementStatusException.class);
    }

    @Test
    @DisplayName("Resetting indicator drafts should restore original OCR values and set edited to false")
    void resetFinancialStatementItems_Success() {
        sampleDraft.setCurrentValue(new BigDecimal("9999999"));
        sampleDraft.setEdited(true);

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));
        when(itemRepository.findByFinancialStatementId(100L)).thenReturn(List.of(sampleItem));

        List<FinancialStatementItemResponse> responses = statementService.resetFinancialStatementItems(1L, 100L);

        assertThat(responses).hasSize(1);
        FinancialStatementItemResponse res = responses.get(0);
        assertThat(res.getCurrent()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(res.getEdited()).isFalse();
        assertThat(sampleDraft.getEdited()).isFalse();
    }

    @Test
    @DisplayName("Confirming financial data changes status to DATA_CONFIRMED")
    void confirmFinancialData_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));
        when(statementRepository.save(any(FinancialStatement.class))).thenAnswer(i -> i.getArgument(0));

        FinancialStatementResponse response = statementService.confirmFinancialData(1L, 100L);

        assertThat(response.getProcessingStatus()).isEqualTo(FinancialStatementStatus.DATA_CONFIRMED);
    }

    @Test
    @DisplayName("Cancelling QUEUED statement should transition immediately to CANCELLED")
    void cancelProcessing_Queued_SetsCancelled() {
        sampleStatement.setProcessingStatus(FinancialStatementStatus.QUEUED);

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));
        when(statementRepository.save(any(FinancialStatement.class))).thenAnswer(i -> i.getArgument(0));

        FinancialStatementStatusResponse response = statementService.cancelProcessing(1L, 100L);

        assertThat(response.getStatus()).isEqualTo(FinancialStatementStatus.CANCELLED);
    }

    @Test
    @DisplayName("Cancelling PROCESSING statement should transition to CANCEL_REQUESTED")
    void cancelProcessing_Processing_SetsCancelRequested() {
        sampleStatement.setProcessingStatus(FinancialStatementStatus.PROCESSING);

        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(statementRepository.findByIdAndCompanyId(100L, 10L)).thenReturn(Optional.of(sampleStatement));
        when(statementRepository.save(any(FinancialStatement.class))).thenAnswer(i -> i.getArgument(0));

        FinancialStatementStatusResponse response = statementService.cancelProcessing(1L, 100L);

        assertThat(response.getStatus()).isEqualTo(FinancialStatementStatus.CANCEL_REQUESTED);
    }
}

