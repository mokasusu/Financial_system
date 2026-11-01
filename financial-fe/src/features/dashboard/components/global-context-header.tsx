"use client";

import type { DashboardContext } from "../types";
import { TrendingUp, Calendar, BookOpen } from "lucide-react";

interface GlobalContextHeaderProps {
  context: DashboardContext;
  overallScore?: number;
  healthClassification?: string;
  isMultiYear?: boolean;
  onToggleMultiYear?: () => void;
}

export function GlobalContextHeader({
  context,
  overallScore = 88,
  healthClassification = "AN TOÀN",
  isMultiYear = false,
  onToggleMultiYear,
}: GlobalContextHeaderProps) {
  return (
    <div className="rounded-xl bg-[#006c49] text-white p-5 shadow-sm space-y-3">
      {/* Top Line: Standard Clean Title & Health Assessment Score Badge */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-lg sm:text-xl font-extrabold tracking-tight uppercase">
          BÁO CÁO TỔNG QUAN SỨC KHỎE TÀI CHÍNH
        </h1>

        <div className="flex items-center gap-2 shrink-0">
          <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-500/20 border border-emerald-400/40 text-emerald-100 font-bold text-xs shadow-xs">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            ĐÁNH GIÁ: {healthClassification} ({overallScore}/100)
          </div>

          {onToggleMultiYear && (
            <button
              onClick={onToggleMultiYear}
              className={`text-xs px-2.5 py-1.5 rounded-lg font-medium transition-colors border flex items-center gap-1.5 ${
                isMultiYear
                  ? "bg-white text-[#006c49] border-white font-bold"
                  : "bg-white/10 text-emerald-100 border-white/20 hover:bg-white/20"
              }`}
            >
              <TrendingUp className="h-3.5 w-3.5" />
              <span>{isMultiYear ? "Đang xem: Đa kỳ" : "So sánh đa năm"}</span>
            </button>
          )}
        </div>
      </div>

      {/* Bottom Line: Metadata Details (Without duplicate Company/MST/File) */}
      <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-emerald-100/85 pt-2 border-t border-white/15">
        <div className="flex items-center gap-1.5">
          <Calendar className="h-3.5 w-3.5 text-emerald-300" />
          <span className="text-emerald-300/80">Kỳ BCTC:</span>{" "}
          <span className="font-semibold">{context.selectedPeriod || "31/12/2025"}</span>
        </div>
        <span className="text-emerald-300/40">•</span>
        <div className="flex items-center gap-1.5">
          <BookOpen className="h-3.5 w-3.5 text-emerald-300" />
          <span className="text-emerald-300/80">Chuẩn mực:</span>{" "}
          <span className="font-semibold">
            {context.accountingStandard
              ? (context.accountingStandard.includes("TT")
                  ? context.accountingStandard
                  : `Thông tư ${context.accountingStandard}`)
              : "TT 200/2014/TT-BTC"}
          </span>
        </div>
      </div>
    </div>
  );
}

