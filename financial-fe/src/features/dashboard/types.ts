export interface DashboardContext {
  companyId: number;
  companyName: string;
  taxCode: string;
  businessSector?: string;
  accountingStandard: string;
  selectedPeriod: string;
  currencyUnit: string;
  financialStatementId?: number;
  financialStatementProcessingStatus?: string;
  fileName?: string;
  hasCompany: boolean;
  hasFinancialStatement: boolean;
  dataStatus: string;
}

export interface DashboardDataStatus {
  companyStatus: string;
  statementStatus: string;
  calculationStatus: string;
  validationStatus: string;
  assessmentStatus: string;
  riskStatus: string;
  aiStatus: string;
}

export interface DashboardKPI {
  code: string;
  name: string;
  value: number | null;
  unit: string;
  calculationStatus: string;
  evaluationStatus: string; // "PASS" | "FAIL" | "NOT_CONFIGURED" | "WARNING"
  configuredThreshold: number | null;
  comparisonOperator: string;
  explanation?: string;
}

export interface RatioItem {
  code: string;
  name: string;
  value: number | null;
  unit: string;
  calculationStatus: string;
  evaluationStatus: string;
  configuredThreshold: number | null;
  comparisonOperator: string;
  formula?: string;
}

export interface DashboardCategory {
  categoryCode: string;
  categoryName: string;
  ratios: RatioItem[];
}

export interface RatioComparison {
  ratioCode: string;
  ratioName: string;
  category: string;
  actualValue: number | null;
  unit: string;
  configuredThreshold: number | null;
  comparisonOperator: string;
  evaluationStatus: string;
  industryBenchmark: number | null;
  benchmarkStatus: string;
}

export interface CapitalStructure {
  totalAssets: number;
  totalLiabilities: number;
  equity: number;
  unit: string;
  debtRatio?: number;
  equityRatio?: number;
}

export interface DashboardVisualAnalytics {
  ratioComparisons: RatioComparison[];
  capitalStructure?: CapitalStructure;
  categoryOverview?: DashboardCategory[];
}

export interface ValidationResultItem {
  ruleCode: string;
  ruleName: string;
  status: string; // "PASS" | "FAIL" | "WARNING"
  severity?: string;
  differenceAmount?: number;
  leftSideValue?: number;
  rightSideValue?: number;
  message?: string;
  sourceReference?: string;
}

export interface DashboardValidation {
  validationId: number;
  overallStatus: string; // "PASS" | "FAIL" | "WARNING"
  accountingStandard: string;
  totalRules: number;
  passedRules: number;
  warningRules: number;
  failedRules: number;
  results: ValidationResultItem[];
}

export interface RiskSummary {
  lowRisks: number;
  mediumRisks: number;
  highRisks: number;
  criticalRisks: number;
  totalRisks: number;
}

export interface IdentifiedRisk {
  ruleCode: string;
  title: string;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  actualValue?: number;
  thresholdValue?: number;
  description?: string;
  recommendation?: string;
}

export interface DashboardRiskOverview {
  summary: RiskSummary;
  identifiedRisks: IdentifiedRisk[];
}

export interface AIStrength {
  title: string;
  detail: string;
  ratioCode?: string;
}

export interface AIRisk {
  title: string;
  detail: string;
  severity: "LOW" | "MEDIUM" | "HIGH";
  ratioCode?: string;
}

export interface AIRecommendation {
  priority: "HIGH" | "MEDIUM" | "LOW";
  title: string;
  action: string;
}

export interface DashboardAICommentary {
  analysisId: number;
  executiveSummary: string;
  strengths: AIStrength[];
  risks: AIRisk[];
  recommendations: AIRecommendation[];
  limitations: string[];
  aiGenerationStatus: string;
  reviewStatus: string;
  aiGeneratedComment?: string;
  finalComment?: string;
  generatedAt?: string;
  completedAt?: string;
}

export interface DashboardOverview {
  context: DashboardContext;
  overallScore?: number | null;
  healthClassification?: string | null;
  healthClassificationDisplayName?: string | null;
  dataStatuses: DashboardDataStatus;
  coreKpis: DashboardKPI[];
  financialRatios: DashboardCategory[];
  visualAnalytics: DashboardVisualAnalytics;
  validation: DashboardValidation;
  riskOverview: DashboardRiskOverview;
  aiCommentary: DashboardAICommentary;
}

export interface TrendDataPoint {
  period: string;
  value: number;
  ratioCode: string;
}

export interface DashboardTrendResponse {
  periodCount: number;
  trends: TrendDataPoint[];
}

