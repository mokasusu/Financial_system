"use client";

import { useState, useEffect } from "react";
import {
  useRatioThresholds,
  RatioThresholdConfig,
  DEFAULT_RATIO_THRESHOLDS,
} from "../threshold-store";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  X,
  SlidersHorizontal,
  LayoutGrid,
  BarChart3,
  Layers,
  RotateCcw,
  Save,
  CheckSquare,
  Square,
  ArrowUpRight,
  ArrowDownRight,
  Info,
  Check,
} from "lucide-react";

interface QuickEditThresholdsModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialTab?: "kpi" | "key8" | "matrix14";
}

export function QuickEditThresholdsModal({
  isOpen,
  onClose,
  initialTab = "kpi",
}: QuickEditThresholdsModalProps) {
  const { thresholds, updateAllThresholds, resetToDefault } = useRatioThresholds();
  const [activeTab, setActiveTab] = useState<"kpi" | "key8" | "matrix14">(initialTab);
  const [localConfigs, setLocalConfigs] = useState<RatioThresholdConfig[]>(thresholds);
  const [isSaved, setIsSaved] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setLocalConfigs(thresholds);
      setActiveTab(initialTab);
      setIsSaved(false);
      setErrorMessage(null);
    }
  }, [isOpen, thresholds, initialTab]);

  if (!isOpen) return null;

  const handleValueChange = (code: string, field: keyof RatioThresholdConfig, val: any) => {
    setLocalConfigs((prev) =>
      prev.map((item) => (item.code === code ? { ...item, [field]: val } : item))
    );
    setIsSaved(false);
    setErrorMessage(null);
  };

  const handleToggleKeyRatio = (code: string) => {
    const currentSelected = localConfigs.filter((c) => c.isKeyRatio);
    const target = localConfigs.find((c) => c.code === code);
    if (!target) return;

    if (!target.isKeyRatio && currentSelected.length >= 8) {
      setErrorMessage("Biểu đồ so sánh tối đa 8 tỷ số trọng yếu. Vui lòng bỏ chọn một tỷ số khác trước khi chọn thêm.");
      return;
    }

    setLocalConfigs((prev) =>
      prev.map((c) => (c.code === code ? { ...c, isKeyRatio: !c.isKeyRatio } : c))
    );
    setIsSaved(false);
    setErrorMessage(null);
  };

  const handleSave = () => {
    updateAllThresholds(localConfigs);
    setIsSaved(true);
    setTimeout(() => {
      onClose();
    }, 600);
  };

  const handleReset = () => {
    resetToDefault();
    setLocalConfigs(DEFAULT_RATIO_THRESHOLDS);
    setIsSaved(true);
    setErrorMessage(null);
  };

  const selectedKeyRatioCount = localConfigs.filter((c) => c.isKeyRatio).length;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in">
      <div className="relative w-full max-w-4xl max-h-[90vh] bg-white rounded-2xl shadow-2xl border border-slate-200 flex flex-col overflow-hidden animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-emerald-100/80 text-[#006c49] flex items-center justify-center">
              <SlidersHorizontal className="h-4 w-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900">
                Điều Chỉnh Mốc Tham Chiếu & Cảnh Báo Tài Chính
              </h3>
              <p className="text-[11px] text-slate-500">
                Tùy chỉnh 3 mốc Xanh (An toàn), Vàng (Lưu ý), Đỏ (Cảnh báo) hiển thị tức thì trên Dashboard
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="h-8 w-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-200/50 transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Sub-tab Navigation */}
        <div className="px-4 py-2 border-b border-slate-100 bg-white flex items-center justify-between gap-2 flex-wrap">
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setActiveTab("kpi")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeTab === "kpi"
                  ? "bg-[#006c49] text-white shadow-2xs"
                  : "text-slate-600 hover:bg-slate-100"
              }`}
            >
              <LayoutGrid className="h-3.5 w-3.5" />
              4 KPI Cốt Lõi
            </button>

            <button
              onClick={() => setActiveTab("key8")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeTab === "key8"
                  ? "bg-[#006c49] text-white shadow-2xs"
                  : "text-slate-600 hover:bg-slate-100"
              }`}
            >
              <BarChart3 className="h-3.5 w-3.5" />
              8 Tỷ Số vs Ngưỡng Ngành ({selectedKeyRatioCount}/8)
            </button>

            <button
              onClick={() => setActiveTab("matrix14")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                activeTab === "matrix14"
                  ? "bg-[#006c49] text-white shadow-2xs"
                  : "text-slate-600 hover:bg-slate-100"
              }`}
            >
              <Layers className="h-3.5 w-3.5" />
              Ma Trận 14 Chỉ Số (Xanh - Vàng - Đỏ)
            </button>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={handleReset}
            className="text-xs h-7 text-slate-500 hover:text-slate-800 gap-1"
            title="Khôi phục mặc định chuẩn TT200"
          >
            <RotateCcw className="h-3 w-3" />
            Khôi phục TT200
          </Button>
        </div>

        {/* Error message */}
        {errorMessage && (
          <div className="px-4 py-2 bg-rose-50 border-b border-rose-100 text-rose-700 text-xs flex items-center gap-2">
            <Info className="h-4 w-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Modal Body (Scrollable) */}
        <div className="p-4 flex-1 overflow-y-auto space-y-4">
          {/* TAB 1: 4 KPI CỐT LÕI */}
          {activeTab === "kpi" && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {localConfigs
                .filter((c) => c.isCoreKpi)
                .map((item) => {
                  const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";
                  const isHigher = item.direction === "HIGHER_IS_BETTER";

                  return (
                    <div
                      key={item.code}
                      className="p-3.5 rounded-xl border border-slate-200 bg-white space-y-3 hover:border-slate-300"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-xs text-slate-800">{item.name}</span>
                        <span className="font-mono text-[10px] text-slate-500 font-semibold">{item.code}</span>
                      </div>

                      <div className="grid grid-cols-2 gap-2 text-xs">
                        <div className="p-2 rounded-lg bg-emerald-50/60 border border-emerald-100 space-y-1">
                          <label className="text-[10px] font-bold text-emerald-800 flex items-center gap-1">
                            <span className="h-1.5 w-1.5 rounded-full bg-[#22c55e]" />
                            Mốc An Toàn (Xanh)
                          </label>
                          <div className="relative">
                            <Input
                              type="number"
                              step="0.01"
                              value={item.safeThreshold}
                              onChange={(e) =>
                                handleValueChange(item.code, "safeThreshold", parseFloat(e.target.value) || 0)
                              }
                              className="h-7 text-xs font-mono font-bold pr-6 bg-white"
                            />
                            <span className="absolute right-2 top-1.5 text-[10px] text-slate-400 font-bold pointer-events-none">
                              {unitSuffix}
                            </span>
                          </div>
                        </div>

                        <div className="p-2 rounded-lg bg-amber-50/60 border border-amber-100 space-y-1">
                          <label className="text-[10px] font-bold text-amber-800 flex items-center gap-1">
                            <span className="h-1.5 w-1.5 rounded-full bg-[#f59e0b]" />
                            Mốc Lưu Ý (Vàng)
                          </label>
                          <div className="relative">
                            <Input
                              type="number"
                              step="0.01"
                              value={item.warningThreshold}
                              onChange={(e) =>
                                handleValueChange(item.code, "warningThreshold", parseFloat(e.target.value) || 0)
                              }
                              className="h-7 text-xs font-mono font-bold pr-6 bg-white"
                            />
                            <span className="absolute right-2 top-1.5 text-[10px] text-slate-400 font-bold pointer-events-none">
                              {unitSuffix}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="text-[10px] text-slate-400">
                        {isHigher
                          ? `≥ ${item.safeThreshold}${unitSuffix} (Xanh) | ≥ ${item.warningThreshold}${unitSuffix} (Vàng) | < ${item.warningThreshold}${unitSuffix} (Đỏ)`
                          : `≤ ${item.safeThreshold}${unitSuffix} (Xanh) | ≤ ${item.warningThreshold}${unitSuffix} (Vàng) | > ${item.warningThreshold}${unitSuffix} (Đỏ)`}
                      </div>
                    </div>
                  );
                })}
            </div>
          )}

          {/* TAB 2: 8 TỶ SỐ TRỌNG YẾU & NGƯỠNG NGÀNH */}
          {activeTab === "key8" && (
            <div className="space-y-3">
              <div className="overflow-x-auto rounded-xl border border-slate-200">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-50 text-[10px] font-bold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                    <tr>
                      <th className="p-2.5 w-10 text-center">Chọn</th>
                      <th className="p-2.5">Chỉ số tài chính</th>
                      <th className="p-2.5">Nhóm</th>
                      <th className="p-2.5 w-40">Ngưỡng ngành (Benchmark)</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {localConfigs.map((item) => {
                      const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";

                      return (
                        <tr key={item.code} className={item.isKeyRatio ? "bg-emerald-50/20" : "hover:bg-slate-50"}>
                          <td className="p-2.5 text-center">
                            <button
                              type="button"
                              onClick={() => handleToggleKeyRatio(item.code)}
                              className="text-[#006c49]"
                            >
                              {item.isKeyRatio ? (
                                <CheckSquare className="h-4 w-4 text-[#006c49]" />
                              ) : (
                                <Square className="h-4 w-4 text-slate-300" />
                              )}
                            </button>
                          </td>
                          <td className="p-2.5">
                            <span className="font-bold text-slate-900">{item.name}</span>
                            <span className="ml-2 font-mono text-[10px] text-slate-400">{item.code}</span>
                          </td>
                          <td className="p-2.5">
                            <Badge variant="outline" className="text-[10px]">
                              {item.categoryName}
                            </Badge>
                          </td>
                          <td className="p-2.5">
                            <div className="relative max-w-[130px]">
                              <Input
                                type="number"
                                step="0.01"
                                value={item.industryBenchmark}
                                onChange={(e) =>
                                  handleValueChange(item.code, "industryBenchmark", parseFloat(e.target.value) || 0)
                                }
                                className="h-7 text-xs font-mono font-bold pr-6"
                              />
                              <span className="absolute right-2 top-1.5 text-[10px] text-slate-400 font-bold pointer-events-none">
                                {unitSuffix}
                              </span>
                            </div>
                            {item.unit === "PERCENT" && (
                              <span className="text-[9px] text-slate-400 block mt-0.5">
                                Quy đổi: {(item.industryBenchmark / 100).toFixed(2)}x
                              </span>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 3: MA TRẬN 14 CHỈ SỐ */}
          {activeTab === "matrix14" && (
            <div className="space-y-4">
              <div className="overflow-x-auto rounded-xl border border-slate-200">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-50 text-[10px] font-bold text-slate-500 uppercase tracking-wider border-b border-slate-200">
                    <tr>
                      <th className="p-2.5">Chỉ số tài chính</th>
                      <th className="p-2.5 w-28">Chiều</th>
                      <th className="p-2.5 w-32 text-emerald-800">Mốc An toàn (Xanh)</th>
                      <th className="p-2.5 w-32 text-amber-800">Mốc Lưu ý (Vàng)</th>
                      <th className="p-2.5 w-32 text-slate-800">Ngưỡng ngành</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {localConfigs.map((item) => {
                      const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";
                      const isHigher = item.direction === "HIGHER_IS_BETTER";

                      return (
                        <tr key={item.code} className="hover:bg-slate-50/70">
                          <td className="p-2.5">
                            <div className="font-bold text-slate-900">{item.name}</div>
                            <div className="font-mono text-[10px] text-slate-400">{item.formula}</div>
                          </td>
                          <td className="p-2.5">
                            <div className="flex items-center gap-1 text-[10px] font-medium text-slate-600">
                              {isHigher ? (
                                <ArrowUpRight className="h-3 w-3 text-emerald-600" />
                              ) : (
                                <ArrowDownRight className="h-3 w-3 text-rose-600" />
                              )}
                              <span>{isHigher ? "≥ tốt" : "≤ tốt"}</span>
                            </div>
                          </td>
                          <td className="p-2.5">
                            <div className="relative">
                              <Input
                                type="number"
                                step="0.01"
                                value={item.safeThreshold}
                                onChange={(e) =>
                                  handleValueChange(item.code, "safeThreshold", parseFloat(e.target.value) || 0)
                                }
                                className="h-7 text-xs font-mono font-bold pr-6 bg-emerald-50/40 border-emerald-300"
                              />
                              <span className="absolute right-2 top-1.5 text-[10px] text-emerald-700 font-bold pointer-events-none">
                                {unitSuffix}
                              </span>
                            </div>
                          </td>
                          <td className="p-2.5">
                            <div className="relative">
                              <Input
                                type="number"
                                step="0.01"
                                value={item.warningThreshold}
                                onChange={(e) =>
                                  handleValueChange(item.code, "warningThreshold", parseFloat(e.target.value) || 0)
                                }
                                className="h-7 text-xs font-mono font-bold pr-6 bg-amber-50/40 border-amber-300"
                              />
                              <span className="absolute right-2 top-1.5 text-[10px] text-amber-700 font-bold pointer-events-none">
                                {unitSuffix}
                              </span>
                            </div>
                          </td>
                          <td className="p-2.5">
                            <div className="relative">
                              <Input
                                type="number"
                                step="0.01"
                                value={item.industryBenchmark}
                                onChange={(e) =>
                                  handleValueChange(item.code, "industryBenchmark", parseFloat(e.target.value) || 0)
                                }
                                className="h-7 text-xs font-mono font-bold pr-6"
                              />
                              <span className="absolute right-2 top-1.5 text-[10px] text-slate-400 font-bold pointer-events-none">
                                {unitSuffix}
                              </span>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50/70 flex items-center justify-between">
          <div className="text-[11px] text-slate-500">
            {isSaved ? (
              <span className="text-emerald-700 font-bold flex items-center gap-1">
                <Check className="h-3.5 w-3.5" /> Đã lưu cấu hình và làm mới Dashboard!
              </span>
            ) : (
              <span>Cấu hình sẽ áp dụng ngay khi bấm Lưu.</span>
            )}
          </div>

          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={onClose} className="text-xs h-8">
              Đóng
            </Button>
            <Button
              size="sm"
              onClick={handleSave}
              className="text-xs h-8 font-bold gap-1.5 bg-[#006c49] hover:bg-[#005237] text-white px-4"
            >
              <Save className="h-3.5 w-3.5" />
              Lưu cấu hình
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

