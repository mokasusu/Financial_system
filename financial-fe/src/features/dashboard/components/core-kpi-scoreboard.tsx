"use client";

import type { DashboardKPI } from "../types";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useRatioThresholds, evaluateRatioHealth } from "../threshold-store";

interface CoreKpiScoreboardProps {
  kpis: DashboardKPI[];
}

export function CoreKpiScoreboard({ kpis }: CoreKpiScoreboardProps) {
  const { getConfig } = useRatioThresholds();

  const getKpiItem = (code: string) => {
    const found = kpis.find((k) => k.code === code);
    const config = getConfig(code);
    const evaluation = evaluateRatioHealth(found?.value ?? null, config);

    const isHigher = config.direction === "HIGHER_IS_BETTER";
    const unitSuffix = config.unit === "PERCENT" ? "%" : config.unit === "DAYS" ? " ngày" : "x";

    const conditionText = isHigher
      ? `Chuẩn: ≥ ${config.safeThreshold}${unitSuffix} (Lưu ý: < ${config.warningThreshold}${unitSuffix})`
      : `Chuẩn: ≤ ${config.safeThreshold}${unitSuffix} (Lưu ý: > ${config.warningThreshold}${unitSuffix})`;

    return {
      code,
      name: config.shortName.toUpperCase(),
      value: found?.value ?? null,
      unit: config.unit,
      evaluation,
      config,
      conditionText,
    };
  };

  const items = [
    getKpiItem("CURRENT_RATIO"),
    getKpiItem("DEBT_TO_EQUITY"),
    getKpiItem("ROA"),
    getKpiItem("ROE"),
  ];

  const getGaugePercentage = (value: number | null, code: string) => {
    if (value === null || value === undefined) return 0;
    const num = Number(value);
    if (code === "CURRENT_RATIO") {
      return Math.min((num / 3.0) * 100, 100);
    }
    if (code === "DEBT_TO_EQUITY") {
      return Math.min((num / 2.0) * 100, 100);
    }
    if (code === "ROA") {
      const val = num * 100 > 1 ? num : num * 100;
      return Math.min((val / 15.0) * 100, 100);
    }
    if (code === "ROE") {
      const val = num * 100 > 1 ? num : num * 100;
      return Math.min((val / 30.0) * 100, 100);
    }
    return 60;
  };

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {items.map((item) => {
        const percentage = getGaugePercentage(item.value, item.code);
        const { status, label, badgeVariant, formattedValue } = item.evaluation;

        const gaugeBg =
          status === "SAFE"
            ? "bg-[#006c49]"
            : status === "WARNING"
            ? "bg-[#f59e0b]"
            : status === "DANGER"
            ? "bg-[#ef4444]"
            : "bg-slate-300";

        return (
          <Card key={item.code} className="p-4 space-y-3 bg-white border border-slate-200/90 shadow-2xs">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-slate-500 uppercase tracking-tight truncate max-w-[170px]" title={item.name}>
                {item.name}
              </span>
              <Badge variant={badgeVariant} className="text-[10px] font-bold">
                {label}
              </Badge>
            </div>

            <div>
              <div className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight font-mono">
                {formattedValue}
              </div>
              <div className="text-[10px] text-slate-400 mt-1 truncate" title={item.conditionText}>
                {item.conditionText}
              </div>
            </div>

            {/* Thanh tiến trình đo lường đổi màu theo 3 mức */}
            <div className="pt-1">
              <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all duration-500 rounded-full ${gaugeBg}`}
                  style={{ width: `${Math.max(percentage, 5)}%` }}
                />
              </div>
            </div>
          </Card>
        );
      })}
    </div>
  );
}

