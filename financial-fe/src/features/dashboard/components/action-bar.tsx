"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useStatementStore } from "@/stores/statement-store";
import { statementService } from "@/features/statements/services";
import {
  Edit3,
  Download,
  FileSpreadsheet,
  Printer,
  Trash2,
  AlertTriangle,
  Loader2,
  X,
  FileText,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import type { DashboardOverview } from "../types";

interface ActionBarProps {
  overview?: DashboardOverview | null;
  statementId?: number;
}

export function ActionBar({ overview, statementId }: ActionBarProps) {
  const router = useRouter();
  const queryClient = useQueryClient();

  const statements = useStatementStore((state) => state.statements);
  const setStatements = useStatementStore((state) => state.setStatements);
  const setActiveStatementId = useStatementStore((state) => state.setActiveStatementId);
  const activeStatement = useStatementStore((state) => state.activeStatement);

  const [isDeleting, setIsDeleting] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const targetStatementId = statementId || overview?.context?.financialStatementId || activeStatement?.id;
  const currentFileName = overview?.context?.fileName || activeStatement?.fileName || "Báo cáo tài chính";
  const selectedPeriod = overview?.context?.selectedPeriod;

  // 1. Export JSON
  const handleExportJSON = () => {
    if (!overview) return;
    const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
      JSON.stringify(overview, null, 2)
    )}`;
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", jsonString);
    downloadAnchor.setAttribute(
      "download",
      `Financial_Assessment_${overview.context?.taxCode || "MST"}_${overview.context?.selectedPeriod || "2025"}.json`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  // 2. Export CSV with UTF-8 BOM
  const handleExportCSV = () => {
    if (!overview) return;

    const BOM = "\uFEFF";
    let csvContent = "Nhóm chỉ số,Mã chỉ số,Tên chỉ số,Giá trị thực tế,Đơn vị,Ngưỡng an toàn,Đánh giá\n";

    overview.visualAnalytics.ratioComparisons?.forEach((item) => {
      csvContent += `"${item.category}","${item.ratioCode}","${item.ratioName}","${
        item.actualValue ?? "N/A"
      }","${item.unit}","${item.configuredThreshold ?? "N/A"}","${
        item.evaluationStatus
      }"\n`;
    });

    const blob = new Blob([BOM + csvContent], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", url);
    downloadAnchor.setAttribute(
      "download",
      `Chi_So_Tai_Chinh_${overview.context?.taxCode || "MST"}_${overview.context?.selectedPeriod || "2025"}.csv`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
    URL.revokeObjectURL(url);
  };

  // 3. Print / Export PDF
  const handlePrintPDF = () => {
    window.print();
  };

  // 4. Edit Data
  const handleEditData = () => {
    const id = targetStatementId;
    if (id) {
      router.push(`/statements/${id}/analysis?step=3`);
    } else {
      router.push("/statements");
    }
  };

  // 5. Delete Statement Session
  const handleDeleteStatement = async () => {
    if (!targetStatementId) return;

    try {
      setIsDeleting(true);
      setDeleteError(null);

      await statementService.deleteStatement(targetStatementId);

      // Invalidate queries to refresh dashboard & header dropdown
      await queryClient.invalidateQueries({ queryKey: ["financial-statements"] });
      await queryClient.invalidateQueries({ queryKey: ["financial-statements-list"] });
      await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });

      // Update zustand store
      const remainingStatements = statements.filter((s) => s.id !== targetStatementId);
      setStatements(remainingStatements);

      if (remainingStatements.length > 0) {
        setActiveStatementId(remainingStatements[0].id);
      } else {
        setActiveStatementId(null);
      }

      setShowConfirmModal(false);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Xóa phiên báo cáo thất bại. Vui lòng thử lại.";
      setDeleteError(msg);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-3 print:hidden">
      {/* Khối 6: Thanh Công Cụ / Tác Vụ Xuất Báo Cáo */}
      <div className="rounded-xl bg-white border border-slate-200 p-4 shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Button
            onClick={handleEditData}
            variant="outline"
            className="h-9 text-xs font-semibold border-slate-300 text-slate-700 hover:bg-slate-50"
          >
            <Edit3 className="h-3.5 w-3.5 mr-1.5 text-[#006c49]" />
            ✏️ Hiệu chỉnh số liệu
          </Button>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <Button
            onClick={handleExportJSON}
            variant="outline"
            className="h-9 text-xs border-slate-200 text-slate-600 hover:bg-slate-50"
          >
            <Download className="h-3.5 w-3.5 mr-1.5 text-slate-500" />
            Tải JSON
          </Button>

          <Button
            onClick={handleExportCSV}
            variant="outline"
            className="h-9 text-xs border-slate-200 text-slate-600 hover:bg-slate-50"
          >
            <FileSpreadsheet className="h-3.5 w-3.5 mr-1.5 text-emerald-600" />
            Tải CSV (UTF-8 BOM)
          </Button>

          <Button
            onClick={handlePrintPDF}
            className="h-9 text-xs bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-medium"
          >
            <Printer className="h-3.5 w-3.5 mr-1.5" />
            Xuất PDF Executive Report
          </Button>
        </div>
      </div>

      {/* Dòng Tác Vụ Bên Dưới Thanh Công Cụ: Quản lý & Xóa phiên báo cáo */}
      <div className="rounded-xl bg-slate-50/80 border border-slate-200/80 px-4 py-3 flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2 min-w-0">
          <FileText className="h-4 w-4 text-slate-400 shrink-0" />
          <span className="text-slate-500">Đang hiển thị phiên:</span>
          <span className="font-semibold text-slate-800 font-mono truncate max-w-xs sm:max-w-md">
            {currentFileName}
          </span>
          {selectedPeriod && (
            <span className="inline-block px-2 py-0.5 rounded bg-slate-200/70 text-[11px] font-medium text-slate-600 shrink-0">
              {selectedPeriod}
            </span>
          )}
        </div>

        <Button
          onClick={() => {
            setDeleteError(null);
            setShowConfirmModal(true);
          }}
          variant="outline"
          size="sm"
          disabled={!targetStatementId || isDeleting}
          className="h-8 text-xs font-semibold text-rose-600 border-rose-200 bg-white hover:bg-rose-50 hover:text-rose-700 hover:border-rose-300 transition-colors shrink-0 shadow-2xs"
          title="Xóa toàn bộ dữ liệu bóc tách và phân tích của phiên báo cáo này"
        >
          <Trash2 className="h-3.5 w-3.5 mr-1.5 text-rose-500" />
          Xóa phiên báo cáo này
        </Button>
      </div>

      {/* Modal Xác Nhận Xóa Phiên Báo Cáo */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-start justify-between">
              <div className="h-11 w-11 rounded-xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-600 shrink-0">
                <AlertTriangle className="h-6 w-6" />
              </div>
              <button
                type="button"
                onClick={() => !isDeleting && setShowConfirmModal(false)}
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
                Bạn có chắc chắn muốn xóa phiên báo cáo này khỏi hệ thống? Thao tác này sẽ xóa vĩnh viễn tệp scan/upload, 25 chỉ tiêu tài chính, 14 tỷ số thẩm định và mọi nhận định AI liên quan.
              </p>
            </div>

            <div className="p-3 rounded-lg bg-slate-50 border border-slate-200 text-xs space-y-1.5 text-slate-600">
              <div className="flex justify-between items-center">
                <span className="text-slate-400">Tệp tài liệu:</span>
                <span className="font-semibold text-slate-800 font-mono truncate max-w-[220px]" title={currentFileName}>
                  {currentFileName}
                </span>
              </div>
              {selectedPeriod && (
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Kỳ BCTC:</span>
                  <span className="font-semibold text-slate-800">
                    {selectedPeriod}
                  </span>
                </div>
              )}
              {targetStatementId && (
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Mã phiên ID:</span>
                  <span className="font-mono text-slate-600">
                    #{targetStatementId}
                  </span>
                </div>
              )}
            </div>

            {deleteError && (
              <div className="p-2.5 rounded-lg bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {deleteError}
              </div>
            )}

            <div className="flex items-center justify-end gap-2.5 pt-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowConfirmModal(false)}
                disabled={isDeleting}
                className="text-xs h-9 px-4"
              >
                Hủy bỏ
              </Button>
              <Button
                size="sm"
                onClick={handleDeleteStatement}
                disabled={isDeleting}
                className="bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs h-9 px-4 shadow-xs"
              >
                {isDeleting ? (
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

