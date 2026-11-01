package com.financial.modules.financialindicator.service.impl;

import com.financial.common.exception.IndicatorNotFoundException;
import com.financial.common.exception.MappingNotFoundException;
import com.financial.common.exception.RatioNotFoundException;
import com.financial.common.exception.UnsupportedAccountingStandardException;
import com.financial.modules.financialindicator.dto.AccountingIndicatorMappingResponse;
import com.financial.modules.financialindicator.dto.FinancialIndicatorResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioInputResponse;
import com.financial.modules.financialindicator.dto.FinancialRatioResponse;
import com.financial.modules.financialindicator.entity.AccountingIndicatorMapping;
import com.financial.modules.financialindicator.entity.FinancialIndicator;
import com.financial.modules.financialindicator.entity.FinancialRatioDefinition;
import com.financial.modules.financialindicator.enums.AccountingStandard;
import com.financial.modules.financialindicator.enums.FinancialRatioCategory;
import com.financial.modules.financialindicator.enums.FinancialStatementType;
import com.financial.modules.financialindicator.enums.MappingStatus;
import com.financial.modules.financialindicator.mapper.FinancialIndicatorMapper;
import com.financial.modules.financialindicator.repository.AccountingIndicatorMappingRepository;
import com.financial.modules.financialindicator.repository.FinancialIndicatorRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioDefinitionRepository;
import com.financial.modules.financialindicator.repository.FinancialRatioInputRepository;
import com.financial.modules.financialindicator.service.FinancialIndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialIndicatorServiceImpl implements FinancialIndicatorService {

    private final FinancialIndicatorRepository indicatorRepository;
    private final FinancialRatioDefinitionRepository ratioRepository;
    private final FinancialRatioInputRepository ratioInputRepository;
    private final AccountingIndicatorMappingRepository mappingRepository;
    private final FinancialIndicatorMapper mapper;

    // ==========================================
    // Financial Indicator
    // ==========================================

    @Override
    public Page<FinancialIndicatorResponse> getIndicators(FinancialStatementType statementType, Boolean activeOnly,
            Pageable pageable) {
        Specification<FinancialIndicator> spec = (root, query, cb) -> cb.conjunction();

        if (statementType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statementType"), statementType));
        }

        if (activeOnly != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), activeOnly));
        }

        return indicatorRepository.findAll(spec, pageable).map(mapper::toIndicatorResponse);
    }

    @Override
    public FinancialIndicatorResponse getIndicatorById(Long id) {
        FinancialIndicator indicator = indicatorRepository.findById(id)
                .orElseThrow(() -> new IndicatorNotFoundException(id));
        return mapper.toIndicatorResponse(indicator);
    }

    @Override
    public FinancialIndicatorResponse getIndicatorByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Indicator code must not be empty");
        }
        FinancialIndicator indicator = indicatorRepository.findByIndicatorCode(code.trim().toUpperCase())
                .orElseThrow(() -> IndicatorNotFoundException.byCode(code));
        return mapper.toIndicatorResponse(indicator);
    }

    @Override
    public List<FinancialIndicatorResponse> getIndicatorsByStandard(String standardStr) {
        AccountingStandard standard = parseAccountingStandard(standardStr);
        List<FinancialIndicator> indicators = indicatorRepository.findDistinctByAccountingStandard(standard);
        return mapper.toIndicatorResponseList(indicators);
    }

    // ==========================================
    // Financial Ratio
    // ==========================================

    @Override
    public Page<FinancialRatioResponse> getRatios(FinancialRatioCategory category, Boolean activeOnly,
            Pageable pageable) {
        Specification<FinancialRatioDefinition> spec = (root, query, cb) -> cb.conjunction();

        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }

        if (activeOnly != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), activeOnly));
        }

        return ratioRepository.findAll(spec, pageable).map(mapper::toRatioResponse);
    }

    @Override
    public FinancialRatioResponse getRatioByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Ratio code must not be empty");
        }
        FinancialRatioDefinition ratio = ratioRepository.findByRatioCode(code.trim().toUpperCase())
                .orElseThrow(() -> RatioNotFoundException.byCode(code));
        return mapper.toRatioResponse(ratio);
    }

    @Override
    public List<FinancialRatioResponse> getRatiosByCategory(FinancialRatioCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Ratio category must not be null");
        }
        List<FinancialRatioDefinition> ratios = ratioRepository.findByCategoryAndIsActiveTrue(category);
        return mapper.toRatioResponseList(ratios);
    }

    @Override
    public List<FinancialRatioResponse> getCoreKpis() {
        List<FinancialRatioDefinition> kpis = ratioRepository.findByIsCoreKpiTrueAndIsActiveTrue();
        return mapper.toRatioResponseList(kpis);
    }

    @Override
    public List<FinancialRatioInputResponse> getRatioInputs(String ratioCode) {
        if (ratioCode == null || ratioCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Ratio code must not be empty");
        }
        String normalizedCode = ratioCode.trim().toUpperCase();
        // Check ratio existence
        if (!ratioRepository.existsByRatioCode(normalizedCode)) {
            throw RatioNotFoundException.byCode(normalizedCode);
        }

        var inputs = ratioInputRepository.findByRatioDefinitionRatioCodeOrderByInputOrderAsc(normalizedCode);
        return mapper.toRatioInputResponseList(inputs);
    }

    // ==========================================
    // Accounting Mapping
    // ==========================================

    @Override
    public Page<AccountingIndicatorMappingResponse> getMappings(AccountingStandard standard,
            FinancialStatementType statementType, Pageable pageable) {
        Specification<AccountingIndicatorMapping> spec = (root, query, cb) -> cb.conjunction();

        if (standard != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accountingStandard"), standard));
        }

        if (statementType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statementType"), statementType));
        }

        return mappingRepository.findAll(spec, pageable).map(mapper::toMappingResponse);
    }

    @Override
    public List<AccountingIndicatorMappingResponse> getMappingsByStandard(AccountingStandard standard) {
        if (standard == null) {
            throw new IllegalArgumentException("Accounting standard must not be null");
        }
        List<AccountingIndicatorMapping> mappings = mappingRepository.findByAccountingStandard(standard);
        return mapper.toMappingResponseList(mappings);
    }

    @Override
    public FinancialIndicatorResponse resolveMapping(
            AccountingStandard standard,
            FinancialStatementType statementType,
            String sourceCode,
            String sourceKey
    ) {
        if (standard == null) {
            throw new IllegalArgumentException("Accounting standard must be provided");
        }
        if (statementType == null) {
            throw new IllegalArgumentException("Statement type must be provided");
        }
        boolean hasCode = sourceCode != null && !sourceCode.trim().isEmpty();
        boolean hasKey = sourceKey != null && !sourceKey.trim().isEmpty();

        if (!hasCode && !hasKey) {
            throw new IllegalArgumentException("Either sourceCode or sourceKey must be provided to resolve mapping");
        }

        Optional<AccountingIndicatorMapping> mappingOpt = Optional.empty();

        if (hasCode) {
            mappingOpt = mappingRepository.findFirstByAccountingStandardAndStatementTypeAndSourceCodeAndMappingStatus(
                    standard, statementType, sourceCode.trim(), MappingStatus.ACTIVE
            );
        }

        if (mappingOpt.isEmpty() && hasKey) {
            mappingOpt = mappingRepository.findFirstByAccountingStandardAndStatementTypeAndSourceKeyAndMappingStatus(
                    standard, statementType, sourceKey.trim(), MappingStatus.ACTIVE
            );
        }

        AccountingIndicatorMapping mapping = mappingOpt.orElseThrow(() ->
                MappingNotFoundException.forLookup(
                        standard.name(),
                        statementType.name(),
                        hasCode ? sourceCode.trim() : sourceKey.trim()
                )
        );

        return mapper.toIndicatorResponse(mapping.getIndicator());
    }

    private AccountingStandard parseAccountingStandard(String standardStr) {
        if (standardStr == null || standardStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Accounting standard must not be empty");
        }
        try {
            return AccountingStandard.fromString(standardStr);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedAccountingStandardException(standardStr);
        }
    }
}
