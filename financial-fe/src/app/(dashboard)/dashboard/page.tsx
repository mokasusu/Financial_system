"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useCompanyStore } from "@/stores/company-store";
import { useStatementStore } from "@/stores/statement-store";
import { dashboardService } from "@/features/dashboard/services";
import { statementService } from "@/features/statements/services";
import { GlobalContextHeader } from "@/features/dashboard/components/global-context-header";
import { CoreKpiScoreboard } from "@/features/dashboard/components/core-kpi-scoreboard";
import { VisualAnalytics } from "@/features/dashboard/components/visual-analytics";
import { RiskHeatmapAndValidation } from "@/features/dashboard/components/risk-heatmap-and-validation";
import { AiExecutiveCommentary } from "@/features/dashboard/components/ai-executive-commentary";
import { ActionBar } from "@/features/dashboard/components/action-bar";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import {
  FileUp,
  UploadCloud,
  FileText,
  Loader2,
  AlertCircle,
  Eye,
  CheckCircle2,
  ArrowRight,
} from "lucide-react";
import type { DashboardContext } from "@/features/dashboard/types";

export default function DashboardPage() {
  const router = useRouter();
  const currentCompany = useCompanyStore((state) => state.currentCompany);

  const statements = useStatementStore((state) => state.statements);
  const activeStatementId = useStatementStore((state) => state.activeStatementId);
  const activeStatement = useStatementStore((state) => state.activeStatement);

  const [isMultiYear, setIsMultiYear] = useState(false);

  // Upload state for Zero-data state
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  // Fetch Dashboard Overview for the active statement
  const {
    data: overview,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ["dashboard-overview", activeStatementId, currentCompany?.id],
    queryFn: () =>
      dashboardService.getOverview(
        undefined,
        activeStatementId || undefined
      ),
    enabled: Boolean(currentCompany?.id),
    staleTime: 60 * 1000,
    retry: 1,
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.size > 50 * 1024 * 1024) {
        setUploadError("Dung lượng file vượt quá 50MB.");
        return;
      }
      setUploadFile(file);
      setUploadError(null);
    }
  };

  const handleDirectUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadFile) {
      setUploadError("Vui lòng chọn tệp Báo cáo Tài chính dạng PDF hoặc hình ảnh scan.");
      return;
    }

    try {
      setIsUploading(true);
      setUploadError(null);

      const res = await statementService.upload(uploadFile, "current", true, true);
      // Route directly to Step 2 (OCR Processing)
      router.push(`/statements/${res.id}/analysis?step=2`);
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Tải lên tệp BCTC thất bại. Vui lòng kiểm tra định dạng file.";
      setUploadError(message);
    } finally {
      setIsUploading(false);
    }
  };

  // 1. Loading Skeleton matching exact 6-block layout
  if (isLoading) {
    return (
      <div className="space-y-4 max-w-7xl mx-auto">
        <Skeleton className="h-28 w-full rounded-xl bg-slate-200" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Skeleton className="h-28 rounded-xl bg-slate-200" />
          <Skeleton className="h-28 rounded-xl bg-slate-200" />
          <Skeleton className="h-28 rounded-xl bg-slate-200" />
          <Skeleton className="h-28 rounded-xl bg-slate-200" />
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <Skeleton className="lg:col-span-2 h-72 rounded-xl bg-slate-200" />
          <Skeleton className="h-72 rounded-xl bg-slate-200" />
        </div>
        <Skeleton className="h-60 w-full rounded-xl bg-slate-200" />
        <Skeleton className="h-32 w-full rounded-xl bg-slate-200" />
      </div>
    );
  }

  // 2. Zero-data Empty State when user has NO statements uploaded yet
  const hasStatement =
    statements.length > 0 ||
    Boolean(overview?.context?.financialStatementId) ||
    Boolean(activeStatementId);

  if (!hasStatement) {
    return (
      <div className="max-w-4xl mx-auto py-8 space-y-6">
        <Card className="bg-white border border-slate-200 shadow-sm overflow-hidden">
          <div className="bg-[#006c49] text-white p-6 text-center space-y-2">
            <h1 className="text-xl font-bold tracking-tight">
              {currentCompany?.companyName || "DOANH NGHIỆP CỦA BẠN"}
            </h1>
            <p className="text-xs text-emerald-100 max-w-xl mx-auto">
              Chưa có phiên báo cáo tài chính nào được tải lên và phân tích. Hãy tải lên tệp BCTC đầu tiên để tự động bóc tách OCR, thẩm định 14 chỉ số và nhận đánh giá chuyên sâu từ AI.
            </p>
          </div>

          <CardContent className="p-8 space-y-6">
            {uploadError && (
              <div className="p-3.5 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700">
                <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
                <div className="flex-1 font-medium">{uploadError}</div>
              </div>
            )}

            <form onSubmit={handleDirectUpload} className="space-y-6">
              {/* Drag & Drop Area */}
              <div className="border-2 border-dashed border-slate-300 hover:border-[#006c49] rounded-2xl p-10 text-center transition-colors bg-slate-50/60">
                <input
                  type="file"
                  id="dashboard-dropzone-input"
                  className="hidden"
                  accept=".pdf,.png,.jpg,.jpeg,.tiff,.bmp"
                  onChange={handleFileChange}
                  disabled={isUploading}
                />
                <label
                  htmlFor="dashboard-dropzone-input"
                  className="cursor-pointer flex flex-col items-center justify-center space-y-3"
                >
                  <div className="h-16 w-16 rounded-2xl bg-[#006c49]/10 text-[#006c49] flex items-center justify-center border border-[#006c49]/20 shadow-xs">
                    <UploadCloud className="h-8 w-8" />
                  </div>

                  {uploadFile ? (
                    <div className="space-y-1">
                      <div className="text-sm font-bold text-slate-900 flex items-center justify-center gap-1.5">
                        <FileText className="h-4 w-4 text-[#006c49]" />
                        {uploadFile.name}
                      </div>
                      <p className="text-xs text-slate-500">
                        {(uploadFile.size / (1024 * 1024)).toFixed(2)} MB • Nhấp để đổi file khác
                      </p>
                    </div>
                  ) : (
                    <div className="space-y-1">
                      <p className="text-sm font-semibold text-slate-800">
                        Kéo thả file BCTC vào đây hoặc{" "}
                        <span className="text-[#006c49] underline underline-offset-2">chọn từ máy tính</span>
                      </p>
                      <p className="text-xs text-slate-400">
                        Hỗ trợ tệp PDF hoặc ảnh scan (PNG, JPG, TIFF) dung lượng tối đa 50MB
                      </p>
                    </div>
                  )}
                </label>
              </div>

              <div className="flex items-center justify-end gap-4 pt-2">
                <Button
                  type="submit"
                  disabled={!uploadFile || isUploading}
                  className="bg-[#006c49] hover:bg-[#005237] text-white font-semibold text-xs px-6 py-2.5 h-auto shadow-xs"
                >
                  {isUploading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Đang tải lên & gửi xử lý OCR...
                    </>
                  ) : (
                    <>
                      Tải lên & Khởi động Phân tích OCR
                      <ArrowRight className="h-4 w-4 ml-2" />
                    </>
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    );
  }

  // 3. Normal Dashboard View (With selected statement)
  const contextData: DashboardContext = {
    companyId: overview?.context?.companyId || currentCompany?.id || 1,
    companyName:
      overview?.context?.companyName ||
      currentCompany?.companyName ||
      "Doanh nghiệp",
    taxCode: overview?.context?.taxCode || currentCompany?.taxCode || "—",
    accountingStandard:
      overview?.context?.accountingStandard ||
      activeStatement?.accountingStandard ||
      "TT 200/2014/TT-BTC",
    selectedPeriod:
      overview?.context?.selectedPeriod ||
      (activeStatement?.asOfDate ? String(activeStatement.asOfDate) : null) ||
      (activeStatement?.fiscalYear ? `Năm ${activeStatement.fiscalYear}` : null) ||
      "31/12/2025",
    currencyUnit: overview?.context?.currencyUnit || activeStatement?.unit || "VND",
    hasCompany: overview?.context?.hasCompany ?? Boolean(currentCompany),
    hasFinancialStatement: true,
    dataStatus: overview?.context?.dataStatus || "READY",
    financialStatementId:
      overview?.context?.financialStatementId || activeStatementId || undefined,
    fileName:
      activeStatement?.fileName ||
      `BCTC_${currentCompany?.taxCode || "DoanhNghiep"}_${overview?.context?.selectedPeriod || "2025"}.pdf`,
  };

  const overallScoreNum = overview?.overallScore !== null && overview?.overallScore !== undefined
    ? Number(overview.overallScore)
    : undefined;
  const healthClassText = overview?.healthClassificationDisplayName || overview?.healthClassification || "CHƯA ĐÁNH GIÁ";

  return (
    <div className="space-y-4 max-w-7xl mx-auto pb-10">
      {/* Khối 1: Header Ngữ cảnh Toàn cục */}
      <GlobalContextHeader
        context={contextData}
        overallScore={overallScoreNum}
        healthClassification={healthClassText}
        isMultiYear={isMultiYear}
        onToggleMultiYear={() => setIsMultiYear(!isMultiYear)}
      />

      {/* Khối 2: 4 Thẻ Core KPI Sống còn */}
      <CoreKpiScoreboard kpis={overview?.coreKpis || []} />

      {/* Khối 3: Phân tích Đồ họa Trực quan (Cột Đôi 8 Tỷ số & Donut Cơ cấu Vốn) */}
      <VisualAnalytics
        data={overview?.visualAnalytics || { ratioComparisons: [] }}
        companyShortName={currentCompany?.shortName || currentCompany?.companyName || "Doanh nghiệp"}
      />

      {/* Khối 3C & 4: Ma trận Rủi ro 14 Tỷ số (với Tên chỉ số dưới số liệu) & Kiểm định 7 Đẳng thức TT200 */}
      <RiskHeatmapAndValidation
        validation={overview?.validation}
        financialRatios={overview?.financialRatios}
      />

      {/* Khối 5: Nhận định Chuyên sâu từ AI */}
      <AiExecutiveCommentary
        commentary={overview?.aiCommentary}
        statementId={contextData.financialStatementId}
      />

      {/* Khối 6: Thanh Tác vụ Xuất Báo cáo */}
      <ActionBar
        overview={overview}
        statementId={contextData.financialStatementId}
      />
    </div>
  );
}
