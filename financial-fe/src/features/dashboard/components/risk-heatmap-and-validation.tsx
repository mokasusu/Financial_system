"use client";

import { useState } from "react";
import type { DashboardValidation, DashboardCategory } from "../types";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { CheckCircle2, AlertTriangle, XCircle, SlidersHorizontal } from "lucide-react";
import { useRatioThresholds, evaluateRatioHealth } from "../threshold-store";
import { QuickEditThresholdsModal } from "./quick-edit-thresholds-modal";

interface RiskHeatmapAndValidationProps {
  validation?: DashboardValidation;
  financialRatios?: DashboardCategory[];
}

export function RiskHeatmapAndValidation({
  validation,
  financialRatios,
}: RiskHeatmapAndValidationProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { thresholds } = useRatioThresholds();

  // Danh mục 4 nhóm tài chính tiêu chuẩn
  const categoryGroups: { key: string; name: string }[] = [
    { key: "LIQUIDITY", name: "Thanh khoản" },
    { key: "SOLVENCY", name: "Đòn bẩy" },
    { key: "PROFITABILITY", name: "Sinh lời" },
    { key: "EFFICIENCY", name: "Hoạt động" },
  ];

  // Trích xuất map giá trị thực tế của các tỷ số từ API
  const apiRatioMap = new Map<string, number | null>();
  if (financialRatios && financialRatios.length > 0) {
    financialRatios.forEach((cat) => {
      cat.ratios?.forEach((r) => {
        if (r.code) {
          apiRatioMap.set(r.code.toUpperCase(), r.value ?? null);
        }
      });
    });
  }

  // Tạo dữ liệu cho Ma trận 14 tỷ số tài chính
  const matrixData = categoryGroups.map((cat) => {
    const groupThresholds = thresholds.filter((t) => t.category === cat.key);
    return {
      group: cat.name,
      items: groupThresholds.map((config) => {
        const rawVal = apiRatioMap.get(config.code.toUpperCase()) ?? null;
        const evaluation = evaluateRatioHealth(rawVal, config);
        const unitSuffix = config.unit === "PERCENT" ? "%" : config.unit === "DAYS" ? " ngày" : "x";
        const isHigher = config.direction === "HIGHER_IS_BETTER";

        const tooltip =
          `${config.name} (${config.code})\n` +
          `• Giá trị thực tế: ${evaluation.formattedValue}\n` +
          `• Đánh giá: ${evaluation.label}\n` +
          `• Mốc An toàn (Xanh): ${isHigher ? "≥" : "≤"} ${config.safeThreshold}${unitSuffix}\n` +
          `• Mốc Lưu ý (Vàng): ${isHigher ? "≥" : "≤"} ${config.warningThreshold}${unitSuffix}\n` +
          `• Công thức: ${config.formula}`;

        return {
          code: config.code,
          name: config.shortName,
          fullName: config.name,
          val: evaluation.formattedValue,
          status: evaluation.status,
          label: evaluation.label,
          tooltip,
        };
      }),
    };
  });

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
        <CardHeader className="pb-3 border-b border-slate-100 flex flex-row items-center justify-between">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
            MA TRẬN CẢNH BÁO RỦI RO 14 TỶ SỐ TÀI CHÍNH
          </CardTitle>
          <button
            onClick={() => setIsModalOpen(true)}
            className="text-[11px] font-semibold text-[#006c49] hover:text-[#005237] hover:underline flex items-center gap-1 cursor-pointer transition-colors"
          >
            <SlidersHorizontal className="h-3 w-3" />
            <span>Tùy chỉnh ngưỡng</span>
          </button>
        </CardHeader>
        <CardContent className="p-4 space-y-2 flex-1 flex flex-col justify-between">
          <div className="space-y-2">
            {matrixData.map((row) => (
              <div key={row.group} className="grid grid-cols-5 gap-2 items-center">
                {/* Row Header */}
                <div className="text-[11px] font-bold text-slate-600 truncate">
                  {row.group}
                </div>

                {/* Cells */}
                {row.items.map((cell) => {
                  const isSafe = cell.status === "SAFE";
                  const isWarning = cell.status === "WARNING";
                  const isDanger = cell.status === "DANGER";

                  const bgColor = isSafe
                    ? "bg-[#22c55e]"
                    : isWarning
                    ? "bg-[#f59e0b]"
                    : isDanger
                    ? "bg-[#ef4444]"
                    : "bg-slate-300 text-slate-700";

                  return (
                    <div
                      key={cell.code}
                      className={`p-2.5 rounded-lg text-center transition-all ${bgColor} text-white shadow-2xs hover:brightness-105 cursor-help`}
                      title={cell.tooltip}
                    >
                      <div className="text-xs font-mono font-bold leading-tight">
                        {cell.val}
                      </div>
                      <div
                        className="text-[10px] font-medium opacity-95 truncate mt-0.5"
                        title={cell.fullName}
                      >
                        {cell.name}
                      </div>
                    </div>
                  );
                })}
              </div>
            ))}
          </div>

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

      <QuickEditThresholdsModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialTab="matrix14"
      />
    </div>
  );
}

