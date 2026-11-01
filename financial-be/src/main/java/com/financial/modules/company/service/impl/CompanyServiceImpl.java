package com.financial.modules.company.service.impl;

import com.financial.common.exception.CompanyAlreadyExistsException;
import com.financial.common.exception.CompanyNotFoundException;
import com.financial.common.exception.TaxCodeAlreadyExistsException;
import com.financial.common.exception.UserNotFoundException;
import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.entity.Company;
import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.company.mapper.CompanyMapper;
import com.financial.modules.company.repository.CompanyRepository;
import com.financial.modules.company.service.CompanyService;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(Long userId, CompanyRequest request) {
        // Business Rule: Each user can own exactly ONE company. Even soft-deleted companies preserve the constraint.
        if (companyRepository.existsByUserId(userId)) {
            throw new CompanyAlreadyExistsException("User already owns a company. Multiple companies are not allowed.");
        }

        String normalizedTaxCode = normalizeTaxCode(request.getTaxCode());
        if (companyRepository.existsByTaxCode(normalizedTaxCode)) {
            throw new TaxCodeAlreadyExistsException("Tax code already registered: " + normalizedTaxCode);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Company company = companyMapper.toEntity(request, user, normalizedTaxCode);
        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getMyCompany(Long userId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found for the authenticated user"));

        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateMyCompany(Long userId, CompanyRequest request) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found for the authenticated user"));

        String normalizedTaxCode = normalizeTaxCode(request.getTaxCode());
        if (!normalizedTaxCode.equalsIgnoreCase(company.getTaxCode())
                && companyRepository.existsByTaxCodeAndIdNot(normalizedTaxCode, company.getId())) {
            throw new TaxCodeAlreadyExistsException("Tax code already registered: " + normalizedTaxCode);
        }

        companyMapper.updateEntity(company, request, normalizedTaxCode);
        Company updatedCompany = companyRepository.save(company);

        return companyMapper.toResponse(updatedCompany);
    }

    @Override
    @Transactional
    public CompanyResponse updateMyCompanyStatus(Long userId, CompanyStatus status) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found for the authenticated user"));

        company.setStatus(status);
        Company updatedCompany = companyRepository.save(company);

        return companyMapper.toResponse(updatedCompany);
    }

    @Override
    @Transactional
    public void deleteMyCompany(Long userId) {
        Company company = companyRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found for the authenticated user"));

        // Soft deletion
        company.setDeleted(true);
        companyRepository.save(company);
    }

    private String normalizeTaxCode(String taxCode) {
        if (taxCode == null) {
            return null;
        }
        return taxCode.trim().toUpperCase();
    }
}

