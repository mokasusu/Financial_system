"use client";

import type { DashboardValidation, DashboardCategory } from "../types";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { CheckCircle2, AlertTriangle, XCircle, ExternalLink } from "lucide-react";

interface RiskHeatmapAndValidationProps {
  validation?: DashboardValidation;
  financialRatios?: DashboardCategory[];
}

export function RiskHeatmapAndValidation({
  validation,
  financialRatios,
}: RiskHeatmapAndValidationProps) {
  // Dynamic 14-ratio matrix from API
  const matrixData =
    financialRatios && financialRatios.length > 0
      ? financialRatios.map((group) => ({
          group: group.categoryName,
          items: group.ratios.map((r) => {
            let formattedVal =
              r.value !== null && r.value !== undefined
                ? r.unit === "PERCENT" || r.unit === "%"
                  ? `${(r.value * 100 > 1 ? r.value : r.value * 100).toFixed(2)}%`
                  : r.unit === "DAYS" || r.unit === "NGAY"
                  ? `${r.value} ngày`
                  : `${Number(r.value).toFixed(2)}x`
                : "—";
            return {
              name: r.name,
              val: formattedVal,
              status: r.evaluationStatus,
            };
          }),
        }))
      : [];

  const validationList =
    validation?.results && validation.results.length > 0
      ? validation.results.map((r) => ({
          name: r.ruleName,
          diff: r.differenceAmount ?? 0,
          status: r.status,
        }))
      : [];

  const passedCount = validation?.passedRules ?? 0;
  const totalCount = validation?.totalRules ?? 0;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
      {/* Khối 3C: Ma trận Cảnh báo Rủi ro 14 Tỷ số (2 cols on lg) */}
      <Card className="lg:col-span-2 bg-white flex flex-col justify-between">
        <CardHeader className="pb-3 border-b border-slate-100">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
            MA TRẬN CẢNH BÁO RỦI RO 14 TỶ SỐ TÀI CHÍNH
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 space-y-2 flex-1 flex flex-col justify-between">
          {matrixData.length > 0 ? (
            <div className="space-y-2">
              {matrixData.map((row) => (
                <div key={row.group} className="grid grid-cols-5 gap-2 items-center">
                  {/* Row Header */}
                  <div className="text-[11px] font-bold text-slate-600 truncate">
                    {row.group}
                  </div>

                  {/* 4 Cells */}
                  {row.items.map((cell) => {
                    const isDanger = cell.status === "DANGER" || cell.status === "FAIL";
                    const isWarning = cell.status === "WARNING" || cell.status === "LUU_Y";
                    const isSafe = cell.status === "PASS" || cell.status === "SAFE";
                    const bgColor = isDanger
                      ? "bg-[#ef4444]"
                      : isWarning
                      ? "bg-[#f59e0b]"
                      : isSafe
                      ? "bg-[#22c55e]"
                      : "bg-slate-400";

                    return (
                      <div
                        key={cell.name}
                        className={`p-2.5 rounded-lg text-center transition-all ${bgColor} text-white shadow-xs hover:brightness-105`}
                      >
                        <div className="text-xs font-mono font-bold leading-tight">
                          {cell.val}
                        </div>
                        <div
                          className="text-[10px] font-medium opacity-95 truncate mt-0.5"
                          title={cell.name}
                        >
                          {cell.name}
                        </div>
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center text-slate-400 space-y-2 my-auto">
              <AlertTriangle className="h-8 w-8 mx-auto text-amber-500/80 mb-2" />
              <p className="font-semibold text-xs text-slate-700">
                Chưa có dữ liệu tỷ số tài chính cho phiên BCTC này
              </p>
              <p className="text-[11px] text-slate-400 max-w-sm mx-auto">
                Vui lòng vào menu &quot;Phân tích BCTC&quot; để chạy quy trình bóc tách OCR và tính toán chỉ số.
              </p>
            </div>
          )}

          {/* Matrix Legend */}
          <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-4 text-[11px] text-slate-500">
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded bg-[#22c55e]" />
              <span>An toàn / Đạt chuẩn</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded bg-[#f59e0b]" />
              <span>Cần lưu ý</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded bg-[#ef4444]" />
              <span>Rủi ro cao</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Khối 4: Kiểm định Đẳng thức TT200 (1 col on lg) */}
      <Card className="bg-white flex flex-col justify-between">
        <CardHeader className="pb-3 border-b border-slate-100 flex flex-row items-center justify-between">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
            KIỂM ĐỊNH ĐẲNG THỨC TT200
          </CardTitle>
          <Badge
            variant={totalCount > 0 && passedCount === totalCount ? "safe" : totalCount > 0 ? "warning" : "outline"}
            className="text-[10px] font-mono"
          >
            {totalCount > 0 ? `${passedCount}/${totalCount} ĐẠT` : "CHƯA KIỂM ĐỊNH"}
          </Badge>
        </CardHeader>

        <CardContent className="p-4 space-y-2 text-xs flex-1 flex flex-col justify-between">
          {validationList.length > 0 ? (
            <div className="space-y-2 overflow-y-auto max-h-[380px]">
              {validationList.map((eq, idx) => {
                const isPass = eq.status === "PASS" || eq.status === "DAT";
                const isWarning = eq.status === "WARNING" || eq.status === "LUU_Y";
                return (
                  <div
                    key={idx}
                    className={`p-2 rounded-lg border flex items-start justify-between gap-2 ${
                      isPass
                        ? "bg-emerald-50/50 border-emerald-100/60"
                        : isWarning
                        ? "bg-amber-50/50 border-amber-200/60"
                        : "bg-rose-50/50 border-rose-200/60"
                    }`}
                  >
                    <div className="flex items-start gap-2">
                      {isPass ? (
                        <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0 mt-0.5" />
                      ) : isWarning ? (
                        <AlertTriangle className="h-3.5 w-3.5 text-amber-600 shrink-0 mt-0.5" />
                      ) : (
                        <XCircle className="h-3.5 w-3.5 text-rose-600 shrink-0 mt-0.5" />
                      )}
                      <div className="text-[11px]">
                        <div className="font-medium text-slate-800">{eq.name}</div>
                        <div
                          className={`text-[10px] font-mono ${
                            isPass ? "text-emerald-700" : isWarning ? "text-amber-700" : "text-rose-700"
                          }`}
                        >
                          {isPass
                            ? `Khớp 100% (Lệch ${Number(eq.diff).toLocaleString("vi-VN")} VND)`
                            : `Chênh lệch: ${Number(eq.diff).toLocaleString("vi-VN")} VND`}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="py-12 text-center text-slate-400 space-y-2 my-auto">
              <AlertTriangle className="h-7 w-7 mx-auto text-slate-300 mb-1" />
              <p className="font-semibold text-xs text-slate-600">
                Chưa có kết quả kiểm tra cân đối kế toán
              </p>
              <p className="text-[11px] text-slate-400">
                Chạy Bước 4 trong luồng Phân tích để xác minh tính toàn vẹn TT200.
              </p>
            </div>
          )}

          <div className="pt-3 border-t border-slate-100 text-[11px] text-slate-500 flex items-center justify-between">
            <span>Dữ liệu kế toán chuẩn hóa</span>
            <span className={passedCount === totalCount && totalCount > 0 ? "text-emerald-700 font-medium" : "text-slate-400 font-medium"}>
              {totalCount > 0 ? `${((passedCount / totalCount) * 100).toFixed(0)}% hoàn tất` : "Chờ phân tích"}
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

