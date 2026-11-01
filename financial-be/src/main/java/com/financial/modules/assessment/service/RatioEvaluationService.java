package com.financial.modules.assessment.service;

import com.financial.modules.assessment.entity.AssessmentDetail;
import com.financial.modules.calculationengine.entity.FinancialCalculation;
import com.financial.modules.financialstatement.entity.FinancialStatement;

import java.util.List;

public interface RatioEvaluationService {

    List<AssessmentDetail> evaluateRatios(FinancialStatement statement, FinancialCalculation calculation);
}

