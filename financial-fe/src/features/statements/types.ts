export type StatementStatus =
  | "QUEUED"
  | "PROCESSING"
  | "OCR_PROCESSING"
  | "COMPLETED"
  | "REVIEWING_DATA"
  | "DATA_CONFIRMED"
  | "CANCEL_REQUESTED"
  | "FAILED"
  | "CANCELLED";

export interface FinancialStatement {
  id: number;
  companyId: number;
  companyName?: string;
  fileName?: string;
  originalFileName?: string;
  fileUrl?: string;
  fileSize?: number;
  contentType?: string;
  accountingStandard?: string;
  fiscalYear?: number;
  period: string; // e.g. "2024", "2025", "current"
  asOfDate?: string;
  currentPeriodLabel?: string;
  previousPeriodLabel?: string;
  unit?: string;
  status?: StatementStatus;
  processingStatus?: string;
  statusDisplayName?: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface StatementItem {
  id: number;
  statementId?: number;
  statementType?: string;
  statementTypeDisplayName?: string;
  accountingStandard?: string;
  code: string; // e.g. "100", "270", "300", "400"
  key?: string | null;
  label: string;
  rawLabel?: string;
  current?: number | null;
  previous?: number | null;
  currentValue?: number;
  previousValue?: number;
  originalCurrentValue?: number;
  originalPreviousValue?: number;
  unit?: string;
  sourcePage?: number;
  confidence?: number;
  confidenceScore?: number;
  mappingStatus?: string;
  mappingStatusDisplayName?: string;
  edited?: boolean;
  editedAt?: string | null;
  note?: string;
  draftCurrentValue?: number;
  draftPreviousValue?: number;
  isDraftModified?: boolean;
}

export interface StatementUploadResponse {
  id: number;
  status: StatementStatus;
  period: string;
  fileName: string;
}

export interface StatementStatusResponse {
  id: number;
  status: StatementStatus;
  errorMessage?: string;
  processingProgress?: number;
}

