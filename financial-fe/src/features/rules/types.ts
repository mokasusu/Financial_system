export type ComparisonOperator =
  | "GREATER_THAN_OR_EQUAL"
  | "LESS_THAN_OR_EQUAL"
  | "GREATER_THAN"
  | "LESS_THAN"
  | "BETWEEN"
  | "EQUAL";

export type FinancialUnit = "TIMES" | "PERCENT" | "DAYS" | "VND";

export interface EvaluationCriterion {
  id: number;
  ratioDefinitionId?: number;
  ratioCode: string;
  ratioName: string;
  criterionName: string;
  comparisonOperator: ComparisonOperator;
  thresholdValue: number;
  minimumValue?: number | null;
  maximumValue?: number | null;
  unit: FinancialUnit;
  description?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface UpdateCriterionRequest {
  criterionName: string;
  comparisonOperator: ComparisonOperator;
  thresholdValue: number;
  minimumValue?: number | null;
  maximumValue?: number | null;
  unit: FinancialUnit;
  description?: string;
  isActive: boolean;
}

export type ValidationSeverity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "INFO";
export type ValidationCategory =
  | "ACCOUNTING_EQUATION"
  | "DATA_COMPLETENESS"
  | "PLAUSIBILITY"
  | "PERIOD_CONSISTENCY";

export interface ValidationRule {
  id: number;
  ruleCode: string;
  ruleName: string;
  description?: string;
  accountingStandard?: "TT200" | "TT133" | "TT99" | null;
  statementType?: "BALANCE_SHEET" | "INCOME_STATEMENT" | "CASH_FLOW" | null;
  category: ValidationCategory;
  categoryDisplayName?: string;
  severity: ValidationSeverity;
  severityDisplayName?: string;
  configuration?: string;
  toleranceValue: number;
  toleranceType?: "ABSOLUTE" | "PERCENTAGE";
  isActive: boolean;
}

export interface UpdateValidationRuleRequest {
  ruleCode: string;
  ruleName: string;
  description?: string;
  accountingStandard?: "TT200" | "TT133" | "TT99" | null;
  statementType?: "BALANCE_SHEET" | "INCOME_STATEMENT" | "CASH_FLOW" | null;
  category: ValidationCategory;
  severity: ValidationSeverity;
  configuration?: string;
  toleranceValue: number;
  toleranceType?: "ABSOLUTE" | "PERCENTAGE";
  isActive: boolean;
}

