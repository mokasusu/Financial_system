package com.financial.modules.company.service;

import com.financial.common.exception.CompanyAlreadyExistsException;
import com.financial.common.exception.CompanyNotFoundException;
import com.financial.common.exception.TaxCodeAlreadyExistsException;
import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.company.enums.CompanyType;
import com.financial.modules.company.mapper.CompanyMapper;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.company.service.impl.CompanyServiceImpl;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import com.financial.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private CompanyMapper companyMapper = new CompanyMapper();

    private CompanyServiceImpl companyService;

    private User sampleUser;
    private Company sampleCompany;
    private CompanyRequest sampleRequest;

    @BeforeEach
    void setUp() {
        companyService = new CompanyServiceImpl(companyRepository, userRepository, companyMapper);

        sampleUser = User.builder()
                .id(1L)
                .fullName("Alice Johnson")
                .email("alice@example.com")
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .build();

        sampleCompany = Company.builder()
                .id(10L)
                .user(sampleUser)
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
                .status(CompanyStatus.ACTIVE)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
    }

    @Test
    @DisplayName("createCompany should save company and return response when user has no company")
    void createCompany_Success() {
        when(companyRepository.existsByUserId(1L)).thenReturn(false);
        when(companyRepository.existsByTaxCode("0101234567")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        CompanyResponse response = companyService.createCompany(1L, sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTaxCode()).isEqualTo("0101234567");
        assertThat(response.getCompanyName()).isEqualTo("Công ty Cổ phần Công nghệ Y tế Sao Mai");
        assertThat(response.getOwnerId()).isEqualTo(1L);
        assertThat(response.getCompanyTypeDisplayName()).isEqualTo("Công ty cổ phần");
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany should reject when user already owns a company")
    void createCompany_UserAlreadyHasCompany_ThrowsException() {
        when(companyRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(1L, sampleRequest))
                .isInstanceOf(CompanyAlreadyExistsException.class)
                .hasMessageContaining("User already owns a company");

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany should reject duplicate tax code")
    void createCompany_DuplicateTaxCode_ThrowsException() {
        when(companyRepository.existsByUserId(1L)).thenReturn(false);
        when(companyRepository.existsByTaxCode("0101234567")).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(1L, sampleRequest))
                .isInstanceOf(TaxCodeAlreadyExistsException.class)
                .hasMessageContaining("Tax code already registered");

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("getMyCompany should return user's company when exists and not deleted")
    void getMyCompany_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));

        CompanyResponse response = companyService.getMyCompany(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTaxCode()).isEqualTo("0101234567");
        assertThat(response.getOwnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMyCompany should throw CompanyNotFoundException when company does not exist")
    void getMyCompany_NotFound_ThrowsException() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getMyCompany(1L))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    @DisplayName("updateMyCompany should update information successfully")
    void updateMyCompany_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        sampleRequest.setCompanyName("Tên Công ty Mới");
        CompanyResponse response = companyService.updateMyCompany(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(companyRepository).save(sampleCompany);
        assertThat(sampleCompany.getCompanyName()).isEqualTo("Tên Công ty Mới");
    }

    @Test
    @DisplayName("updateMyCompany with another company's tax code should throw TaxCodeAlreadyExistsException")
    void updateMyCompany_DuplicateTaxCode_ThrowsException() {
        sampleRequest.setTaxCode("9999999999");
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.existsByTaxCodeAndIdNot("9999999999", 10L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.updateMyCompany(1L, sampleRequest))
                .isInstanceOf(TaxCodeAlreadyExistsException.class)
                .hasMessageContaining("Tax code already registered");

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("updateMyCompanyStatus should update status successfully")
    void updateMyCompanyStatus_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        CompanyResponse response = companyService.updateMyCompanyStatus(1L, CompanyStatus.INACTIVE);

        assertThat(response).isNotNull();
        assertThat(sampleCompany.getStatus()).isEqualTo(CompanyStatus.INACTIVE);
        verify(companyRepository).save(sampleCompany);
    }

    @Test
    @DisplayName("deleteMyCompany should set deleted to true (soft delete)")
    void deleteMyCompany_Success() {
        when(companyRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        companyService.deleteMyCompany(1L);

        assertThat(sampleCompany.getDeleted()).isTrue();
        verify(companyRepository).save(sampleCompany);
    }
}

