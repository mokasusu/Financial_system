package com.financial.modules.validation.validator;

import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.validation.entity.ValidationResult;
import com.financial.modules.validation.entity.ValidationRule;
import com.financial.modules.validation.enums.ValidationCategory;

public interface CategoryValidator {

    ValidationCategory getCategory();

    ValidationResult validate(FinancialStatement statement, ValidationRule rule);
}

