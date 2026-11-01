package com.financial.modules.assessment.service;

import com.financial.modules.assessment.dto.ScoringResult;
import com.financial.modules.assessment.entity.ScoringConfiguration;
import com.financial.modules.assessment.enums.AssessmentScoringStatus;
import com.financial.modules.assessment.enums.HealthClassification;
import com.financial.modules.assessment.repository.ScoringConfigurationRepository;
import com.financial.modules.assessment.service.impl.AssessmentScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentScoringServiceTest {

    @Mock
    private ScoringConfigurationRepository scoringConfigRepository;

    private AssessmentScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new AssessmentScoringServiceImpl(scoringConfigRepository);
    }

    @Test
    @DisplayName("When no scoring configuration is active/approved, return NOT_CONFIGURED and no invented score")
    void testNoScoringConfig_ReturnsNotConfigured() {
        when(scoringConfigRepository.findFirstByIsActiveTrueAndIsApprovedTrue())
                .thenReturn(Optional.empty());

        ScoringResult result = scoringService.evaluateScoring(Collections.emptyList(), Collections.emptyList());

        assertThat(result.getScoringStatus()).isEqualTo(AssessmentScoringStatus.NOT_CONFIGURED);
        assertThat(result.getOverallScore()).isNull();
        assertThat(result.getHealthClassification()).isEqualTo(HealthClassification.NOT_CONFIGURED);
    }

    @Test
    @DisplayName("When scoring configuration is incomplete, return PARTIAL without fake scores")
    void testIncompleteScoringConfig_ReturnsPartial() {
        ScoringConfiguration incompleteConfig = ScoringConfiguration.builder()
                .id(1L)
                .configName("Draft Model")
                .ratioWeightsJson(null) // missing weights
                .categoryWeightsJson("{\"LIQUIDITY\": 0.25}")
                .classificationRulesJson(null)
                .isActive(true)
                .isApproved(true)
                .build();

        when(scoringConfigRepository.findFirstByIsActiveTrueAndIsApprovedTrue())
                .thenReturn(Optional.of(incompleteConfig));

        ScoringResult result = scoringService.evaluateScoring(Collections.emptyList(), Collections.emptyList());

        assertThat(result.getScoringStatus()).isEqualTo(AssessmentScoringStatus.PARTIAL);
        assertThat(result.getOverallScore()).isNull();
        assertThat(result.getHealthClassification()).isEqualTo(HealthClassification.NOT_CONFIGURED);
        assertThat(result.getScoringNotes()).contains("thiếu trọng số");
    }
}

