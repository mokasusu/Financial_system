"use client";

import { useEffect, useState, use } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useStatementStore } from "@/stores/statement-store";
import {
  statementService,
  pipelineService,
} from "@/features/statements/services";
import { dashboardService } from "@/features/dashboard/services";
import { PipelineStepper } from "@/features/statements/components/pipeline-stepper";
import type { StatementItem, StatementStatus } from "@/features/statements/types";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Upload,
  Cpu,
  Edit,
  Calculator,
  Sparkles,
  FileCheck,
  Save,
  CheckCircle2,
  Clock,
  ArrowRight,
  ArrowLeft,
  Loader2,
  AlertCircle,
  FileText,
  RotateCw,
  Search,
  X,
  ExternalLink,
  Trash2,
  AlertTriangle,
  Edit3,
  Check,
  RotateCcw,
} from "lucide-react";
import { useAuthStore } from "@/stores/auth-store";

interface AnalysisPageProps {
  params: Promise<{ id: string }>;
}

export default function StatementAnalysisPipelinePage({ params }: AnalysisPageProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const resolvedParams = use(params);
  const statementId = parseInt(resolvedParams.id, 10);

  const stepFromQuery = parseInt(searchParams.get("step") || "2", 10);
  const [currentStep, setCurrentStep] = useState<number>(stepFromQuery || 2);

  // Synchronize URL query param with step
  const goToStep = (step: number) => {
    setCurrentStep(step);
    router.push(`/statements/${statementId}/analysis?step=${step}`);
  };

  // 1. Query Statement Metadata
  const { data: statement, refetch: refetchStatement } = useQuery({
    queryKey: ["statement-meta", statementId],
    queryFn: () => statementService.getStatementById(statementId),
    enabled: Boolean(statementId),
  });

  const token = useAuthStore((state) => state.accessToken);
  const apiBaseUrl = (process.env.NEXT_PUBLIC_API_URL?.trim() || "").replace(/\/+$/, "").replace(/\/api$/, "");

  const filePreviewUrl = statement?.fileUrl
    ? (statement.fileUrl.startsWith("http")
        ? `${statement.fileUrl}${token ? `?token=${encodeURIComponent(token)}` : ""}`
        : `${apiBaseUrl}${statement.fileUrl.startsWith("/") ? "" : "/"}${statement.fileUrl}${token ? `?token=${encodeURIComponent(token)}` : ""}`)
    : statementId
    ? `${apiBaseUrl}/api/financial-statements/${statementId}/file${token ? `?token=${encodeURIComponent(token)}` : ""}`
    : null;

  const currentPeriodText =
    statement?.currentPeriodLabel ||
    (statement?.asOfDate
      ? new Date(statement.asOfDate).toLocaleDateString("vi-VN")
      : "Kỳ này");

  const previousPeriodText =
    statement?.previousPeriodLabel ||
    (statement?.fiscalYear ? `01/01/${statement.fiscalYear}` : "Kỳ trước");

  // 2. Query Statement Items for Step 3
  const {
    data: items = [],
    isLoading: isItemsLoading,
    refetch: refetchItems,
  } = useQuery({
    queryKey: ["statement-items", statementId],
    queryFn: () => statementService.getItems(statementId),
    enabled: Boolean(statementId) && (currentStep === 3 || currentStep === 4),
  });

  // Local state for editing draft items in Step 3
  const [editableItems, setEditableItems] = useState<StatementItem[]>([]);
  const [isSavingDraft, setIsSavingDraft] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);
  const [stepError, setStepError] = useState<string | null>(null);
  const [filterCategory, setFilterCategory] = useState<
    "ALL" | "BALANCE_SHEET" | "INCOME_STATEMENT" | "CASH_FLOW_STATEMENT" | "NEEDS_REVIEW"
  >("ALL");
  const [itemSearchQuery, setItemSearchQuery] = useState("");

  // Step 2: Query Real Status with Polling (every 2.5s)
  const [isRetryingOcr, setIsRetryingOcr] = useState(false);
  const { data: statusData, refetch: refetchStatus } = useQuery({
    queryKey: ["statement-status", statementId],
    queryFn: () => statementService.getStatus(statementId),
    enabled: Boolean(statementId) && currentStep === 2,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (
        status === "REVIEWING_DATA" ||
        status === "COMPLETED" ||
        status === "FAILED" ||
        status === "DATA_CONFIRMED"
      ) {
        return false;
      }
      return 2500;
    },
  });

  // Automatically transition to Step 3 when OCR completes
  useEffect(() => {
    if (currentStep === 2 && statusData?.status) {
      if (
        statusData.status === "REVIEWING_DATA" ||
        statusData.status === "COMPLETED"
      ) {
        const timer = setTimeout(() => {
          goToStep(3);
        }, 600);
        return () => clearTimeout(timer);
      }
    }
  }, [currentStep, statusData?.status]);

  const handleRetryExtract = async () => {
    try {
      setIsRetryingOcr(true);
      setStepError(null);
      await statementService.extract(statementId);
      refetchStatus();
    } catch (err: unknown) {
      const msg =
        (err as { message?: string })?.message ||
        "Kích hoạt bóc tách OCR thất bại. Vui lòng kiểm tra lại dịch vụ OCR.";
      setStepError(msg);
    } finally {
      setIsRetryingOcr(false);
    }
  };

  // Step 4-6: Query Overview data (ratios, validation, AI commentary)
  const { data: analysisOverview, refetch: refetchOverview } = useQuery({
    queryKey: ["analysis-overview", statementId],
    queryFn: () => dashboardService.getOverview(undefined, statementId),
    enabled: Boolean(statementId) && currentStep >= 4,
    retry: 1,
  });

  useEffect(() => {
    if (items.length > 0) {
      setEditableItems(
        items.map((i) => ({
          ...i,
          currentValue: i.currentValue ?? i.current ?? 0,
          previousValue: i.previousValue ?? i.previous ?? 0,
        }))
      );
    }
  }, [items]);

  // Handle cell edit by item ID (safe with filtering/sorting)
  const handleItemValueChange = (
    id: number,
    field: "currentValue" | "previousValue",
    val: string
  ) => {
    // Keep numbers and negative sign, allow typing empty string
    const cleanStr = val.replace(/[^\d.-]/g, "");
    setEditableItems((prev) =>
      prev.map((item) =>
        item.id === id
          ? {
              ...item,
              [field]: cleanStr === "" || cleanStr === "-" ? cleanStr : (parseFloat(cleanStr) || 0),
              isDraftModified: true,
            }
          : item
      )
    );
  };

  const bsCount = editableItems.filter((i) => i.statementType === "BALANCE_SHEET").length;
  const isCount = editableItems.filter((i) => i.statementType === "INCOME_STATEMENT").length;
  const cfCount = editableItems.filter((i) => i.statementType === "CASH_FLOW_STATEMENT").length;
  const reviewCount = editableItems.filter((i) => i.mappingStatus !== "MATCHED").length;
  const matchedCount = editableItems.filter((i) => i.mappingStatus === "MATCHED").length;

  const filteredItems = editableItems.filter((item) => {
    if (filterCategory === "NEEDS_REVIEW") {
      if (item.mappingStatus === "MATCHED") return false;
    } else if (filterCategory !== "ALL") {
      if (item.statementType !== filterCategory) return false;
    }

    if (itemSearchQuery.trim()) {
      const q = itemSearchQuery.toLowerCase().trim();
      const matchCode = item.code?.toLowerCase().includes(q);
      const matchLabel = item.label?.toLowerCase().includes(q);
      const matchRawLabel = item.rawLabel?.toLowerCase().includes(q);
      const matchKey = item.key?.toLowerCase().includes(q);
      if (!matchCode && !matchLabel && !matchRawLabel && !matchKey) {
        return false;
      }
    }
    return true;
  });

  // Step 3: Confirm statement data
  const handleConfirmStatement = async () => {
    try {
      setIsConfirming(true);
      setStepError(null);

      // Save any modified draft items
      const modified = editableItems
        .filter((i) => i.isDraftModified)
        .map((i) => ({
          ...i,
          currentValue: typeof i.currentValue === "string" ? parseFloat(i.currentValue) || 0 : (i.currentValue ?? 0),
          previousValue: typeof i.previousValue === "string" ? parseFloat(i.previousValue) || 0 : (i.previousValue ?? 0),
        }));
      if (modified.length > 0) {
        await statementService.updateItems(statementId, modified);
      }

      // Call backend confirm endpoint (locks state to DATA_CONFIRMED)
      await statementService.confirm(statementId);
      refetchStatement();

      // Proceed to Step 4 (Calculation & Validation)
      goToStep(4);
    } catch (err: unknown) {
      const msg =
        (err as { message?: string })?.message ||
        "Không thể xác nhận chốt số liệu. Vui lòng kiểm tra lại.";
      setStepError(msg);
    } finally {
      setIsConfirming(false);
    }
  };

  const queryClient = useQueryClient();
  const statements = useStatementStore((state) => state.statements);
  const setStatements = useStatementStore((state) => state.setStatements);
  const setActiveStatementId = useStatementStore((state) => state.setActiveStatementId);

  // Delete session modal state
  const [showDeleteSessionModal, setShowDeleteSessionModal] = useState(false);
  const [isDeletingSession, setIsDeletingSession] = useState(false);
  const [deleteSessionError, setDeleteSessionError] = useState<string | null>(null);

  const handleDeleteSession = async () => {
    try {
      setIsDeletingSession(true);
      setDeleteSessionError(null);

      await statementService.deleteStatement(statementId);

      await queryClient.invalidateQueries({ queryKey: ["financial-statements"] });
      await queryClient.invalidateQueries({ queryKey: ["financial-statements-list"] });
      await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });

      const remaining = statements.filter((s) => s.id !== statementId);
      setStatements(remaining);
      if (remaining.length > 0) {
        setActiveStatementId(remaining[0].id);
      } else {
        setActiveStatementId(null);
      }

      setShowDeleteSessionModal(false);
      router.push("/statements");
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Xóa phiên báo cáo thất bại.";
      setDeleteSessionError(msg);
    } finally {
      setIsDeletingSession(false);
    }
  };

  // Step 4: Trigger Validation & Calculation
  const [isCalculating, setIsCalculating] = useState(false);
  const handleRunCalculations = async () => {
    try {
      setIsCalculating(true);
      setStepError(null);

      await pipelineService.triggerValidation(statementId);
      await pipelineService.triggerCalculation(statementId);
      try {
        await pipelineService.triggerAssessment(statementId);
        await pipelineService.triggerRiskAnalysis(statementId);
        // Automatically generate AI commentary
        await pipelineService.generateAIByStatement(statementId);
      } catch (subErr) {
        console.warn("Lưu ý: Một số chỉ tiêu chưa đủ để hoàn tất chấm điểm toàn diện", subErr);
      }
      await refetchOverview();

      // Proceed to Step 5 (Assessment & AI Review)
      goToStep(5);
    } catch (err: unknown) {
      const msg =
        (err as { message?: string })?.message ||
        "Tính toán thất bại. BCTC có thể chưa được chốt số liệu.";
      setStepError(msg);
    } finally {
      setIsCalculating(false);
    }
  };

  // Step 5 & 6: AI Generation and Direct Editing
  const [isRegeneratingAI, setIsRegeneratingAI] = useState(false);
  const [isEditingComment, setIsEditingComment] = useState(false);
  const [directCommentText, setDirectCommentText] = useState("");
  const [directCommentSaved, setDirectCommentSaved] = useState(false);
  const [finalComment, setFinalComment] = useState("");
  const [isSavingComment, setIsSavingComment] = useState(false);

  const handleRegenerateAI = async () => {
    try {
      setIsRegeneratingAI(true);
      setStepError(null);

      await pipelineService.generateAIByStatement(statementId);
      await refetchOverview();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Tạo nhận định AI thất bại.";
      setStepError(msg);
    } finally {
      setIsRegeneratingAI(false);
    }
  };

  const handleSaveDirectComment = async () => {
    if (!directCommentText.trim()) return;
    try {
      setIsSavingComment(true);
      setStepError(null);

      await pipelineService.updateAIFinalCommentByStatement(statementId, directCommentText.trim());
      await refetchOverview();
      setIsEditingComment(false);
      setDirectCommentSaved(true);
      setTimeout(() => setDirectCommentSaved(false), 3000);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Lưu nhận xét thất bại.";
      setStepError(msg);
    } finally {
      setIsSavingComment(false);
    }
  };

  const handleSaveFinalComment = async () => {
    try {
      setIsSavingComment(true);
      if (finalComment.trim()) {
        await pipelineService.updateAIFinalCommentByStatement(
          statementId,
          finalComment.trim()
        );
        await refetchOverview();
      }
      goToStep(7);
    } catch {
      goToStep(7);
    } finally {
      setIsSavingComment(false);
    }
  };

  useEffect(() => {
    if (analysisOverview?.aiCommentary?.finalComment) {
      setFinalComment(analysisOverview.aiCommentary.finalComment);
      setDirectCommentText(analysisOverview.aiCommentary.finalComment);
    } else if (analysisOverview?.aiCommentary?.executiveSummary) {
      setFinalComment(analysisOverview.aiCommentary.executiveSummary);
      setDirectCommentText(analysisOverview.aiCommentary.executiveSummary);
    }
  }, [analysisOverview]);

  // Step 7: Final Approval
  const [isApproving, setIsApproving] = useState(false);
  const handleApprove = async () => {
    try {
      setIsApproving(true);
      // Route back to dashboard upon approval
      router.push("/dashboard");
    } finally {
      setIsApproving(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto space-y-6 pb-12">
      {/* Thanh thông tin phiên BCTC & Nút Xóa phiên báo cáo */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-white p-3.5 px-4 rounded-xl border border-slate-200 shadow-2xs">
        <div className="flex items-center gap-2.5 min-w-0">
          <FileText className="h-4 w-4 text-[#006c49] shrink-0" />
          <div className="flex items-center gap-2 min-w-0 flex-wrap">
            <span className="font-bold text-xs text-slate-800 font-mono truncate max-w-xs sm:max-w-md">
              {statement?.fileName || `BCTC #${statementId}`}
            </span>
            <Badge variant="outline" className="text-[10px] font-semibold text-slate-600 bg-slate-50 border-slate-200">
              {statement?.accountingStandard || "Chuẩn mực Kế toán"}
            </Badge>
            <span className="text-[11px] text-slate-400">
              Kỳ: {currentPeriodText}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              setDeleteSessionError(null);
              setShowDeleteSessionModal(true);
            }}
            className="h-7 text-xs font-semibold text-rose-600 border-rose-200 bg-white hover:bg-rose-50 hover:text-rose-700 hover:border-rose-300 gap-1.5 shadow-2xs"
            title="Xóa vĩnh viễn phiên báo cáo này và toàn bộ dữ liệu phân tích liên quan"
          >
            <Trash2 className="h-3.5 w-3.5 text-rose-500" />
            Xóa phiên báo cáo này
          </Button>
        </div>
      </div>

      {/* 1. Thanh tiến trình 7 bước dính sát dưới Header, bước sau làm mờ */}
      <PipelineStepper currentStep={currentStep} onStepClick={goToStep} />

      {stepError && (
        <div className="p-3.5 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 animate-in fade-in">
          <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
          <div className="flex-1 font-medium">{stepError}</div>
        </div>
      )}

      {/* 2. Step 2 Content: Bóc tách OCR Polling & Auto-Transition */}
      {currentStep === 2 && (
        <Card className="bg-white p-12 text-center max-w-xl mx-auto shadow-xs">
          <div className="space-y-6 flex flex-col items-center">
            <div className="relative">
              <div className={`h-20 w-20 rounded-full flex items-center justify-center border-2 ${
                statusData?.status === "FAILED"
                  ? "bg-rose-50 text-rose-600 border-rose-200"
                  : "bg-emerald-50 text-[#006c49] border-emerald-200 animate-pulse"
              }`}>
                {statusData?.status === "FAILED" ? (
                  <AlertCircle className="h-10 w-10" />
                ) : (
                  <Cpu className="h-10 w-10" />
                )}
              </div>
              {statusData?.status !== "FAILED" && (
                <span className="absolute top-0 right-0 h-4 w-4 rounded-full bg-[#006c49] animate-ping" />
              )}
            </div>

            <div className="space-y-2">
              <h3 className="text-lg font-bold text-slate-900">
                {statusData?.status === "FAILED"
                  ? "Bóc tách OCR không thành công"
                  : statusData?.status === "REVIEWING_DATA" || statusData?.status === "COMPLETED"
                  ? "Bóc tách OCR thành công! Đang chuyển tiếp..."
                  : "Đang Bóc tách Dữ liệu BCTC qua OCR"}
              </h3>
              <p className="text-xs text-slate-500 max-w-sm">
                {statusData?.status === "FAILED"
                  ? statusData.errorMessage || "Dịch vụ OCR phản hồi lỗi hoặc tệp scan không thể trích xuất. Vui lòng bấm thử lại."
                  : "Hệ thống đang gọi endpoint /api/v1/extract để phân tích các bảng tài chính. Giao diện sẽ tự động chuyển sang Bước 3 ngay khi hoàn tất."}
              </p>
            </div>

            <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  statusData?.status === "FAILED"
                    ? "bg-rose-500 w-full"
                    : statusData?.status === "REVIEWING_DATA" || statusData?.status === "COMPLETED"
                    ? "bg-[#006c49] w-full"
                    : statusData?.status === "PROCESSING"
                    ? "bg-[#006c49] w-3/4 animate-pulse"
                    : "bg-[#006c49] w-1/3 animate-pulse"
                }`}
              />
            </div>

            <div className="flex items-center gap-2 text-xs text-slate-500">
              {statusData?.status === "FAILED" ? (
                <span className="text-rose-600 font-semibold">Trạng thái: FAILED</span>
              ) : (
                <>
                  <Clock className="h-3.5 w-3.5 animate-spin text-[#006c49]" />
                  <span>
                    Trạng thái: <strong>{statusData?.status || "QUEUED"}</strong> • Đang chờ phản hồi từ /api/v1/extract...
                  </span>
                </>
              )}
            </div>

            {statusData?.status === "FAILED" ? (
              <Button
                onClick={handleRetryExtract}
                disabled={isRetryingOcr}
                className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-semibold px-6"
              >
                {isRetryingOcr ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Đang kết nối lại OCR...
                  </>
                ) : (
                  <>
                    <RotateCw className="h-4 w-4 mr-2" />
                    Thử lại bóc tách OCR
                  </>
                )}
              </Button>
            ) : (
              <Button
                onClick={() => goToStep(3)}
                variant="outline"
                className="text-xs text-slate-600 hover:text-slate-900"
              >
                Chuyển thủ công tới Bước 3: Rà soát
                <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
              </Button>
            )}
          </div>
        </Card>
      )}

      {/* 3. Step 3 Content: Split Screen (Editable Table on Left, Original Document Viewer on Right) */}
      {currentStep === 3 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Left Column: Editable Table & Confirm Action */}
          <Card className="bg-white flex flex-col h-[750px] shadow-xs">
            <CardHeader className="py-3 px-4 border-b border-slate-100 flex flex-col gap-2.5">
              <div className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
                    Đối Soát Chỉ Tiêu Kế Toán (Draft Data)
                  </CardTitle>
                  <CardDescription className="text-[11px]">
                    Kiểm tra và chỉnh sửa trực tiếp giá trị trích xuất đối chiếu với tài liệu gốc bên phải
                  </CardDescription>
                </div>
                <div className="flex items-center gap-1.5 flex-wrap justify-end">
                  <span className="text-[10px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                    Tổng: <strong className="text-slate-800">{editableItems.length}</strong>
                  </span>
                  <span className="text-[10px] font-medium text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                    Khớp chuẩn: <strong>{matchedCount}</strong>
                  </span>
                  {reviewCount > 0 && (
                    <span className="text-[10px] font-medium text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
                      Chưa chuẩn hóa: <strong>{reviewCount}</strong>
                    </span>
                  )}
                </div>
              </div>

              {/* Filters & Search */}
              <div className="flex flex-col sm:flex-row gap-2 pt-1 border-t border-slate-100">
                <div className="relative flex-1">
                  <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-slate-400" />
                  <Input
                    placeholder="Tìm theo mã số, tên chỉ tiêu..."
                    value={itemSearchQuery}
                    onChange={(e) => setItemSearchQuery(e.target.value)}
                    className="h-8 pl-8 pr-7 text-xs bg-slate-50/70"
                  />
                  {itemSearchQuery && (
                    <button
                      type="button"
                      onClick={() => setItemSearchQuery("")}
                      className="absolute right-2 top-2 text-slate-400 hover:text-slate-600"
                    >
                      <X className="h-3.5 w-3.5" />
                    </button>
                  )}
                </div>
                <div className="flex items-center gap-1 overflow-x-auto pb-0.5 scrollbar-thin">
                  <button
                    type="button"
                    onClick={() => setFilterCategory("ALL")}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors whitespace-nowrap ${
                      filterCategory === "ALL"
                        ? "bg-[#006c49] text-white"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    Tất cả ({editableItems.length})
                  </button>
                  <button
                    type="button"
                    onClick={() => setFilterCategory("BALANCE_SHEET")}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors whitespace-nowrap ${
                      filterCategory === "BALANCE_SHEET"
                        ? "bg-[#006c49] text-white"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    Bảng CĐKT ({bsCount})
                  </button>
                  <button
                    type="button"
                    onClick={() => setFilterCategory("INCOME_STATEMENT")}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors whitespace-nowrap ${
                      filterCategory === "INCOME_STATEMENT"
                        ? "bg-[#006c49] text-white"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    Kết quả KD ({isCount})
                  </button>
                  <button
                    type="button"
                    onClick={() => setFilterCategory("CASH_FLOW_STATEMENT")}
                    className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors whitespace-nowrap ${
                      filterCategory === "CASH_FLOW_STATEMENT"
                        ? "bg-[#006c49] text-white"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    Lưu chuyển TT ({cfCount})
                  </button>
                  {reviewCount > 0 && (
                    <button
                      type="button"
                      onClick={() => setFilterCategory("NEEDS_REVIEW")}
                      className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors whitespace-nowrap ${
                        filterCategory === "NEEDS_REVIEW"
                          ? "bg-amber-600 text-white"
                          : "bg-amber-50 text-amber-700 hover:bg-amber-100 border border-amber-200"
                      }`}
                    >
                      Cần rà soát ({reviewCount})
                    </button>
                  )}
                </div>
              </div>
            </CardHeader>

            <CardContent className="flex-1 p-0 overflow-auto">
              <table className="w-full text-xs min-w-[620px]">
                <thead className="bg-slate-50 sticky top-0 border-b text-slate-500 font-semibold z-10 shadow-2xs">
                  <tr>
                    <th className="p-2.5 text-left w-14 shrink-0">Mã số</th>
                    <th className="p-2.5 text-left w-44 sm:w-52">Tên chỉ tiêu</th>
                    {/* BỎ CỘT TRẠNG THÁI THEO YÊU CẦU */}
                    <th className="p-2.5 text-right w-56 sm:w-64 min-w-[210px]">
                      <div className="font-semibold text-slate-700 truncate" title={currentPeriodText}>
                        {currentPeriodText}
                      </div>
                      <div className="text-[10px] text-slate-400 font-normal">
                        ({statement?.unit || "VND"})
                      </div>
                    </th>
                    <th className="p-2.5 text-right w-56 sm:w-64 min-w-[210px]">
                      <div className="font-semibold text-slate-700 truncate" title={previousPeriodText}>
                        {previousPeriodText}
                      </div>
                      <div className="text-[10px] text-slate-400 font-normal">
                        ({statement?.unit || "VND"})
                      </div>
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {isItemsLoading ? (
                    <tr>
                      <td colSpan={4} className="p-8 text-center text-slate-400">
                        <Loader2 className="h-6 w-6 animate-spin mx-auto text-[#006c49] mb-2" />
                        Đang tải danh sách chỉ tiêu bóc tách từ OCR...
                      </td>
                    </tr>
                  ) : filteredItems.length > 0 ? (
                    filteredItems.map((item) => (
                      <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                        <td className="p-2.5 font-mono font-bold text-slate-600 w-14 shrink-0">
                          {item.code || "—"}
                        </td>
                        <td className="p-2.5 w-44 sm:w-52">
                          <div className="font-medium text-slate-800 leading-snug">
                            <div className="flex items-center gap-1.5">
                              <span className="line-clamp-2" title={item.label}>
                                {item.label}
                              </span>
                              {item.isDraftModified && (
                                <span
                                  className="inline-block w-1.5 h-1.5 rounded-full bg-amber-500 shrink-0"
                                  title="Đã chỉnh sửa thủ công"
                                />
                              )}
                            </div>
                          </div>
                          {item.rawLabel && item.rawLabel !== item.label && (
                            <div className="text-[10px] text-slate-400 truncate max-w-[190px]" title={item.rawLabel}>
                              OCR: {item.rawLabel}
                            </div>
                          )}
                        </td>
                        {/* BỎ CỘT TRẠNG THÁI - TĂNG KÍCH THƯỚC CỘT VÀ BỎ BUTTON TĂNG GIẢM */}
                        <td className="p-2.5 text-right w-56 sm:w-64 min-w-[210px]">
                          <Input
                            type="text"
                            inputMode="numeric"
                            value={item.currentValue ?? item.current ?? ""}
                            onChange={(e) =>
                              handleItemValueChange(item.id, "currentValue", e.target.value)
                            }
                            className={`h-8 text-right text-xs font-mono font-semibold px-2.5 w-full tracking-tight [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none ${
                              item.isDraftModified ? 'border-amber-300 bg-amber-50/30' : ''
                            }`}
                          />
                        </td>
                        <td className="p-2.5 text-right w-56 sm:w-64 min-w-[210px]">
                          <Input
                            type="text"
                            inputMode="numeric"
                            value={item.previousValue ?? item.previous ?? ""}
                            onChange={(e) =>
                              handleItemValueChange(item.id, "previousValue", e.target.value)
                            }
                            className={`h-8 text-right text-xs font-mono px-2.5 w-full tracking-tight [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none ${
                              item.isDraftModified ? 'border-amber-300 bg-amber-50/30' : ''
                            }`}
                          />
                        </td>
                      </tr>
                    ))
                  ) : editableItems.length > 0 ? (
                    <tr>
                      <td colSpan={4} className="p-8 text-center text-slate-400">
                        <AlertCircle className="h-7 w-7 mx-auto mb-2 text-slate-300" />
                        <p className="font-semibold text-xs text-slate-600">
                          Không tìm thấy chỉ tiêu nào phù hợp với bộ lọc
                        </p>
                        <p className="text-[11px] text-slate-400 mt-1 mb-3">
                          Thử đổi từ khóa tìm kiếm hoặc bấm &quot;Tất cả&quot;.
                        </p>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setFilterCategory("ALL");
                            setItemSearchQuery("");
                          }}
                          className="text-xs h-7"
                        >
                          Xóa bộ lọc
                        </Button>
                      </td>
                    </tr>
                  ) : (
                    <tr>
                      <td colSpan={4} className="p-8 text-center text-slate-400">
                        <AlertCircle className="h-8 w-8 mx-auto mb-2 text-slate-300" />
                        <p className="font-semibold text-xs text-slate-600">
                          Chưa có chỉ tiêu nào được bóc tách từ tài liệu này
                        </p>
                        <p className="text-[11px] text-slate-400 mt-1">
                          Vui lòng bấm &quot;Thử lại OCR&quot; ở Bước 2 để quét lại bảng tài chính.
                        </p>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </CardContent>

            <div className="p-3 border-t border-slate-100 bg-slate-50 flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                onClick={() => goToStep(2)}
                className="text-xs"
              >
                <ArrowLeft className="h-3.5 w-3.5 mr-1" />
                Quay lại Bước 2
              </Button>

              <Button
                onClick={handleConfirmStatement}
                disabled={isConfirming}
                className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-semibold h-8 shadow-xs"
              >
                {isConfirming ? (
                  <>
                    <Loader2 className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                    Đang khóa dữ liệu...
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="h-3.5 w-3.5 mr-1.5" />
                    Xác nhận chốt số liệu (Confirm)
                  </>
                )}
              </Button>
            </div>
          </Card>

          {/* Right Column: Original Uploaded Document Viewer */}
          <Card className="bg-white flex flex-col h-[750px] shadow-xs overflow-hidden">
            <CardHeader className="py-2.5 px-4 border-b border-slate-100 flex flex-row items-center justify-between bg-slate-50/70">
              <div className="flex items-center gap-2 min-w-0">
                <FileText className="h-4 w-4 text-[#006c49] shrink-0" />
                <div className="min-w-0">
                  <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700 truncate">
                    Tài liệu BCTC Gốc (Bản Scan / Upload)
                  </CardTitle>
                  <CardDescription className="text-[11px] truncate text-slate-500">
                    {statement?.fileName || "Tài liệu BCTC"}
                    {statement?.fileSize
                      ? ` • ${(statement.fileSize / (1024 * 1024)).toFixed(2)} MB`
                      : ""}
                    {statement?.accountingStandard
                      ? ` • ${statement.accountingStandard}`
                      : ""}
                  </CardDescription>
                </div>
              </div>

              <div className="flex items-center gap-2 shrink-0">
                {filePreviewUrl && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => window.open(filePreviewUrl, "_blank")}
                    className="text-xs h-7 px-2.5 text-[#006c49] hover:bg-emerald-50 border-emerald-200"
                    title="Mở tài liệu gốc ở tab trình duyệt mới"
                  >
                    <ExternalLink className="h-3.5 w-3.5 mr-1" />
                    Mở tab mới
                  </Button>
                )}
              </div>
            </CardHeader>

            <CardContent className="flex-1 p-0 bg-slate-100 relative overflow-hidden flex flex-col">
              {filePreviewUrl ? (
                <iframe
                  src={filePreviewUrl}
                  className="w-full h-full border-0 bg-white"
                  title="Tài liệu BCTC gốc"
                />
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-slate-400 p-8 text-center space-y-2">
                  <Loader2 className="h-8 w-8 animate-spin text-[#006c49]" />
                  <p className="text-xs font-medium text-slate-600">
                    Đang chuẩn bị hiển thị tài liệu gốc...
                  </p>
                  <p className="text-[11px] text-slate-400 max-w-xs">
                    Tài liệu tải lên sẽ được hiển thị trực tiếp tại đây để đối soát song song cùng bảng chỉ số bên trái.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* 4. Step 4 Content: Calculation & Validation Engine */}
      {currentStep === 4 && (
        <Card className="bg-white p-8 max-w-2xl mx-auto shadow-xs text-center space-y-6">
          <div className="h-16 w-16 rounded-2xl bg-emerald-50 text-[#006c49] flex items-center justify-center mx-auto border border-emerald-100">
            <Calculator className="h-8 w-8" />
          </div>

          <div className="space-y-2">
            <h3 className="text-xl font-bold text-slate-900">
              Kích hoạt Engine Kiểm định & Tính toán 14 Tỷ số
            </h3>
            <p className="text-xs text-slate-500 max-w-md mx-auto">
              Hệ thống sẽ chạy bộ kiểm tra các đẳng thức cân đối kế toán ({statement?.accountingStandard || "TT200 / TT99"}) và tính toán toàn diện 14 chỉ số tài chính thuộc 4 nhóm nghiệp vụ.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-left text-xs space-y-2 max-w-md mx-auto">
            <div className="flex items-center gap-2 text-slate-700">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" />
              <span>Dữ liệu BCTC đã được chốt (Status: DATA_CONFIRMED)</span>
            </div>
            <div className="flex items-center gap-2 text-slate-700">
              <CheckCircle2 className="h-4 w-4 text-emerald-600" />
              <span>Sẵn sàng tính toán thanh khoản, đòn bẩy, sinh lời & hiệu quả</span>
            </div>
          </div>

          <div className="flex items-center justify-center gap-3">
            <Button
              variant="outline"
              onClick={() => goToStep(3)}
              className="text-xs"
            >
              Xem lại số liệu
            </Button>

            <Button
              onClick={handleRunCalculations}
              disabled={isCalculating}
              className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-semibold px-6 text-xs h-9"
            >
              {isCalculating ? (
                <>
                  <Loader2 className="h-3.5 w-3.5 mr-2 animate-spin" />
                  Đang tính toán từ backend...
                </>
              ) : (
                <>
                  Chạy tính toán & Nhận xét AI
                  <ArrowRight className="h-3.5 w-3.5 ml-2" />
                </>
              )}
            </Button>
          </div>
        </Card>
      )}

      {/* 5. Step 5 Content: Nhận xét AI Split-Screen */}
      {currentStep === 5 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900">
              Bước 5: Nhận định Chuyên sâu từ Trợ lý AI
            </h2>
            <Button
              onClick={() => goToStep(6)}
              className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs h-8"
            >
              Chuyển sang Bước 6: Chỉnh sửa nhận định
              <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
            </Button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {/* Left: Summary Ratios */}
            <Card className="bg-white p-5 space-y-4 shadow-xs">
              <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
                Chỉ số Tài chính Đã Tính Toán
              </CardTitle>

              <div className="space-y-2 text-xs">
                {analysisOverview?.coreKpis && analysisOverview.coreKpis.length > 0 ? (
                  analysisOverview.coreKpis.map((kpi) => (
                    <div
                      key={kpi.code}
                      className="p-3 rounded-lg bg-emerald-50 border border-emerald-100 flex justify-between items-center"
                    >
                      <span className="font-semibold text-slate-800">{kpi.name}</span>
                      <span className="font-mono font-bold text-emerald-800">
                        {kpi.value !== null && kpi.value !== undefined
                          ? kpi.unit === "PERCENT" || kpi.unit === "%"
                            ? `${(kpi.value * 100 > 1 ? kpi.value : kpi.value * 100).toFixed(2)}%`
                            : `${Number(kpi.value).toFixed(2)}x`
                          : "—"}{" "}
                        [{kpi.evaluationStatus || "AN TOÀN"}]
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-slate-400">
                    <AlertCircle className="h-6 w-6 mx-auto mb-1.5 text-slate-300" />
                    Chưa có chỉ số nào được tính toán.
                  </div>
                )}
              </div>
            </Card>

            {/* Right: AI Commentary with Regenerate & Direct Editing */}
            <Card className="bg-white p-5 space-y-4 border-2 border-emerald-500/40 shadow-xs">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div className="flex items-center gap-2">
                  <Sparkles className="h-4 w-4 text-[#006c49]" />
                  <CardTitle className="text-xs font-bold uppercase tracking-wider text-[#006c49]">
                    Nhận định AI (OpenRouter Gateway)
                  </CardTitle>
                </div>

                <div className="flex items-center gap-2 flex-wrap">
                  {/* Nút Tạo lại nhận xét AI */}
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={handleRegenerateAI}
                    disabled={isRegeneratingAI}
                    className="h-7 text-xs font-semibold text-[#006c49] border-emerald-300 hover:bg-emerald-50 gap-1"
                    title="Tạo lại nhận định AI từ đầu dựa trên 14 tỷ số đã tính toán"
                  >
                    <RotateCw className={`h-3 w-3 ${isRegeneratingAI ? "animate-spin" : ""}`} />
                    {isRegeneratingAI ? "Đang tạo lại..." : "Tạo lại nhận xét AI"}
                  </Button>

                  {/* Nút Chỉnh sửa trực tiếp */}
                  {!isEditingComment ? (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setIsEditingComment(true)}
                      className="h-7 text-xs font-semibold text-slate-700 border-slate-300 hover:bg-slate-50 gap-1"
                    >
                      <Edit3 className="h-3 w-3 text-[#006c49]" />
                      Chỉnh sửa
                    </Button>
                  ) : (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setIsEditingComment(false)}
                      disabled={isSavingComment}
                      className="h-7 text-xs text-slate-600 gap-1"
                    >
                      <X className="h-3 w-3" />
                      Đóng
                    </Button>
                  )}

                  <Badge variant={analysisOverview?.aiCommentary?.executiveSummary ? "safe" : "outline"}>
                    {analysisOverview?.aiCommentary?.executiveSummary ? "COMPLETED" : "CHƯA KÍCH HOẠT"}
                  </Badge>
                </div>
              </div>

              {directCommentSaved && (
                <div className="p-2 rounded-lg bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 font-medium flex items-center gap-1.5">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                  Đã lưu chỉnh sửa nhận xét của bạn thành công!
                </div>
              )}

              {/* Form Chỉnh sửa trực tiếp */}
              {isEditingComment ? (
                <div className="space-y-3 bg-slate-50/70 p-3.5 rounded-xl border border-slate-200 animate-in fade-in">
                  <div className="flex items-center justify-between">
                    <label className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                      <Edit3 className="h-3.5 w-3.5 text-[#006c49]" />
                      Chỉnh sửa trực tiếp nhận xét:
                    </label>
                    {analysisOverview?.aiCommentary?.executiveSummary && (
                      <button
                        type="button"
                        onClick={() => setDirectCommentText(analysisOverview.aiCommentary?.executiveSummary || "")}
                        className="text-[11px] text-[#006c49] hover:underline flex items-center gap-1"
                        title="Điền lại nhận định ban đầu của AI"
                      >
                        <RotateCcw className="h-3 w-3" />
                        Lấy lại gốc AI
                      </button>
                    )}
                  </div>

                  <textarea
                    rows={6}
                    value={directCommentText}
                    onChange={(e) => setDirectCommentText(e.target.value)}
                    placeholder="Nhập nội dung nhận xét hoặc điều chỉnh nhận định..."
                    className="w-full rounded-lg border border-slate-300 p-3 text-xs leading-relaxed text-slate-900 bg-white focus:outline-none focus:ring-1 focus:ring-[#006c49] font-medium"
                  />

                  <div className="flex items-center justify-between pt-1">
                    <span className="text-[11px] text-slate-400">
                      {directCommentText.length} ký tự
                    </span>

                    <div className="flex items-center gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setIsEditingComment(false)}
                        disabled={isSavingComment}
                        className="h-7 text-xs"
                      >
                        Hủy
                      </Button>

                      <Button
                        size="sm"
                        onClick={handleSaveDirectComment}
                        disabled={isSavingComment || !directCommentText.trim()}
                        className="h-7 text-xs font-semibold bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs"
                      >
                        {isSavingComment ? (
                          <>
                            <Loader2 className="h-3 w-3 mr-1 animate-spin" />
                            Đang lưu...
                          </>
                        ) : (
                          <>
                            <Check className="h-3 w-3 mr-1" />
                            Lưu nhận xét
                          </>
                        )}
                      </Button>
                    </div>
                  </div>
                </div>
              ) : null}

              {/* View Mode */}
              <div className="space-y-3 text-xs text-slate-700 leading-relaxed">
                {analysisOverview?.aiCommentary?.finalComment &&
                analysisOverview.aiCommentary.finalComment !== analysisOverview.aiCommentary.executiveSummary ? (
                  <div className="p-3 rounded-lg bg-emerald-50/80 border border-emerald-200 text-slate-800">
                    <div className="font-bold text-[#006c49] mb-1">• Nhận định đã chỉnh sửa bởi Chuyên viên:</div>
                    <p className="font-medium">{analysisOverview.aiCommentary.finalComment}</p>
                  </div>
                ) : null}

                {analysisOverview?.aiCommentary?.executiveSummary ? (
                  <>
                    <p>
                      <strong>• Tóm tắt điều hành:</strong>{" "}
                      {analysisOverview.aiCommentary.executiveSummary}
                    </p>
                    {analysisOverview.aiCommentary.strengths && analysisOverview.aiCommentary.strengths.length > 0 && (
                      <p>
                        <strong>• Điểm mạnh cốt lõi:</strong>{" "}
                        {analysisOverview.aiCommentary.strengths.map((s) => s.detail).join("; ")}
                      </p>
                    )}
                    {analysisOverview.aiCommentary.recommendations && analysisOverview.aiCommentary.recommendations.length > 0 && (
                      <p>
                        <strong>• Khuyến nghị hành động:</strong>{" "}
                        {analysisOverview.aiCommentary.recommendations.map((r) => r.action).join("; ")}
                      </p>
                    )}

                    {!isEditingComment && (
                      <div className="pt-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setIsEditingComment(true)}
                          className="h-7 text-xs text-slate-700 border-slate-200 hover:bg-slate-50 gap-1.5"
                        >
                          <Edit3 className="h-3 w-3 text-[#006c49]" />
                          Chỉnh sửa trực tiếp nhận xét này
                        </Button>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="py-6 text-center text-slate-400 space-y-3">
                    <Sparkles className="h-7 w-7 text-[#006c49] mx-auto" />
                    <p className="font-semibold text-slate-600 text-xs">
                      Chưa có nhận định AI cho phiên BCTC này
                    </p>
                    <p className="text-[11px] text-slate-400 max-w-xs mx-auto">
                      Bấm nút bên dưới để hệ thống AI tổng hợp 14 tỷ số tài chính và tự động tạo nhận định.
                    </p>
                    <Button
                      size="sm"
                      onClick={handleRegenerateAI}
                      disabled={isRegeneratingAI}
                      className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-semibold h-8 shadow-xs"
                    >
                      <Sparkles className="h-3.5 w-3.5 mr-1.5" />
                      {isRegeneratingAI ? "Đang phân tích..." : "Tạo nhận định AI ngay"}
                    </Button>
                  </div>
                )}
              </div>
            </Card>
          </div>
        </div>
      )}

      {/* 6. Step 6 Content: Chuyên viên chỉnh sửa Final Comment */}
      {currentStep === 6 && (
        <Card className="bg-white p-6 max-w-3xl mx-auto shadow-xs space-y-4">
          <CardHeader className="p-0 pb-3 border-b border-slate-100">
            <CardTitle className="text-base font-bold text-slate-900">
              Bước 6: Tinh chỉnh Nhận định của Chuyên viên Thẩm định
            </CardTitle>
            <CardDescription className="text-xs">
              Nội dung này sẽ được in trực tiếp vào Báo cáo Điều hành chính thức (Executive Summary)
            </CardDescription>
          </CardHeader>

          <CardContent className="p-0 space-y-4 pt-2">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-700">
                Ý kiến kết luận của chuyên viên (Final Comment)
              </label>
              <textarea
                rows={5}
                value={finalComment}
                onChange={(e) => setFinalComment(e.target.value)}
                className="w-full rounded-lg border border-slate-200 p-3 text-xs leading-relaxed text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#006c49]"
              />
            </div>

            <div className="flex items-center justify-between pt-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => goToStep(5)}
                className="text-xs"
              >
                Quay lại Bước 5
              </Button>

              <Button
                onClick={handleSaveFinalComment}
                disabled={isSavingComment}
                className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-semibold h-8"
              >
                {isSavingComment ? (
                  <>
                    <Loader2 className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                    Đang lưu nhận định...
                  </>
                ) : (
                  <>
                    Lưu & Chuyển sang Phê duyệt
                    <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* 7. Step 7 Content: Lưu & Phê duyệt Báo cáo */}
      {currentStep === 7 && (
        <Card className="bg-white p-8 max-w-xl mx-auto shadow-xs text-center space-y-6">
          <div className="h-16 w-16 rounded-full bg-emerald-50 text-[#006c49] flex items-center justify-center mx-auto border border-emerald-200">
            <CheckCircle2 className="h-9 w-9" />
          </div>

          <div className="space-y-2">
            <h3 className="text-xl font-bold text-slate-900">
              Phê duyệt & Hoàn tất Hồ sơ Thẩm định
            </h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Hồ sơ BCTC sẽ được khóa bảo mật, lưu vào lịch sử thẩm định và hiển thị đồng bộ trên Dashboard tổng quan.
            </p>
          </div>

          <div className="flex items-center justify-center gap-3">
            <Button
              variant="outline"
              onClick={() => goToStep(6)}
              className="text-xs"
            >
              Chỉnh sửa thêm
            </Button>

            <Button
              onClick={handleApprove}
              disabled={isApproving}
              className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-semibold px-6 text-xs h-9"
            >
              {isApproving ? (
                <>
                  <Loader2 className="h-3.5 w-3.5 mr-2 animate-spin" />
                  Đang phê duyệt...
                </>
              ) : (
                <>
                  Phê duyệt & Vào Dashboard Tổng quan
                  <ArrowRight className="h-3.5 w-3.5 ml-2" />
                </>
              )}
            </Button>
          </div>
        </Card>
      )}

      {/* Modal Xác Nhận Xóa Phiên Báo Cáo */}
      {showDeleteSessionModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-start justify-between">
              <div className="h-11 w-11 rounded-xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-600 shrink-0">
                <AlertTriangle className="h-6 w-6" />
              </div>
              <button
                type="button"
                onClick={() => !isDeletingSession && setShowDeleteSessionModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-1.5">
              <h3 className="text-base font-bold text-slate-900">
                Xác nhận xóa phiên báo cáo tài chính
              </h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Bạn có chắc chắn muốn xóa phiên báo cáo này khỏi hệ thống? Thao tác này sẽ xóa vĩnh viễn tệp scan/upload, toàn bộ 25 chỉ tiêu OCR, 14 tỷ số kiểm định, hồ sơ thẩm định và mọi nhận định AI liên quan.
              </p>
            </div>

            <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 text-xs space-y-1.5 text-slate-600">
              <div className="flex justify-between items-center">
                <span className="text-slate-400">Tệp tài liệu:</span>
                <span className="font-semibold text-slate-800 font-mono truncate max-w-[220px]" title={statement?.fileName}>
                  {statement?.fileName || `#${statementId}`}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400">Kỳ BCTC:</span>
                <span className="font-semibold text-slate-800">
                  {currentPeriodText}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-400">Mã phiên ID:</span>
                <span className="font-mono text-slate-600">
                  #{statementId}
                </span>
              </div>
            </div>

            {deleteSessionError && (
              <div className="p-2.5 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {deleteSessionError}
              </div>
            )}

            <div className="flex items-center justify-end gap-2.5 pt-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowDeleteSessionModal(false)}
                disabled={isDeletingSession}
                className="text-xs h-9 px-4"
              >
                Hủy bỏ
              </Button>
              <Button
                size="sm"
                onClick={handleDeleteSession}
                disabled={isDeletingSession}
                className="bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs h-9 px-4 shadow-xs"
              >
                {isDeletingSession ? (
                  <>
                    <Loader2 className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                    Đang xóa dữ liệu...
                  </>
                ) : (
                  <>
                    <Trash2 className="h-3.5 w-3.5 mr-1.5" />
                    Xác nhận xóa vĩnh viễn
                  </>
                )}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

