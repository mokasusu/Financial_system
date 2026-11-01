"use client";

import type { DashboardKPI } from "../types";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";

interface CoreKpiScoreboardProps {
  kpis: DashboardKPI[];
}

export function CoreKpiScoreboard({ kpis }: CoreKpiScoreboardProps) {
  // Map or fallback to the 4 core KPIs
  const getKpi = (code: string, fallbackName: string, defaultFormula: string) => {
    const found = kpis.find((k) => k.code === code);
    return {
      name: found?.name || fallbackName,
      value: found?.value ?? null,
      unit: found?.unit || "TIMES",
      status: found?.evaluationStatus || "PASS",
      threshold: found?.configuredThreshold,
      operator: found?.comparisonOperator || ">=",
      formula: defaultFormula,
    };
  };

  const currentRatio = getKpi(
    "CURRENT_RATIO",
    "THANH TOÁN HIỆN HÀNH",
    "Chỉ tiêu: ≥ 1.50x (TSNH / Nợ ngắn hạn)"
  );
  const debtToEquity = getKpi(
    "DEBT_TO_EQUITY",
    "NỢ / VỐN CSH (D/E)",
    "Chỉ tiêu: ≤ 1.00x (Nợ phải trả / VCSH)"
  );
  const roa = getKpi(
    "ROA",
    "SINH LỜI / TÀI SẢN (ROA)",
    "Chỉ tiêu: ≥ 5.00% (LNST / Tổng tài sản)"
  );
  const roe = getKpi(
    "ROE",
    "SINH LỜI / VỐN CSH (ROE)",
    "Chỉ tiêu: ≥ 12.00% (LNST / Vốn CSH)"
  );

  const renderBadge = (status: string) => {
    if (status === "PASS" || status === "AN_TOAN" || status === "SAFE") {
      return <Badge variant="safe">AN TOÀN</Badge>;
    }
    if (status === "WARNING" || status === "LUU_Y") {
      return <Badge variant="warning">LƯU Ý</Badge>;
    }
    return <Badge variant="danger">CẢNH BÁO</Badge>;
  };

  const formatValue = (value: number | null, unit: string) => {
    if (value === null || value === undefined) return "—";
    if (unit === "PERCENT" || unit === "%") {
      return `${(value * 100 > 1 ? value : value * 100).toFixed(2)}%`;
    }
    return `${Number(value).toFixed(2)}x`;
  };

  const getGaugePercentage = (value: number | null, code: string) => {
    if (value === null || value === undefined) return 0;
    if (code === "CURRENT_RATIO") {
      return Math.min((value / 3.0) * 100, 100);
    }
    if (code === "DEBT_TO_EQUITY") {
      return Math.min((value / 2.0) * 100, 100);
    }
    if (code === "ROA") {
      const val = value * 100 > 1 ? value : value * 100;
      return Math.min((val / 15.0) * 100, 100);
    }
    if (code === "ROE") {
      const val = value * 100 > 1 ? value : value * 100;
      return Math.min((val / 30.0) * 100, 100);
    }
    return 60;
  };

  const items = [
    { ...currentRatio, code: "CURRENT_RATIO" },
    { ...debtToEquity, code: "DEBT_TO_EQUITY" },
    { ...roa, code: "ROA" },
    { ...roe, code: "ROE" },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {items.map((item) => {
        const percentage = getGaugePercentage(item.value, item.code);
        const isSafe = item.status === "PASS" || item.status === "SAFE";

        return (
          <Card key={item.code} className="p-4 space-y-3 bg-white">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-500 uppercase tracking-tight">
                {item.name}
              </span>
              {renderBadge(item.status)}
            </div>

            <div>
              <div className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight font-mono">
                {formatValue(item.value, item.unit)}
              </div>
              <div className="text-[11px] text-slate-400 mt-1 truncate">
                {item.formula}
              </div>
            </div>

            {/* Progress Gauge */}
            <div className="pt-1">
              <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all duration-500 rounded-full ${
                    isSafe ? "bg-[#006c49]" : "bg-amber-500"
                  }`}
                  style={{ width: `${percentage}%` }}
                />
              </div>
            </div>
          </Card>
        );
      })}
    </div>
  );
}

