package com.financial.modules.assessment.service.impl;

import com.financial.modules.assessment.dto.ScoringResult;
import com.financial.modules.assessment.entity.AssessmentCategoryScore;
import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.assessment.entity.ScoringConfiguration;
import com.financial.modules.assessment.repository.ScoringConfigurationRepository;
import com.financial.modules.assessment.service.AssessmentScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentScoringServiceImpl implements AssessmentScoringService {

    private final ScoringConfigurationRepository scoringConfigRepository;

    @Override
    public ScoringResult evaluateScoring(List<AssessmentDetail> details, List<AssessmentCategoryScore> categoryScores) {
        log.info("Evaluating assessment scoring. Checking for active approved scoring configuration...");

        Optional<ScoringConfiguration> activeConfigOpt = scoringConfigRepository.findFirstByIsActiveTrueAndIsApprovedTrue();

        if (activeConfigOpt.isEmpty()) {
            log.info("No approved active scoring configuration found. Returning scoringStatus: NOT_CONFIGURED.");
            return ScoringResult.notConfigured();
        }

        ScoringConfiguration config = activeConfigOpt.get();

        // Extensible check: if weights or classification rules are missing / empty
        if (config.getRatioWeightsJson() == null || config.getCategoryWeightsJson() == null
                || config.getClassificationRulesJson() == null) {
            log.warn("Scoring configuration '{}' is incomplete. Returning scoringStatus: PARTIAL.", config.getConfigName());
            return ScoringResult.partial(String.format("Cấu hình '%s' thiếu trọng số hoặc thang phân loại.", config.getConfigName()));
        }

        // When complete approved configuration is present in the future, calculation logic can be executed here.
        log.info("Scoring configuration '{}' is present but advanced weight evaluation is pending approved specification.", config.getConfigName());
        return ScoringResult.partial("Cấu hình trọng số đang chờ phê duyệt chi tiết.");
    }
}

