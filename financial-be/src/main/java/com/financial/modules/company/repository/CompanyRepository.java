package com.financial.modules.company.repository;

import com.financial.modules.company.entity.Company;
import com.financial.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUserAndDeletedFalse(User user);

    Optional<Company> findByUserIdAndDeletedFalse(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    boolean existsByTaxCode(String taxCode);

    boolean existsByTaxCodeAndIdNot(String taxCode, Long id);
}

