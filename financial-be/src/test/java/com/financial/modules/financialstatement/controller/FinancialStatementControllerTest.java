package com.financial.modules.financialstatement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.StatementNotFoundException;
import com.financial.common.response.PageResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.financialstatement.dto.*;
import com.financial.modules.financialstatement.enums.FinancialStatementStatus;
import com.financial.modules.financialstatement.enums.FinancialStatementType;
import com.financial.modules.financialstatement.enums.MappingStatus;
import com.financial.modules.financialstatement.service.FinancialStatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinancialStatementControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FinancialStatementService statementService;

    @InjectMocks
    private FinancialStatementController statementController;

    private UserPrincipal samplePrincipal;
    private FinancialStatementResponse sampleResponse;
    private FinancialStatementStatusResponse sampleStatusResponse;
    private FinancialStatementItemResponse sampleItemResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleStatusResponse = FinancialStatementStatusResponse.builder()
                .id(100L)
                .status(FinancialStatementStatus.PROCESSING)
                .statusDisplayName("Đang xử lý OCR")
                .uploadedAt(LocalDateTime.now())
                .processingStartedAt(LocalDateTime.now())
                .build();

        sampleItemResponse = FinancialStatementItemResponse.builder()
                .id(201L)
                .statementType(FinancialStatementType.BALANCE_SHEET)
                .statementTypeDisplayName("Bảng cân đối kế toán")
                .code("100")
                .key("CURRENT_ASSETS")
                .label("Tài sản ngắn hạn")
                .rawLabel("Tài sản ngắn hạn")
                .current(new BigDecimal("29403000000000"))
                .previous(new BigDecimal("28150000000000"))
                .originalCurrentValue(new BigDecimal("29403000000000"))
                .originalPreviousValue(new BigDecimal("28150000000000"))
                .unit("VND")
                .mappingStatus(MappingStatus.MATCHED)
                .mappingStatusDisplayName("Khớp chuẩn")
                .edited(false)
                .build();

        sampleResponse = FinancialStatementResponse.builder()
                .id(100L)
                .companyId(10L)
                .companyName("Công ty ABC")
                .fileName("bctc_2023.pdf")
                .fileSize(1048576L)
                .contentType("application/pdf")
                .accountingStandard("TT200")
                .fiscalYear(2023)
                .period("current")
                .unit("VND")
                .processingStatus(FinancialStatementStatus.COMPLETED)
                .statusDisplayName("Xử lý OCR thành công")
                .items(List.of(sampleItemResponse))
                .build();

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return samplePrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(statementController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(), authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/financial-statements/upload returns 202 Accepted with QUEUED status")
    void uploadStatement_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bctc.pdf", "application/pdf", "dummy pdf content".getBytes()
        );

        FinancialStatementUploadResponse uploadResponse = FinancialStatementUploadResponse.builder()
                .id(100L)
                .status(FinancialStatementStatus.QUEUED)
                .message("Financial statement has been received and queued for processing.")
                .build();

        when(statementService.uploadFinancialStatement(eq(1L), any(), any(), any(), any()))
                .thenReturn(uploadResponse);

        mockMvc.perform(multipart("/api/financial-statements/upload")
                        .file(file)
                        .param("period", "current"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{id}/status returns 200 OK with status details")
    void getStatus_Success() throws Exception {
        when(statementService.getFinancialStatementStatus(1L, 100L)).thenReturn(sampleStatusResponse);

        mockMvc.perform(get("/api/financial-statements/100/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{id} returns 200 OK with full details")
    void getFinancialStatement_Success() throws Exception {
        when(statementService.getFinancialStatement(1L, 100L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/financial-statements/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.fileName").value("bctc_2023.pdf"))
                .andExpect(jsonPath("$.data.accountingStandard").value("TT200"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{id} returns 404 when statement not found")
    void getFinancialStatement_NotFound() throws Exception {
        when(statementService.getFinancialStatement(1L, 999L))
                .thenThrow(new StatementNotFoundException(999L));

        mockMvc.perform(get("/api/financial-statements/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("STATEMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/financial-statements/{id}/items returns 200 OK with indicators list")
    void getItems_Success() throws Exception {
        when(statementService.getFinancialStatementItems(1L, 100L)).thenReturn(List.of(sampleItemResponse));

        mockMvc.perform(get("/api/financial-statements/100/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("100"))
                .andExpect(jsonPath("$.data[0].key").value("CURRENT_ASSETS"));
    }

    @Test
    @DisplayName("PUT /api/financial-statements/{id}/items returns 200 OK with updated items")
    void updateItems_Success() throws Exception {
        FinancialStatementItemUpdateRequest request = FinancialStatementItemUpdateRequest.builder()
                .items(List.of(new FinancialStatementItemUpdateRequest.ItemUpdate(201L, new BigDecimal("30000000000000"), null)))
                .build();

        sampleItemResponse.setCurrent(new BigDecimal("30000000000000"));
        sampleItemResponse.setEdited(true);

        when(statementService.updateFinancialStatementItems(eq(1L), eq(100L), any()))
                .thenReturn(List.of(sampleItemResponse));

        mockMvc.perform(put("/api/financial-statements/100/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].edited").value(true));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{id}/items/reset returns 200 OK with reset items")
    void resetItems_Success() throws Exception {
        when(statementService.resetFinancialStatementItems(1L, 100L)).thenReturn(List.of(sampleItemResponse));

        mockMvc.perform(post("/api/financial-statements/100/items/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].edited").value(false));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{id}/confirm returns 200 OK with confirmed status")
    void confirmData_Success() throws Exception {
        sampleResponse.setProcessingStatus(FinancialStatementStatus.DATA_CONFIRMED);
        sampleResponse.setStatusDisplayName("Dữ liệu tài chính đã xác nhận");

        when(statementService.confirmFinancialData(1L, 100L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/financial-statements/100/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.processingStatus").value("DATA_CONFIRMED"));
    }

    @Test
    @DisplayName("POST /api/financial-statements/{id}/cancel returns 200 OK with cancellation response")
    void cancelProcessing_Success() throws Exception {
        sampleStatusResponse.setStatus(FinancialStatementStatus.CANCELLED);
        sampleStatusResponse.setStatusDisplayName("Đã hủy xử lý");

        when(statementService.cancelProcessing(1L, 100L)).thenReturn(sampleStatusResponse);

        mockMvc.perform(post("/api/financial-statements/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /api/financial-statements returns 200 OK with paginated list")
    void getFinancialStatements_Success() throws Exception {
        PageResponse<FinancialStatementResponse> pageResponse = PageResponse.<FinancialStatementResponse>builder()
                .items(List.of(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(statementService.getFinancialStatements(eq(1L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/financial-statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(100));
    }
}

