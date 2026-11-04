"use client";

import { useState } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import type { DashboardVisualAnalytics } from "../types";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { useRatioThresholds } from "../threshold-store";
import { QuickEditThresholdsModal } from "./quick-edit-thresholds-modal";
import { SlidersHorizontal } from "lucide-react";

interface VisualAnalyticsProps {
  data: DashboardVisualAnalytics;
  companyShortName?: string;
}

export function VisualAnalytics({
  data,
  companyShortName = "Doanh nghiệp",
}: VisualAnalyticsProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { thresholds } = useRatioThresholds();

  // 1. Data for 8 Key Ratios Bar Chart from configured key ratios
  const keyConfigs = thresholds.filter((t) => t.isKeyRatio).slice(0, 8);

  const chartData = keyConfigs.map((config) => {
    const found = data.ratioComparisons?.find(
      (rc) => rc.ratioCode?.toUpperCase() === config.code.toUpperCase()
    );

    const isPercent =
      config.unit === "PERCENT" || found?.unit === "PERCENT" || found?.unit === "%";

    let rawVal = found?.actualValue ?? 0;
    let pctActual = 0;
    if (rawVal !== null && rawVal !== undefined) {
      pctActual = isPercent
        ? Math.abs(rawVal) <= 1.0 && rawVal !== 0
          ? rawVal * 100
          : rawVal
        : 0;
    }

    const pctBenchmark = config.industryBenchmark;

    // Quy đổi về hệ số lần (times: chia 100 nếu là %) để đồng nhất thang đo trục tung
    const chartActual = isPercent ? pctActual / 100 : rawVal;
    const chartBenchmark = isPercent ? pctBenchmark / 100 : pctBenchmark;

    return {
      name: config.shortName,
      fullName: config.name,
      actual: Number(chartActual.toFixed(2)),
      benchmark: Number(chartBenchmark.toFixed(2)),
      isPercent,
      origActual: Number(pctActual.toFixed(2)),
      origBenchmark: Number(pctBenchmark.toFixed(2)),
      unit: isPercent ? "%" : config.unit === "DAYS" ? " ngày" : "x",
    };
  });

  const hasChartData = chartData.some((d) => d.actual > 0) || (data.ratioComparisons && data.ratioComparisons.length > 0);

  // 2. Data for Capital Structure Donut Chart
  const totalAssets = data.capitalStructure?.totalAssets || 0;
  const liabilities = data.capitalStructure?.totalLiabilities || 0;
  const equity = data.capitalStructure?.equity || 0;

  const debtPercent = totalAssets > 0 ? Number(((liabilities / totalAssets) * 100).toFixed(0)) : 0;
  const equityPercent = totalAssets > 0 ? Number(((equity / totalAssets) * 100).toFixed(0)) : 0;

  const donutData = totalAssets > 0 ? [
    { name: "Vốn CSH", value: equity, percent: equityPercent },
    { name: "Nợ Phải Trả", value: liabilities, percent: debtPercent },
  ] : [];

  const DONUT_COLORS = ["#006c49", "#6ee7b7"];

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
      {/* 8 Ratios Grouped Bar Chart (2 cols on lg) */}
      <Card className="lg:col-span-2 bg-white">
        <CardHeader className="pb-2 flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
              SO SÁNH 8 TỶ SỐ TÀI CHÍNH TRỌNG YẾU VS NGƯỠNG NGÀNH
            </CardTitle>
            <p className="text-[10px] text-slate-400 mt-0.5">
              Đồng nhất thang đo: các chỉ số (%) được quy đổi sang hệ số lần (x) trên biểu đồ
            </p>
          </div>
          <button
            onClick={() => setIsModalOpen(true)}
            className="text-[11px] font-semibold text-[#006c49] hover:text-[#005237] hover:underline flex items-center gap-1 cursor-pointer transition-colors shrink-0"
          >
            <SlidersHorizontal className="h-3 w-3" />
            <span>Tùy chỉnh ngưỡng</span>
          </button>
        </CardHeader>
        <CardContent className="h-72 pt-2 flex items-center justify-center">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 10, right: 10, left: -10, bottom: 25 }}
              >
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis
                  dataKey="name"
                  tick={{ fontSize: 10, fill: "#64748b" }}
                  angle={-15}
                  textAnchor="end"
                  interval={0}
                />
                <YAxis
                  tick={{ fontSize: 10, fill: "#64748b" }}
                  tickFormatter={(val) => `${val}x`}
                />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "#ffffff",
                    borderColor: "#e2e8f0",
                    borderRadius: "8px",
                    fontSize: "12px",
                  }}
                  formatter={(val: any, name: any, item: any) => {
                    const isPct = item.payload?.isPercent;
                    const num = Number(val);
                    if (isPct) {
                      return [`${num.toFixed(2)}x (${(num * 100).toFixed(2)}%)`, name];
                    }
                    return [`${num.toFixed(2)}x`, name];
                  }}
                />
                <Legend
                  verticalAlign="top"
                  align="left"
                  wrapperStyle={{ fontSize: "11px", paddingBottom: "10px" }}
                />
                <Bar
                  name={`Thực tế ${companyShortName}`}
                  dataKey="actual"
                  fill="#006c49"
                  radius={[4, 4, 0, 0]}
                />
                <Bar
                  name="Ngưỡng An toàn Ngành"
                  dataKey="benchmark"
                  fill="#6ee7b7"
                  radius={[4, 4, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-center text-slate-400 space-y-1">
              <p className="font-semibold text-xs text-slate-600">
                Chưa có dữ liệu so sánh tỷ số
              </p>
              <p className="text-[11px] text-slate-400">
                Dữ liệu sẽ được hiển thị khi BCTC hoàn tất bước tính toán.
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Capital Structure Donut Chart (1 col on lg) */}
      <Card className="bg-white flex flex-col justify-between">
        <CardHeader className="pb-2">
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
            CƠ CẤU NGUỒN VỐN (TT200)
          </CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col items-center justify-center pt-2">
          {totalAssets > 0 ? (
            <>
              <div className="h-52 w-full relative">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={donutData}
                      cx="50%"
                      cy="50%"
                      innerRadius={55}
                      outerRadius={80}
                      paddingAngle={3}
                      dataKey="value"
                    >
                      {donutData.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={DONUT_COLORS[index % DONUT_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(val: unknown) => [
                        `${Number(val).toLocaleString("vi-VN")} VND`,
                        "",
                      ]}
                      contentStyle={{
                        backgroundColor: "#ffffff",
                        borderColor: "#e2e8f0",
                        borderRadius: "8px",
                        fontSize: "12px",
                      }}
                    />
                  </PieChart>
                </ResponsiveContainer>

                {/* Donut Center Text */}
                <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                  <span className="text-xl font-extrabold text-slate-900 font-mono">
                    {equityPercent}%
                  </span>
                  <span className="text-[10px] text-slate-400 font-medium">Vốn CSH</span>
                </div>
              </div>

              {/* Legend Details */}
              <div className="w-full pt-3 border-t border-slate-100 flex items-center justify-around text-xs">
                <div className="flex items-center gap-1.5">
                  <span className="h-3 w-3 rounded-full bg-[#006c49]" />
                  <span className="text-slate-600 font-medium">
                    Vốn CSH: <strong>{equityPercent}%</strong>
                  </span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="h-3 w-3 rounded-full bg-[#6ee7b7]" />
                  <span className="text-slate-600 font-medium">
                    Nợ Phải Trả: <strong>{debtPercent}%</strong>
                  </span>
                </div>
              </div>
            </>
          ) : (
            <div className="py-12 text-center text-slate-400 space-y-1">
              <p className="font-semibold text-xs text-slate-600">
                Chưa có dữ liệu cơ cấu nguồn vốn
              </p>
              <p className="text-[11px] text-slate-400">
                Dữ liệu sẽ được tổng hợp tự động từ Bảng cân đối kế toán.
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      <QuickEditThresholdsModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialTab="key8"
      />
    </div>
  );
}

