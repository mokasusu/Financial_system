package com.financial.modules.company.mapper;

import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequest request, User user, String normalizedTaxCode) {
        if (request == null) {
            return null;
        }

        return Company.builder()
                .user(user)
                .taxCode(normalizedTaxCode)
                .companyName(request.getCompanyName().trim())
                .companyNameEn(request.getCompanyNameEn() != null ? request.getCompanyNameEn().trim() : null)
                .shortName(request.getShortName() != null ? request.getShortName().trim() : null)
                .companyType(request.getCompanyType())
                .mainBusinessLine(request.getMainBusinessLine().trim())
                .establishedDate(request.getEstablishedDate())
                .legalRepresentative(request.getLegalRepresentative() != null ? request.getLegalRepresentative().trim() : null)
                .address(request.getAddress().trim())
                .ward(request.getWard().trim())
                .province(request.getProvince().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .website(request.getWebsite() != null ? request.getWebsite().trim() : null)
                .status(CompanyStatus.ACTIVE)
                .deleted(false)
                .build();
    }

    public void updateEntity(Company company, CompanyRequest request, String normalizedTaxCode) {
        if (company == null || request == null) {
            return;
        }

        company.setTaxCode(normalizedTaxCode);
        company.setCompanyName(request.getCompanyName().trim());
        company.setCompanyNameEn(request.getCompanyNameEn() != null ? request.getCompanyNameEn().trim() : null);
        company.setShortName(request.getShortName() != null ? request.getShortName().trim() : null);
        company.setCompanyType(request.getCompanyType());
        company.setMainBusinessLine(request.getMainBusinessLine().trim());
        company.setEstablishedDate(request.getEstablishedDate());
        company.setLegalRepresentative(request.getLegalRepresentative() != null ? request.getLegalRepresentative().trim() : null);
        company.setAddress(request.getAddress().trim());
        company.setWard(request.getWard().trim());
        company.setProvince(request.getProvince().trim());
        company.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        company.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        company.setWebsite(request.getWebsite() != null ? request.getWebsite().trim() : null);
    }

    public CompanyResponse toResponse(Company company) {
        if (company == null) {
            return null;
        }

        Long ownerId = null;
        String ownerName = null;
        if (company.getUser() != null) {
            ownerId = company.getUser().getId();
            ownerName = company.getUser().getFullName();
        }

        return CompanyResponse.builder()
                .id(company.getId())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .taxCode(company.getTaxCode())
                .companyName(company.getCompanyName())
                .companyNameEn(company.getCompanyNameEn())
                .shortName(company.getShortName())
                .companyType(company.getCompanyType())
                .companyTypeDisplayName(company.getCompanyType() != null ? company.getCompanyType().getDisplayName() : null)
                .mainBusinessLine(company.getMainBusinessLine())
                .establishedDate(company.getEstablishedDate())
                .legalRepresentative(company.getLegalRepresentative())
                .address(company.getAddress())
                .ward(company.getWard())
                .province(company.getProvince())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .status(company.getStatus())
                .statusDisplayName(company.getStatus() != null ? company.getStatus().getDisplayName() : null)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}

