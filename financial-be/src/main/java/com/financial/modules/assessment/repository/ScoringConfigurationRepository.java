package com.financial.modules.assessment.repository;

import com.financial.modules.assessment.entity.ScoringConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoringConfigurationRepository extends JpaRepository<ScoringConfiguration, Long> {

    Optional<ScoringConfiguration> findFirstByIsActiveTrueAndIsApprovedTrue();

    boolean existsByConfigName(String configName);
}

