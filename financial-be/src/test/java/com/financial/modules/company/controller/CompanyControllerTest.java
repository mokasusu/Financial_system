package com.financial.modules.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.common.exception.CompanyAlreadyExistsException;
import com.financial.common.exception.CompanyNotFoundException;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.TaxCodeAlreadyExistsException;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.dto.CompanyStatusUpdateRequest;
import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.company.enums.CompanyType;
import com.financial.modules.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    private UserPrincipal samplePrincipal;
    private CompanyRequest sampleRequest;
    private CompanyResponse sampleResponse;

    @BeforeEach
    void setUp() {
        samplePrincipal = UserPrincipal.builder()
                .id(1L)
                .fullName("Alice Johnson")
                .email("alice@example.com")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        sampleRequest = CompanyRequest.builder()
                .taxCode("0101234567")
                .companyName("Công ty Cổ phần Công nghệ Y tế Sao Mai")
                .companyNameEn("Sao Mai Health Tech JSC")
                .shortName("SAOMAI TECH")
                .companyType(CompanyType.JOINT_STOCK_COMPANY)
                .mainBusinessLine("Lập trình máy vi tính")
                .establishedDate(LocalDate.of(2020, 5, 15))
                .legalRepresentative("Nguyễn Văn An")
                .address("Số 123 Đường Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .province("Thành phố Hà Nội")
                .phone("+842438889999")
                .email("contact@saomaitech.vn")
                .website("https://saomaitech.vn")
                .build();

        sampleResponse = CompanyResponse.builder()
                .id(10L)
                .ownerId(1L)
                .ownerName("Alice Johnson")
                .taxCode("0101234567")
                .companyName("Công ty Cổ phần Công nghệ Y tế Sao Mai")
                .companyNameEn("Sao Mai Health Tech JSC")
                .shortName("SAOMAI TECH")
                .companyType(CompanyType.JOINT_STOCK_COMPANY)
                .companyTypeDisplayName(CompanyType.JOINT_STOCK_COMPANY.getDisplayName())
                .mainBusinessLine("Lập trình máy vi tính")
                .establishedDate(LocalDate.of(2020, 5, 15))
                .legalRepresentative("Nguyễn Văn An")
                .address("Số 123 Đường Cầu Giấy")
                .ward("Phường Dịch Vọng Hậu")
                .province("Thành phố Hà Nội")
                .phone("+842438889999")
                .email("contact@saomaitech.vn")
                .website("https://saomaitech.vn")
                .status(CompanyStatus.ACTIVE)
                .statusDisplayName(CompanyStatus.ACTIVE.getDisplayName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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

        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/companies should return 201 Created when request is valid")
    void createCompany_Success() throws Exception {
        when(companyService.createCompany(eq(1L), any(CompanyRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.taxCode").value("0101234567"))
                .andExpect(jsonPath("$.data.companyName").value("Công ty Cổ phần Công nghệ Y tế Sao Mai"))
                .andExpect(jsonPath("$.data.companyTypeDisplayName").value("Công ty cổ phần"))
                .andExpect(jsonPath("$.data.statusDisplayName").value("Đang hoạt động"));
    }

    @Test
    @DisplayName("POST /api/companies should return 400 Bad Request when tax code is invalid format")
    void createCompany_InvalidTaxCode_ReturnsBadRequest() throws Exception {
        sampleRequest.setTaxCode("12345"); // invalid length

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.taxCode").exists());
    }

    @Test
    @DisplayName("POST /api/companies should return 400 Bad Request when required fields are missing")
    void createCompany_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        CompanyRequest emptyRequest = new CompanyRequest();

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.companyName").exists())
                .andExpect(jsonPath("$.data.taxCode").exists())
                .andExpect(jsonPath("$.data.companyType").exists())
                .andExpect(jsonPath("$.data.address").exists())
                .andExpect(jsonPath("$.data.ward").exists())
                .andExpect(jsonPath("$.data.province").exists());
    }

    @Test
    @DisplayName("POST /api/companies should return 409 Conflict when user already owns a company")
    void createCompany_UserAlreadyOwnsCompany_ReturnsConflict() throws Exception {
        when(companyService.createCompany(eq(1L), any(CompanyRequest.class)))
                .thenThrow(new CompanyAlreadyExistsException("Authenticated user already owns a registered company"));

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authenticated user already owns a registered company"));
    }

    @Test
    @DisplayName("POST /api/companies should return 409 Conflict when tax code already registered")
    void createCompany_DuplicateTaxCode_ReturnsConflict() throws Exception {
        when(companyService.createCompany(eq(1L), any(CompanyRequest.class)))
                .thenThrow(new TaxCodeAlreadyExistsException("Tax code 0101234567 is already registered"));

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tax code 0101234567 is already registered"));
    }

    @Test
    @DisplayName("GET /api/companies/me should return 200 OK with company details")
    void getMyCompany_Success() throws Exception {
        when(companyService.getMyCompany(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/companies/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.taxCode").value("0101234567"))
                .andExpect(jsonPath("$.data.ownerId").value(1))
                .andExpect(jsonPath("$.data.ownerName").value("Alice Johnson"));
    }

    @Test
    @DisplayName("GET /api/companies/me should return 404 Not Found when company does not exist")
    void getMyCompany_NotFound() throws Exception {
        when(companyService.getMyCompany(1L))
                .thenThrow(new CompanyNotFoundException("No active company found for user ID: 1"));

        mockMvc.perform(get("/api/companies/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No active company found for user ID: 1"));
    }

    @Test
    @DisplayName("PUT /api/companies/me should return 200 OK when update is successful")
    void updateMyCompany_Success() throws Exception {
        sampleResponse.setCompanyName("Công ty Cổ phần Công nghệ Y tế Sao Mai Cập Nhật");
        when(companyService.updateMyCompany(eq(1L), any(CompanyRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/companies/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.companyName").value("Công ty Cổ phần Công nghệ Y tế Sao Mai Cập Nhật"));
    }

    @Test
    @DisplayName("PATCH /api/companies/me/status should return 200 OK when updating status")
    void updateMyCompanyStatus_Success() throws Exception {
        sampleResponse.setStatus(CompanyStatus.INACTIVE);
        sampleResponse.setStatusDisplayName(CompanyStatus.INACTIVE.getDisplayName());

        when(companyService.updateMyCompanyStatus(1L, CompanyStatus.INACTIVE)).thenReturn(sampleResponse);

        CompanyStatusUpdateRequest statusRequest = CompanyStatusUpdateRequest.builder()
                .status(CompanyStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/companies/me/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.statusDisplayName").value("Tạm ngừng hoạt động"));
    }

    @Test
    @DisplayName("PATCH /api/companies/me/status should return 400 Bad Request when status is missing")
    void updateMyCompanyStatus_MissingStatus_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/companies/me/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.status").exists());
    }

    @Test
    @DisplayName("DELETE /api/companies/me should return 200 OK when soft delete is successful")
    void deleteMyCompany_Success() throws Exception {
        doNothing().when(companyService).deleteMyCompany(1L);

        mockMvc.perform(delete("/api/companies/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Company deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/companies/me should return 404 Not Found when company does not exist")
    void deleteMyCompany_NotFound() throws Exception {
        doThrow(new CompanyNotFoundException("No active company found for user ID: 1"))
                .when(companyService).deleteMyCompany(1L);

        mockMvc.perform(delete("/api/companies/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

