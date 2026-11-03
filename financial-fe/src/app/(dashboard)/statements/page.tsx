"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useStatementStore } from "@/stores/statement-store";
import { statementService } from "@/features/statements/services";
import type { FinancialStatement } from "@/features/statements/types";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  FileSpreadsheet,
  Upload,
  ArrowRight,
  Clock,
  CheckCircle2,
  AlertTriangle,
  FileText,
  Trash2,
  Loader2,
} from "lucide-react";

export default function StatementsListPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  const setStoreStatements = useStatementStore((state) => state.setStatements);
  const setActiveStatementId = useStatementStore((state) => state.setActiveStatementId);
  const activeStatementId = useStatementStore((state) => state.activeStatementId);

  const [deletingId, setDeletingId] = useState<number | null>(null);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ["financial-statements-list"],
    queryFn: () => statementService.listStatements(0, 20),
  });

  const statements = data?.items || [];

  const handleDelete = async (id: number, fileName?: string) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa phiên báo cáo "${fileName || `#${id}`}" không? Thao tác này sẽ xóa mềm báo cáo và ẩn khỏi danh sách để lưu vết bảo mật.`)) {
      return;
    }

    try {
      setDeletingId(id);
      await statementService.deleteStatement(id);

      await queryClient.invalidateQueries({ queryKey: ["financial-statements-list"] });
      await queryClient.invalidateQueries({ queryKey: ["financial-statements"] });
      await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });

      if (activeStatementId === id) {
        const remaining = statements.filter((s) => s.id !== id);
        setStoreStatements(remaining);
        setActiveStatementId(remaining.length > 0 ? remaining[0].id : null);
      }
    } catch (err: unknown) {
      alert((err as { message?: string })?.message || "Xóa phiên báo cáo thất bại.");
    } finally {
      setDeletingId(null);
    }
  };

  const renderStatusBadge = (status?: string, isDraft?: boolean) => {
    if (isDraft) {
      return (
        <Badge variant="outline" className="text-amber-700 bg-amber-50 border-amber-300">
          <Clock className="h-3 w-3 mr-1 text-amber-500" />
          BẢN NHÁP
        </Badge>
      );
    }
    switch (status) {
      case "DATA_CONFIRMED":
        return <Badge variant="safe">ĐÃ CHỐT SỐ LIỆU</Badge>;
      case "REVIEWING_DATA":
        return <Badge variant="warning">CHỜ ĐỐI SOÁT</Badge>;
      case "OCR_PROCESSING":
      case "QUEUED":
        return (
          <Badge variant="outline" className="text-emerald-700 bg-emerald-50 border-emerald-200">
            <Clock className="h-3 w-3 mr-1 animate-spin" />
            ĐANG BÓC TÁCH
          </Badge>
        );
      case "FAILED":
        return <Badge variant="danger">LỖI XỬ LÝ</Badge>;
      default:
        return <Badge variant="outline">{status || "QUEUED"}</Badge>;
    }
  };

  const getStepForStatus = (status?: string) => {
    switch (status) {
      case "QUEUED":
      case "OCR_PROCESSING":
        return 2;
      case "REVIEWING_DATA":
        return 3;
      case "DATA_CONFIRMED":
        return 5;
      default:
        return 1;
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Top Banner & Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Hồ sơ Báo cáo Tài chính
          </h1>
          <p className="text-sm text-slate-500">
            Quản lý các đợt số hóa BCTC, theo dõi tiến trình bóc tách OCR và kích hoạt chuỗi phân tích 7 bước
          </p>
        </div>

        <Button
          onClick={() => router.push("/statements/pipeline")}
          className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-semibold h-10 px-5"
        >
          <Upload className="h-4 w-4 mr-2" />
          Tải lên BCTC mới
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          <Skeleton className="h-20 w-full rounded-xl bg-slate-200" />
          <Skeleton className="h-20 w-full rounded-xl bg-slate-200" />
          <Skeleton className="h-20 w-full rounded-xl bg-slate-200" />
        </div>
      ) : statements.length === 0 ? (
        <Card className="bg-white border-dashed border-2 border-slate-200 p-12 text-center">
          <div className="flex flex-col items-center justify-center max-w-md mx-auto space-y-4">
            <div className="h-16 w-16 rounded-2xl bg-emerald-50 text-[#006c49] flex items-center justify-center border border-emerald-100">
              <FileSpreadsheet className="h-8 w-8" />
            </div>

            <div className="space-y-1">
              <h3 className="text-base font-bold text-slate-900">
                Chưa có Báo cáo Tài chính nào
              </h3>
              <p className="text-xs text-slate-500">
                Bắt đầu quy trình thẩm định bằng việc tải lên file BCTC dạng scan hoặc PDF đầu tiên của doanh nghiệp.
              </p>
            </div>

            <Button
              onClick={() => router.push("/statements/pipeline")}
              className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs font-semibold h-9 px-4 text-xs"
            >
              <Upload className="h-3.5 w-3.5 mr-1.5" />
              Khởi động Quy trình 7 bước
            </Button>
          </div>
        </Card>
      ) : (
        <Card className="bg-white shadow-xs overflow-hidden">
          <div className="divide-y divide-slate-100">
            {statements.map((stmt) => (
              <div
                key={stmt.id}
                className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-50/60 transition-colors"
              >
                <div className="flex items-start gap-3.5">
                  <div className="h-10 w-10 rounded-xl bg-[#006c49]/10 text-[#006c49] flex items-center justify-center shrink-0 border border-[#006c49]/20">
                    <FileText className="h-5 w-5" />
                  </div>

                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sm text-slate-900">
                        {stmt.originalFileName || `BCTC_${stmt.period}.pdf`}
                      </span>
                      {renderStatusBadge(stmt.status, stmt.isDraft)}
                    </div>

                    <div className="flex items-center gap-3 text-xs text-slate-500">
                      <span>
                        Kỳ báo cáo: <strong>{stmt.period}</strong>
                      </span>
                      <span>•</span>
                      <span>
                        Ngày tải: {new Date(stmt.createdAt).toLocaleDateString("vi-VN")}
                      </span>
                      {stmt.fileSize && (
                        <>
                          <span>•</span>
                          <span>{(stmt.fileSize / (1024 * 1024)).toFixed(1)} MB</span>
                        </>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    onClick={() =>
                      router.push(
                        `/statements/${stmt.id}/analysis?step=${getStepForStatus(
                          stmt.status
                        )}`
                      )
                    }
                    variant="outline"
                    className="h-8 text-xs font-semibold text-[#006c49] border-emerald-200 hover:bg-emerald-50"
                  >
                    Vào quy trình thẩm định
                    <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
                  </Button>

                  <Button
                    onClick={() => handleDelete(stmt.id, stmt.originalFileName || stmt.fileName)}
                    disabled={deletingId === stmt.id}
                    variant="outline"
                    size="sm"
                    className="h-8 w-8 p-0 text-rose-600 border-rose-200 hover:bg-rose-50 hover:text-rose-700"
                    title="Xóa phiên báo cáo này"
                  >
                    {deletingId === stmt.id ? (
                      <Loader2 className="h-3.5 w-3.5 animate-spin" />
                    ) : (
                      <Trash2 className="h-3.5 w-3.5" />
                    )}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
}

