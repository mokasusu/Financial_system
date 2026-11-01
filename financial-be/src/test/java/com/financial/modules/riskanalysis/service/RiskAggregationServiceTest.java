package com.financial.modules.riskanalysis.service;

import com.financial.modules.company.entity.Company;
import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.riskanalysis.dto.RiskSummaryResponse;
import com.financial.modules.riskanalysis.entity.IdentifiedRisk;
import com.financial.modules.riskanalysis.entity.RiskAnalysis;
import com.financial.modules.riskanalysis.enums.RiskAnalysisStatus;
import com.financial.modules.riskanalysis.enums.RiskCategory;
import com.financial.modules.riskanalysis.enums.RiskSeverity;
import com.financial.modules.riskanalysis.enums.RiskStatus;
import com.financial.modules.riskanalysis.mapper.RiskAnalysisMapper;
import com.financial.modules.riskanalysis.service.impl.RiskAggregationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskAggregationServiceTest {

    private RiskAggregationService aggregationService;
    private RiskAnalysisMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RiskAnalysisMapper();
        aggregationService = new RiskAggregationServiceImpl(mapper);
    }

    @Test
    @DisplayName("aggregateRiskSummary correctly calculates counts for all categories and severities")
    void testAggregateRiskSummary() {
        Company company = Company.builder().id(10L).companyName("Công ty ABC").build();
        FinancialStatement statement = FinancialStatement.builder().id(100L).build();

        IdentifiedRisk r1 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.LIQUIDITY)
                .severity(RiskSeverity.LOW)
                .riskStatus(RiskStatus.DETECTED)
                .build();

        IdentifiedRisk r2 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.SOLVENCY)
                .severity(RiskSeverity.MEDIUM)
                .riskStatus(RiskStatus.MONITORING)
                .build();

        IdentifiedRisk r3 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.PROFITABILITY)
                .severity(RiskSeverity.HIGH)
                .riskStatus(RiskStatus.DETECTED)
                .build();

        IdentifiedRisk r4 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.DATA_QUALITY)
                .severity(RiskSeverity.CRITICAL)
                .riskStatus(RiskStatus.DETECTED)
                .build();

        IdentifiedRisk r5 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.OPERATIONAL_EFFICIENCY)
                .severity(RiskSeverity.MEDIUM)
                .riskStatus(RiskStatus.RESOLVED)
                .build();

        IdentifiedRisk r6 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.LIQUIDITY)
                .riskStatus(RiskStatus.NOT_CONFIGURED)
                .build();

        IdentifiedRisk r7 = IdentifiedRisk.builder()
                .riskCategory(RiskCategory.SOLVENCY)
                .riskStatus(RiskStatus.INSUFFICIENT_DATA)
                .build();

        RiskAnalysis analysis = RiskAnalysis.builder()
                .id(501L)
                .company(company)
                .financialStatement(statement)
                .analysisVersion(1)
                .executionStatus(RiskAnalysisStatus.COMPLETED)
                .analyzedAt(LocalDateTime.now())
                .identifiedRisks(List.of(r1, r2, r3, r4, r5, r6, r7))
                .build();

        RiskSummaryResponse summary = aggregationService.aggregateRiskSummary(analysis);

        assertThat(summary).isNotNull();
        assertThat(summary.getRiskAnalysisId()).isEqualTo(501L);
        assertThat(summary.getFinancialStatementId()).isEqualTo(100L);
        assertThat(summary.getCompanyId()).isEqualTo(10L);
        assertThat(summary.getAnalysisVersion()).isEqualTo(1);
        assertThat(summary.getExecutionStatus()).isEqualTo(RiskAnalysisStatus.COMPLETED);

        // Active risks: r1, r2, r3, r4 -> 4
        assertThat(summary.getTotalRisks()).isEqualTo(4);
        assertThat(summary.getLowRisks()).isEqualTo(1);
        assertThat(summary.getMediumRisks()).isEqualTo(1);
        assertThat(summary.getHighRisks()).isEqualTo(1);
        assertThat(summary.getCriticalRisks()).isEqualTo(1);
        assertThat(summary.getResolvedRisks()).isEqualTo(1);
        assertThat(summary.getNotConfiguredCount()).isEqualTo(1);
        assertThat(summary.getInsufficientDataCount()).isEqualTo(1);

        // Check category breakdown (for active risks)
        assertThat(summary.getCategoryCounts().get(RiskCategory.LIQUIDITY)).isEqualTo(1);
        assertThat(summary.getCategoryCounts().get(RiskCategory.SOLVENCY)).isEqualTo(1);
        assertThat(summary.getCategoryCounts().get(RiskCategory.PROFITABILITY)).isEqualTo(1);
        assertThat(summary.getCategoryCounts().get(RiskCategory.DATA_QUALITY)).isEqualTo(1);
        assertThat(summary.getCategoryCounts().get(RiskCategory.OPERATIONAL_EFFICIENCY)).isEqualTo(0);

        // Check severity breakdown
        assertThat(summary.getSeverityCounts().get(RiskSeverity.LOW)).isEqualTo(1);
        assertThat(summary.getSeverityCounts().get(RiskSeverity.MEDIUM)).isEqualTo(1);
        assertThat(summary.getSeverityCounts().get(RiskSeverity.HIGH)).isEqualTo(1);
        assertThat(summary.getSeverityCounts().get(RiskSeverity.CRITICAL)).isEqualTo(1);
    }
}

