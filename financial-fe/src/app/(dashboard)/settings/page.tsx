"use client";

import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ruleSettingsService } from "@/features/rules/services";
import {
  useRatioThresholds,
  RatioThresholdConfig,
  DEFAULT_RATIO_THRESHOLDS,
  RatioCategory,
} from "@/features/dashboard/threshold-store";
import type {
  EvaluationCriterion,
  UpdateCriterionRequest,
  ValidationRule,
  UpdateValidationRuleRequest,
  ComparisonOperator,
  FinancialUnit,
  ValidationSeverity,
} from "@/features/rules/types";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  SlidersHorizontal,
  ShieldCheck,
  Edit3,
  X,
  Save,
  Loader2,
  AlertCircle,
  CheckCircle2,
  HelpCircle,
  Power,
  RotateCw,
  LayoutGrid,
  BarChart3,
  Layers,
  RotateCcw,
  Check,
  CheckSquare,
  Square,
  ArrowUpRight,
  ArrowDownRight,
  Info,
} from "lucide-react";

export default function SettingsPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState<"criteria" | "rules">("criteria");

  // State for Editing Criterion
  const [editingCriterion, setEditingCriterion] =
    useState<EvaluationCriterion | null>(null);
  const [criterionForm, setCriterionForm] = useState<UpdateCriterionRequest>({
    criterionName: "",
    comparisonOperator: "GREATER_THAN_OR_EQUAL",
    thresholdValue: 1.5,
    unit: "TIMES",
    description: "",
    isActive: true,
  });

  // State for Editing Validation Rule
  const [editingRule, setEditingRule] = useState<ValidationRule | null>(null);
  const [ruleForm, setRuleForm] = useState<UpdateValidationRuleRequest>({
    ruleCode: "",
    ruleName: "",
    description: "",
    accountingStandard: null,
    statementType: null,
    category: "ACCOUNTING_EQUATION",
    severity: "CRITICAL",
    toleranceValue: 0,
    toleranceType: "ABSOLUTE",
    isActive: true,
  });

  const [notification, setNotification] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  // Sub-tab for Criteria (3 tabs: 4 KPI, 8 Key Ratios, 14 Matrix)
  const [criteriaSubTab, setCriteriaSubTab] = useState<"kpi" | "key8" | "matrix14">("kpi");
  const { thresholds, updateAllThresholds, resetToDefault } = useRatioThresholds();
  const [localConfigs, setLocalConfigs] = useState<RatioThresholdConfig[]>(thresholds);
  const [isDirty, setIsDirty] = useState(false);

  useEffect(() => {
    setLocalConfigs(thresholds);
    setIsDirty(false);
  }, [thresholds]);

  const handleConfigValueChange = (code: string, field: keyof RatioThresholdConfig, value: any) => {
    setLocalConfigs((prev) =>
      prev.map((c) => (c.code === code ? { ...c, [field]: value } : c))
    );
    setIsDirty(true);
  };

  const handleToggleKeyRatio = (code: string) => {
    const currentSelected = localConfigs.filter((c) => c.isKeyRatio);
    const target = localConfigs.find((c) => c.code === code);
    if (!target) return;

    if (!target.isKeyRatio && currentSelected.length >= 8) {
      setNotification({
        type: "error",
        message: "Biểu đồ so sánh tối đa 8 tỷ số trọng yếu. Vui lòng bỏ chọn một chỉ số khác trước khi chọn thêm.",
      });
      return;
    }

    setLocalConfigs((prev) =>
      prev.map((c) => (c.code === code ? { ...c, isKeyRatio: !c.isKeyRatio } : c))
    );
    setIsDirty(true);
  };

  const handleSaveAllThresholds = () => {
    updateAllThresholds(localConfigs);
    setIsDirty(false);
    queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
    setNotification({
      type: "success",
      message: "Đã lưu toàn bộ cấu hình mốc tham chiếu và cập nhật màu sắc Dashboard thành công!",
    });

    localConfigs.forEach((lc) => {
      const matched = criteria.find((c) => c.ratioCode === lc.code);
      if (matched && lc.safeThreshold !== undefined) {
        ruleSettingsService
          .updateCriterion(matched.id, {
            criterionName: matched.criterionName,
            comparisonOperator:
              lc.direction === "LOWER_IS_BETTER"
                ? "LESS_THAN_OR_EQUAL"
                : "GREATER_THAN_OR_EQUAL",
            thresholdValue: lc.safeThreshold,
            unit: lc.unit as any,
            description: lc.description,
            isActive: true,
          })
          .catch(() => {});
      }
    });
  };

  const handleResetToTT200 = () => {
    resetToDefault();
    setLocalConfigs(DEFAULT_RATIO_THRESHOLDS);
    setIsDirty(false);
    queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
    setNotification({
      type: "success",
      message: "Đã khôi phục toàn bộ 14 chỉ số tài chính về ngưỡng chuẩn mực mặc định TT200!",
    });
  };

  // 1. Query Criteria
  const {
    data: criteria = [],
    isLoading: isCriteriaLoading,
    refetch: refetchCriteria,
  } = useQuery({
    queryKey: ["evaluation-criteria"],
    queryFn: () => ruleSettingsService.listCriteria(0, 50),
  });

  // 2. Query Validation Rules
  const {
    data: rules = [],
    isLoading: isRulesLoading,
    refetch: refetchRules,
  } = useQuery({
    queryKey: ["validation-rules"],
    queryFn: () => ruleSettingsService.listValidationRules(0, 50),
  });

  // Mutation: Update Criterion
  const updateCriterionMutation = useMutation({
    mutationFn: (vars: { id: number; data: UpdateCriterionRequest }) =>
      ruleSettingsService.updateCriterion(vars.id, vars.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["evaluation-criteria"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
      setEditingCriterion(null);
      setNotification({
        type: "success",
        message: "Cập nhật tiêu chí tài chính thành công!",
      });
    },
    onError: (err: unknown) => {
      setNotification({
        type: "error",
        message:
          (err as { message?: string })?.message ||
          "Cập nhật tiêu chí thất bại",
      });
    },
  });

  // Mutation: Update Validation Rule
  const updateRuleMutation = useMutation({
    mutationFn: (vars: { id: number; data: UpdateValidationRuleRequest }) =>
      ruleSettingsService.updateValidationRule(vars.id, vars.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["validation-rules"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
      setEditingRule(null);
      setNotification({
        type: "success",
        message: "Cập nhật quy tắc kiểm định thành công!",
      });
    },
    onError: (err: unknown) => {
      setNotification({
        type: "error",
        message:
          (err as { message?: string })?.message ||
          "Cập nhật quy tắc thất bại",
      });
    },
  });

  // Mutation: Toggle Rule Status
  const toggleRuleStatusMutation = useMutation({
    mutationFn: (vars: { id: number; isActive: boolean }) =>
      ruleSettingsService.toggleValidationRuleStatus(vars.id, vars.isActive),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["validation-rules"] });
      setNotification({
        type: "success",
        message: "Cập nhật trạng thái quy tắc thành công!",
      });
    },
  });

  // Open Edit Criterion Form
  const handleOpenEditCriterion = (item: EvaluationCriterion) => {
    setNotification(null);
    setEditingCriterion(item);
    setCriterionForm({
      criterionName: item.criterionName || item.ratioName,
      comparisonOperator: item.comparisonOperator || "GREATER_THAN_OR_EQUAL",
      thresholdValue: Number(item.thresholdValue) || 0,
      unit: item.unit || "TIMES",
      description: item.description || "",
      isActive: item.isActive ?? true,
    });
  };

  // Open Edit Rule Form
  const handleOpenEditRule = (rule: ValidationRule) => {
    setNotification(null);
    setEditingRule(rule);
    setRuleForm({
      ruleCode: rule.ruleCode,
      ruleName: rule.ruleName,
      description: rule.description || "",
      accountingStandard: rule.accountingStandard || null,
      statementType: rule.statementType || null,
      category: rule.category || "ACCOUNTING_EQUATION",
      severity: rule.severity || "CRITICAL",
      toleranceValue: Number(rule.toleranceValue) || 0,
      toleranceType: rule.toleranceType || "ABSOLUTE",
      isActive: rule.isActive ?? true,
    });
  };

  const formatOperator = (op: ComparisonOperator) => {
    switch (op) {
      case "GREATER_THAN_OR_EQUAL":
        return "≥";
      case "LESS_THAN_OR_EQUAL":
        return "≤";
      case "GREATER_THAN":
        return ">";
      case "LESS_THAN":
        return "<";
      case "EQUAL":
        return "=";
      case "BETWEEN":
        return "Trong khoảng";
      default:
        return "≥";
    }
  };

  const formatUnit = (unit: FinancialUnit) => {
    switch (unit) {
      case "PERCENT":
        return "%";
      case "TIMES":
        return "x";
      case "DAYS":
        return "ngày";
      case "VND":
        return "VND";
      default:
        return "";
    }
  };

  const renderSeverityBadge = (sev: ValidationSeverity) => {
    switch (sev) {
      case "CRITICAL":
        return <Badge variant="danger">NGHIÊM TRỌNG</Badge>;
      case "HIGH":
        return <Badge variant="warning">RỦI RO CAO</Badge>;
      case "MEDIUM":
        return (
          <Badge
            variant="outline"
            className="text-amber-700 bg-amber-50 border-amber-200"
          >
            TRUNG BÌNH
          </Badge>
        );
      default:
        return <Badge variant="outline">{sev}</Badge>;
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-12">
      {/* Tiêu đề Phân hệ */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Tiêu chí Đánh giá & Quy tắc Tài chính
          </h1>
          <p className="text-sm text-slate-500">
            Cấu hình ngưỡng an toàn cho 14 chỉ số tài chính và các đẳng thức kiểm định kế toán TT200/TT133
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            size="sm"
            variant="outline"
            onClick={() => {
              refetchCriteria();
              refetchRules();
            }}
            className="text-xs font-semibold gap-1.5 h-8 text-slate-600"
          >
            <RotateCw className="h-3.5 w-3.5" />
            Làm mới
          </Button>
        </div>
      </div>

      {/* Thông báo Phản hồi */}
      {notification && (
        <div
          className={`p-3.5 rounded-xl border flex items-start gap-2.5 text-xs shadow-2xs animate-in fade-in ${
            notification.type === "success"
              ? "bg-emerald-50 border-emerald-200 text-emerald-900"
              : "bg-rose-50 border-rose-200 text-rose-800"
          }`}
        >
          {notification.type === "success" ? (
            <CheckCircle2 className="h-4 w-4 text-[#006c49] shrink-0 mt-0.5" />
          ) : (
            <AlertCircle className="h-4 w-4 text-rose-600 shrink-0 mt-0.5" />
          )}
          <span className="font-medium flex-1">{notification.message}</span>
          <button
            onClick={() => setNotification(null)}
            className="text-slate-400 hover:text-slate-600"
          >
            <X className="h-3.5 w-3.5" />
          </button>
        </div>
      )}

      {/* Thanh Tab Chuyển đổi */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-px">
        <button
          onClick={() => {
            setActiveTab("criteria");
            setEditingCriterion(null);
            setEditingRule(null);
          }}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-all ${
            activeTab === "criteria"
              ? "border-[#006c49] text-[#006c49] font-bold"
              : "border-transparent text-slate-500 hover:text-slate-900"
          }`}
        >
          <SlidersHorizontal className="h-4 w-4" />
          Tiêu chí Đánh giá Tỷ số (Health Criteria)
        </button>

        <button
          onClick={() => {
            setActiveTab("rules");
            setEditingCriterion(null);
            setEditingRule(null);
          }}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-all ${
            activeTab === "rules"
              ? "border-[#006c49] text-[#006c49] font-bold"
              : "border-transparent text-slate-500 hover:text-slate-900"
          }`}
        >
          <ShieldCheck className="h-4 w-4" />
          Quy tắc Kiểm định Đẳng thức (Validation Rules)
        </button>
      </div>

      {/* TAB 1: TIÊU CHÍ ĐÁNH GIÁ TỶ SỐ (CHỨA 3 SUB-TAB ĐIỀU CHỈNH) */}
      {activeTab === "criteria" && (
        <div className="space-y-6">
          {/* Thanh Điều hướng 3 Sub-Tab & Nút Tác vụ */}
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-white p-3 rounded-xl border border-slate-200 shadow-2xs">
            <div className="flex items-center gap-1.5 flex-wrap">
              <button
                onClick={() => setCriteriaSubTab("kpi")}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
                  criteriaSubTab === "kpi"
                    ? "bg-[#006c49] text-white shadow-xs"
                    : "text-slate-600 hover:bg-slate-100"
                }`}
              >
                <LayoutGrid className="h-3.5 w-3.5" />
                4 KPI Cốt Lõi (Xanh - Vàng - Đỏ)
              </button>

              <button
                onClick={() => setCriteriaSubTab("key8")}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
                  criteriaSubTab === "key8"
                    ? "bg-[#006c49] text-white shadow-xs"
                    : "text-slate-600 hover:bg-slate-100"
                }`}
              >
                <BarChart3 className="h-3.5 w-3.5" />
                8 Tỷ Số Trọng Yếu & Ngưỡng Ngành
              </button>

              <button
                onClick={() => setCriteriaSubTab("matrix14")}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all ${
                  criteriaSubTab === "matrix14"
                    ? "bg-[#006c49] text-white shadow-xs"
                    : "text-slate-600 hover:bg-slate-100"
                }`}
              >
                <Layers className="h-3.5 w-3.5" />
                Ma Trận 14 Chỉ Số (Xanh - Vàng - Đỏ)
              </button>
            </div>

            <div className="flex items-center gap-2 self-end md:self-center">
              <Button
                variant="outline"
                size="sm"
                onClick={handleResetToTT200}
                className="text-xs h-8 gap-1.5 text-slate-600 hover:text-slate-900 border-slate-300"
                title="Khôi phục toàn bộ 14 chỉ số về chuẩn mực mặc định TT200"
              >
                <RotateCcw className="h-3.5 w-3.5 text-slate-500" />
                Khôi phục chuẩn TT200
              </Button>

              <Button
                size="sm"
                onClick={handleSaveAllThresholds}
                className={`text-xs h-8 font-bold gap-1.5 px-4 text-white shadow-xs transition-all ${
                  isDirty
                    ? "bg-amber-600 hover:bg-amber-700 animate-pulse"
                    : "bg-[#006c49] hover:bg-[#005237]"
                }`}
              >
                <Save className="h-3.5 w-3.5" />
                {isDirty ? "Lưu thay đổi *" : "Đã lưu cấu hình"}
              </Button>
            </div>
          </div>

          {/* SUB-TAB 1: 4 CHỈ SỐ KPI CỐT LÕI */}
          {criteriaSubTab === "kpi" && (
            <div className="space-y-4">
              <div className="p-3.5 rounded-xl bg-emerald-50/60 border border-emerald-100 text-xs text-emerald-900 flex items-start gap-2">
                <Info className="h-4 w-4 text-[#006c49] shrink-0 mt-0.5" />
                <div>
                  <span className="font-bold">4 Chỉ số KPI Cốt lõi (Scoreboard điều hành):</span> Thiết lập 2 mốc ngưỡng để hệ thống tự động phân loại 3 màu: <strong>Xanh (An toàn)</strong>, <strong>Vàng (Lưu ý)</strong>, <strong>Đỏ (Cảnh báo)</strong>. Các mốc này được đồng bộ trực tiếp với Ma trận 14 chỉ số.
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {localConfigs
                  .filter((c) => c.isCoreKpi)
                  .map((item) => {
                    const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";
                    const isHigher = item.direction === "HIGHER_IS_BETTER";

                    return (
                      <Card key={item.code} className="bg-white border border-slate-200 shadow-xs hover:border-[#006c49]/40 transition-colors">
                        <CardHeader className="pb-3 border-b border-slate-100">
                          <div className="flex items-start justify-between gap-2">
                            <div>
                              <Badge variant="outline" className="text-[10px] uppercase font-bold text-slate-500 mb-1">
                                {item.categoryName}
                              </Badge>
                              <CardTitle className="text-sm font-bold text-slate-900">
                                {item.name}
                              </CardTitle>
                              <p className="text-[11px] font-mono text-slate-400 mt-0.5">
                                {item.formula}
                              </p>
                            </div>

                            <span className="font-mono text-[11px] px-2 py-0.5 rounded bg-slate-100 text-slate-600 font-bold">
                              {item.code}
                            </span>
                          </div>
                        </CardHeader>

                        <CardContent className="p-4 space-y-4 text-xs">
                          <div className="grid grid-cols-2 gap-3">
                            <div className="space-y-1.5 p-2.5 rounded-lg bg-emerald-50/50 border border-emerald-200/60">
                              <label className="font-bold text-emerald-900 flex items-center gap-1.5 text-[11px]">
                                <span className="h-2 w-2 rounded-full bg-[#22c55e]" />
                                Mốc An Toàn (Xanh)
                              </label>
                              <div className="relative">
                                <Input
                                  type="number"
                                  step="0.01"
                                  value={item.safeThreshold}
                                  onChange={(e) =>
                                    handleConfigValueChange(
                                      item.code,
                                      "safeThreshold",
                                      parseFloat(e.target.value) || 0
                                    )
                                  }
                                  className="h-8 text-xs font-mono font-bold pr-7 bg-white border-emerald-300 focus-visible:ring-emerald-500"
                                />
                                <span className="absolute right-2 top-2 text-[11px] text-slate-400 font-bold pointer-events-none">
                                  {unitSuffix}
                                </span>
                              </div>
                              <span className="text-[10px] text-emerald-700 block">
                                {isHigher ? `Đạt từ ≥ ${item.safeThreshold}${unitSuffix}` : `Tối đa ≤ ${item.safeThreshold}${unitSuffix}`}
                              </span>
                            </div>

                            <div className="space-y-1.5 p-2.5 rounded-lg bg-amber-50/50 border border-amber-200/60">
                              <label className="font-bold text-amber-900 flex items-center gap-1.5 text-[11px]">
                                <span className="h-2 w-2 rounded-full bg-[#f59e0b]" />
                                Mốc Lưu Ý (Vàng)
                              </label>
                              <div className="relative">
                                <Input
                                  type="number"
                                  step="0.01"
                                  value={item.warningThreshold}
                                  onChange={(e) =>
                                    handleConfigValueChange(
                                      item.code,
                                      "warningThreshold",
                                      parseFloat(e.target.value) || 0
                                    )
                                  }
                                  className="h-8 text-xs font-mono font-bold pr-7 bg-white border-amber-300 focus-visible:ring-amber-500"
                                />
                                <span className="absolute right-2 top-2 text-[11px] text-slate-400 font-bold pointer-events-none">
                                  {unitSuffix}
                                </span>
                              </div>
                              <span className="text-[10px] text-amber-700 block">
                                {isHigher ? `Cận chuẩn ≥ ${item.warningThreshold}${unitSuffix}` : `Cận chuẩn ≤ ${item.warningThreshold}${unitSuffix}`}
                              </span>
                            </div>
                          </div>

                          <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px]">
                            <div className="text-slate-500">
                              Dưới mức lưu ý:{" "}
                              <span className="font-bold text-rose-600">
                                {isHigher ? `< ${item.warningThreshold}${unitSuffix} (Cảnh báo)` : `> ${item.warningThreshold}${unitSuffix} (Cảnh báo)`}
                              </span>
                            </div>

                            <div className="flex items-center gap-1">
                              <Badge variant="safe" className="text-[9px] px-1.5 py-0">XANH</Badge>
                              <Badge variant="warning" className="text-[9px] px-1.5 py-0">VÀNG</Badge>
                              <Badge variant="danger" className="text-[9px] px-1.5 py-0">ĐỎ</Badge>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
              </div>
            </div>
          )}

          {/* SUB-TAB 2: 8 TỶ SỐ TRỌNG YẾU & NGƯỠNG NGÀNH */}
          {criteriaSubTab === "key8" && (
            <div className="space-y-4">
              {(() => {
                const selectedCount = localConfigs.filter((c) => c.isKeyRatio).length;
                return (
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs">
                    <div className="space-y-0.5">
                      <div className="font-bold text-slate-800">
                        Cấu hình Biểu đồ so sánh 8 tỷ số vs Ngưỡng an toàn ngành
                      </div>
                      <p className="text-[11px] text-slate-500">
                        Chọn 8 tỷ số tài chính tiêu biểu để hiển thị trên biểu đồ nhóm cột và nhập giá trị Ngưỡng ngành (Benchmark) tham chiếu.
                      </p>
                    </div>

                    <Badge
                      variant={selectedCount === 8 ? "safe" : "warning"}
                      className="text-xs font-mono font-bold shrink-0 self-start sm:self-center"
                    >
                      Đã chọn: {selectedCount}/8 chỉ số
                    </Badge>
                  </div>
                );
              })()}

              <Card className="bg-white border border-slate-200 shadow-xs overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-left">
                    <thead className="bg-slate-50 text-[11px] font-bold text-slate-600 uppercase tracking-wider border-b border-slate-200">
                      <tr>
                        <th className="p-3 w-12 text-center">Hiển thị</th>
                        <th className="p-3">Tỷ số tài chính</th>
                        <th className="p-3">Nhóm</th>
                        <th className="p-3">Công thức</th>
                        <th className="p-3 w-44">Ngưỡng ngành (Benchmark)</th>
                        <th className="p-3 text-right">Trạng thái biểu đồ</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {localConfigs.map((item) => {
                        const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";

                        return (
                          <tr
                            key={item.code}
                            className={`transition-colors ${
                              item.isKeyRatio ? "bg-emerald-50/20 hover:bg-emerald-50/40" : "hover:bg-slate-50/60"
                            }`}
                          >
                            <td className="p-3 text-center">
                              <button
                                type="button"
                                onClick={() => handleToggleKeyRatio(item.code)}
                                className="text-[#006c49] hover:opacity-80 transition-opacity"
                              >
                                {item.isKeyRatio ? (
                                  <CheckSquare className="h-5 w-5 text-[#006c49]" />
                                ) : (
                                  <Square className="h-5 w-5 text-slate-300 hover:text-slate-400" />
                                )}
                              </button>
                            </td>

                            <td className="p-3">
                              <div className="font-bold text-slate-900">{item.name}</div>
                              <div className="font-mono text-[10px] text-slate-400">{item.code}</div>
                            </td>

                            <td className="p-3">
                              <Badge variant="outline" className="text-[10px]">
                                {item.categoryName}
                              </Badge>
                            </td>

                            <td className="p-3 text-slate-500 font-mono text-[11px]">
                              {item.formula}
                            </td>

                            <td className="p-3">
                              <div className="relative max-w-[150px]">
                                <Input
                                  type="number"
                                  step="0.01"
                                  value={item.industryBenchmark}
                                  onChange={(e) =>
                                    handleConfigValueChange(
                                      item.code,
                                      "industryBenchmark",
                                      parseFloat(e.target.value) || 0
                                    )
                                  }
                                  className="h-8 text-xs font-mono font-bold pr-7 bg-white"
                                />
                                <span className="absolute right-2 top-2 text-[11px] text-slate-400 font-bold pointer-events-none">
                                  {unitSuffix}
                                </span>
                              </div>
                            </td>

                            <td className="p-3 text-right">
                              {item.isKeyRatio ? (
                                <Badge variant="safe" className="text-[10px]">
                                  HIỂN THỊ
                                </Badge>
                              ) : (
                                <span className="text-[11px] text-slate-400">Ẩn</span>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </Card>
            </div>
          )}

          {/* SUB-TAB 3: MA TRẬN CẢNH BÁO 14 CHỈ SỐ */}
          {criteriaSubTab === "matrix14" && (
            <div className="space-y-6">
              <div className="p-3.5 rounded-xl bg-blue-50/60 border border-blue-100 text-xs text-blue-900 flex items-start gap-2">
                <Info className="h-4 w-4 text-blue-600 shrink-0 mt-0.5" />
                <div>
                  <span className="font-bold">Ma trận Cảnh báo 14 Chỉ số (Toàn diện TT200):</span> Quản lý đồng bộ cả 14 chỉ số tài chính thuộc 4 nhóm. Thiết lập ngưỡng An toàn (Xanh) và ngưỡng Lưu ý (Vàng) giúp Dashboard hiển thị đúng màu sắc cho từng chỉ số.
                </div>
              </div>

              {(
                [
                  { key: "LIQUIDITY", title: "1. NHÓM THANH KHOẢN (LIQUIDITY)" },
                  { key: "SOLVENCY", title: "2. NHÓM ĐÒN BẨY & NỢ (SOLVENCY)" },
                  { key: "PROFITABILITY", title: "3. NHÓM HIỆU SUẤT SINH LỜI (PROFITABILITY)" },
                  { key: "EFFICIENCY", title: "4. NHÓM HIỆU QUẢ HOẠT ĐỘNG (EFFICIENCY)" },
                ] as const
              ).map((group) => {
                const groupItems = localConfigs.filter((c) => c.category === group.key);

                return (
                  <Card key={group.key} className="bg-white border border-slate-200 shadow-xs overflow-hidden">
                    <CardHeader className="bg-slate-50/70 border-b border-slate-200/80 py-2.5 px-4 flex flex-row items-center justify-between">
                      <CardTitle className="text-xs font-bold uppercase tracking-wider text-slate-700">
                        {group.title}
                      </CardTitle>
                      <Badge variant="outline" className="text-[10px]">
                        {groupItems.length} chỉ số
                      </Badge>
                    </CardHeader>

                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left">
                        <thead className="bg-slate-50 text-[10px] font-bold text-slate-500 uppercase tracking-wider border-b border-slate-100">
                          <tr>
                            <th className="p-3">Tỷ số & Công thức</th>
                            <th className="p-3 w-32">Hướng đánh giá</th>
                            <th className="p-3 w-36 text-emerald-800">Ngưỡng An Toàn (Xanh)</th>
                            <th className="p-3 w-36 text-amber-800">Ngưỡng Lưu Ý (Vàng)</th>
                            <th className="p-3 w-36 text-slate-800">Ngưỡng Ngành</th>
                            <th className="p-3 text-right">Phân loại dải màu</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {groupItems.map((item) => {
                            const unitSuffix = item.unit === "PERCENT" ? "%" : item.unit === "DAYS" ? " ngày" : "x";
                            const isHigher = item.direction === "HIGHER_IS_BETTER";

                            return (
                              <tr key={item.code} className="hover:bg-slate-50/60 transition-colors">
                                <td className="p-3">
                                  <div className="font-bold text-slate-900">{item.name}</div>
                                  <div className="flex items-center gap-2 mt-0.5">
                                    <span className="font-mono text-[10px] text-slate-400">{item.code}</span>
                                    <span className="text-slate-300">•</span>
                                    <span className="font-mono text-[10px] text-slate-500">{item.formula}</span>
                                  </div>
                                </td>

                                <td className="p-3">
                                  <div className="flex items-center gap-1 text-[11px] font-medium text-slate-600">
                                    {isHigher ? (
                                      <>
                                        <ArrowUpRight className="h-3.5 w-3.5 text-emerald-600" />
                                        <span>Càng cao càng tốt</span>
                                      </>
                                    ) : (
                                      <>
                                        <ArrowDownRight className="h-3.5 w-3.5 text-rose-600" />
                                        <span>Càng thấp càng tốt</span>
                                      </>
                                    )}
                                  </div>
                                </td>

                                <td className="p-3">
                                  <div className="relative">
                                    <Input
                                      type="number"
                                      step="0.01"
                                      value={item.safeThreshold}
                                      onChange={(e) =>
                                        handleConfigValueChange(
                                          item.code,
                                          "safeThreshold",
                                          parseFloat(e.target.value) || 0
                                        )
                                      }
                                      className="h-8 text-xs font-mono font-bold pr-7 bg-emerald-50/30 border-emerald-300 focus-visible:ring-emerald-500"
                                    />
                                    <span className="absolute right-2 top-2 text-[11px] text-emerald-700 font-bold pointer-events-none">
                                      {unitSuffix}
                                    </span>
                                  </div>
                                </td>

                                <td className="p-3">
                                  <div className="relative">
                                    <Input
                                      type="number"
                                      step="0.01"
                                      value={item.warningThreshold}
                                      onChange={(e) =>
                                        handleConfigValueChange(
                                          item.code,
                                          "warningThreshold",
                                          parseFloat(e.target.value) || 0
                                        )
                                      }
                                      className="h-8 text-xs font-mono font-bold pr-7 bg-amber-50/30 border-amber-300 focus-visible:ring-amber-500"
                                    />
                                    <span className="absolute right-2 top-2 text-[11px] text-amber-700 font-bold pointer-events-none">
                                      {unitSuffix}
                                    </span>
                                  </div>
                                </td>

                                <td className="p-3">
                                  <div className="relative">
                                    <Input
                                      type="number"
                                      step="0.01"
                                      value={item.industryBenchmark}
                                      onChange={(e) =>
                                        handleConfigValueChange(
                                          item.code,
                                          "industryBenchmark",
                                          parseFloat(e.target.value) || 0
                                        )
                                      }
                                      className="h-8 text-xs font-mono font-bold pr-7 bg-white"
                                    />
                                    <span className="absolute right-2 top-2 text-[11px] text-slate-400 font-bold pointer-events-none">
                                      {unitSuffix}
                                    </span>
                                  </div>
                                </td>

                                <td className="p-3 text-right">
                                  <div className="flex items-center justify-end gap-1">
                                    <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-[#22c55e] text-white">
                                      {isHigher ? `≥ ${item.safeThreshold}` : `≤ ${item.safeThreshold}`}
                                    </span>
                                    <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-[#f59e0b] text-white">
                                      {isHigher ? `≥ ${item.warningThreshold}` : `≤ ${item.warningThreshold}`}
                                    </span>
                                    <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-[#ef4444] text-white">
                                      {isHigher ? `< ${item.warningThreshold}` : `> ${item.warningThreshold}`}
                                    </span>
                                  </div>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: QUY TẮC KIỂM ĐỊNH KẾ TOÁN */}
      {activeTab === "rules" && (
        <div className="space-y-6">
          {/* Card Form Chỉnh sửa Quy tắc */}
          {editingRule && (
            <Card className="bg-white border-2 border-[#006c49]/40 shadow-sm animate-in fade-in">
              <CardHeader className="bg-emerald-50/50 border-b border-emerald-100 pb-3 flex flex-row items-center justify-between">
                <div>
                  <CardTitle className="text-sm font-bold text-[#006c49]">
                    Hiệu chỉnh Quy tắc Kiểm định: {editingRule.ruleName}
                  </CardTitle>
                  <CardDescription className="text-xs">
                    Mã quy tắc:{" "}
                    <code className="font-mono font-bold text-slate-800">
                      {editingRule.ruleCode}
                    </code>
                  </CardDescription>
                </div>

                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => setEditingRule(null)}
                  className="h-7 text-xs text-slate-500"
                >
                  <X className="h-4 w-4 mr-1" /> Hủy
                </Button>
              </CardHeader>

              <CardContent className="p-5 space-y-4 text-xs">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="space-y-1 sm:col-span-2">
                    <label className="font-semibold text-slate-700">
                      Tên quy tắc kiểm định
                    </label>
                    <Input
                      value={ruleForm.ruleName}
                      onChange={(e) =>
                        setRuleForm({
                          ...ruleForm,
                          ruleName: e.target.value,
                        })
                      }
                      className="text-xs h-9"
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Mức độ vi phạm (Severity)
                    </label>
                    <select
                      value={ruleForm.severity}
                      onChange={(e) =>
                        setRuleForm({
                          ...ruleForm,
                          severity: e.target.value as ValidationSeverity,
                        })
                      }
                      className="flex h-9 w-full rounded-lg border border-input bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-[#006c49]"
                    >
                      <option value="CRITICAL">NGHIÊM TRỌNG (Critical)</option>
                      <option value="HIGH">CAO (High)</option>
                      <option value="MEDIUM">TRUNG BÌNH (Medium)</option>
                      <option value="LOW">THẤP (Low)</option>
                    </select>
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Sai số cho phép (Tolerance Value)
                    </label>
                    <Input
                      type="number"
                      step="1"
                      value={ruleForm.toleranceValue}
                      onChange={(e) =>
                        setRuleForm({
                          ...ruleForm,
                          toleranceValue: parseFloat(e.target.value) || 0,
                        })
                      }
                      className="text-xs h-9 font-mono font-bold"
                    />
                    <p className="text-[10px] text-slate-400">
                      Chênh lệch tối đa cho phép (VND) để coi là cân đối
                    </p>
                  </div>

                  <div className="space-y-1 sm:col-span-2">
                    <label className="font-semibold text-slate-700">
                      Mô tả & Căn cứ chuẩn mực kế toán
                    </label>
                    <Input
                      value={ruleForm.description || ""}
                      onChange={(e) =>
                        setRuleForm({
                          ...ruleForm,
                          description: e.target.value,
                        })
                      }
                      placeholder="Mô tả đẳng thức kế toán theo TT200/TT133..."
                      className="text-xs h-9"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2">
                  <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700">
                    <input
                      type="checkbox"
                      checked={ruleForm.isActive}
                      onChange={(e) =>
                        setRuleForm({
                          ...ruleForm,
                          isActive: e.target.checked,
                        })
                      }
                      className="rounded border-slate-300 text-[#006c49] focus:ring-[#006c49] h-4 w-4"
                    />
                    Kích hoạt quy tắc kiểm định này
                  </label>

                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setEditingRule(null)}
                      className="text-xs h-8"
                    >
                      Hủy bỏ
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => {
                        if (editingRule) {
                          updateRuleMutation.mutate({
                            id: editingRule.id,
                            data: ruleForm,
                          });
                        }
                      }}
                      disabled={updateRuleMutation.isPending}
                      className="bg-[#006c49] hover:bg-[#005237] text-white text-xs h-8 font-semibold gap-1 px-4 shadow-xs"
                    >
                      {updateRuleMutation.isPending ? (
                        <>
                          <Loader2 className="h-3.5 w-3.5 animate-spin mr-1" />
                          Đang lưu...
                        </>
                      ) : (
                        <>
                          <Save className="h-3.5 w-3.5 mr-1" />
                          Lưu quy tắc
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Danh sách Quy tắc */}
          <Card className="bg-white border border-slate-200 shadow-xs">
            <CardHeader className="border-b border-slate-100 pb-3 flex flex-row items-center justify-between">
              <div>
                <CardTitle className="text-sm font-semibold text-slate-900">
                  Hệ thống 7 Đẳng thức Cân đối TT200 / TT133
                </CardTitle>
                <CardDescription className="text-xs">
                  Tự động kiểm định đẳng thức cân đối kế toán sau khi bóc tách OCR
                </CardDescription>
              </div>
            </CardHeader>

            <CardContent className="p-0">
              {isRulesLoading ? (
                <div className="p-6 space-y-3">
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              ) : rules.length === 0 ? (
                <div className="p-8 text-center text-slate-400 text-xs">
                  Không có quy tắc kiểm định nào.
                </div>
              ) : (
                <div className="divide-y divide-slate-100 text-xs">
                  {rules.map((rule) => (
                    <div
                      key={rule.id}
                      className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-slate-50/80 transition-colors"
                    >
                      <div className="space-y-1 min-w-0 flex-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="font-bold text-slate-900 text-sm">
                            {rule.ruleName}
                          </span>
                          <span className="font-mono text-[11px] px-2 py-0.5 rounded bg-slate-100 text-slate-600 font-semibold">
                            {rule.ruleCode}
                          </span>
                          {rule.accountingStandard && (
                            <Badge variant="outline" className="text-[10px] font-mono">
                              {rule.accountingStandard}
                            </Badge>
                          )}
                          {renderSeverityBadge(rule.severity)}
                        </div>
                        <p className="text-slate-500 text-xs leading-relaxed">
                          {rule.description || "Đẳng thức kiểm tra tự động"}
                        </p>
                      </div>

                      <div className="flex items-center gap-3 shrink-0 self-end sm:self-center">
                        <div className="text-right">
                          <div className="text-[11px] text-slate-400 font-medium">
                            Sai số cho phép
                          </div>
                          <div className="font-mono font-bold text-xs text-slate-800">
                            ± {Number(rule.toleranceValue).toLocaleString("vi-VN")} VND
                          </div>
                        </div>

                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleOpenEditRule(rule)}
                          className="h-8 text-xs font-semibold text-[#006c49] border-emerald-200 hover:bg-emerald-50 gap-1 px-3"
                        >
                          <Edit3 className="h-3.5 w-3.5" />
                          Sửa
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
