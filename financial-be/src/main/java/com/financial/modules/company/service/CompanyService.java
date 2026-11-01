package com.financial.modules.company.service;

import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.enums.CompanyStatus;

public interface CompanyService {

    CompanyResponse createCompany(Long userId, CompanyRequest request);

    CompanyResponse getMyCompany(Long userId);

    CompanyResponse updateMyCompany(Long userId, CompanyRequest request);

    CompanyResponse updateMyCompanyStatus(Long userId, CompanyStatus status);

    void deleteMyCompany(Long userId);
}

