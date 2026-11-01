"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ruleSettingsService } from "@/features/rules/services";
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

      {/* TAB 1: TIÊU CHÍ ĐÁNH GIÁ TỶ SỐ */}
      {activeTab === "criteria" && (
        <div className="space-y-6">
          {/* Card Form Chỉnh sửa Tiêu chí */}
          {editingCriterion && (
            <Card className="bg-white border-2 border-[#006c49]/40 shadow-sm animate-in fade-in">
              <CardHeader className="bg-emerald-50/50 border-b border-emerald-100 pb-3 flex flex-row items-center justify-between">
                <div>
                  <CardTitle className="text-sm font-bold text-[#006c49]">
                    Hiệu chỉnh Ngưỡng Tiêu chí: {editingCriterion.ratioName}
                  </CardTitle>
                  <CardDescription className="text-xs">
                    Mã tỷ số:{" "}
                    <code className="font-mono font-bold text-slate-800">
                      {editingCriterion.ratioCode}
                    </code>
                  </CardDescription>
                </div>

                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => setEditingCriterion(null)}
                  className="h-7 text-xs text-slate-500"
                >
                  <X className="h-4 w-4 mr-1" /> Hủy
                </Button>
              </CardHeader>

              <CardContent className="p-5 space-y-4 text-xs">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Tên tiêu chí hiển thị
                    </label>
                    <Input
                      value={criterionForm.criterionName}
                      onChange={(e) =>
                        setCriterionForm({
                          ...criterionForm,
                          criterionName: e.target.value,
                        })
                      }
                      className="text-xs h-9"
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Toán tử so sánh
                    </label>
                    <select
                      value={criterionForm.comparisonOperator}
                      onChange={(e) =>
                        setCriterionForm({
                          ...criterionForm,
                          comparisonOperator: e.target
                            .value as ComparisonOperator,
                        })
                      }
                      className="flex h-9 w-full rounded-lg border border-input bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-[#006c49]"
                    >
                      <option value="GREATER_THAN_OR_EQUAL">
                        Lớn hơn hoặc bằng (≥)
                      </option>
                      <option value="LESS_THAN_OR_EQUAL">
                        Nhỏ hơn hoặc bằng (≤)
                      </option>
                      <option value="GREATER_THAN">Lớn hơn (&gt;)</option>
                      <option value="LESS_THAN">Nhỏ hơn (&lt;)</option>
                      <option value="EQUAL">Bằng (=)</option>
                    </select>
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Ngưỡng an toàn ({formatUnit(criterionForm.unit)})
                    </label>
                    <Input
                      type="number"
                      step="0.01"
                      value={criterionForm.thresholdValue}
                      onChange={(e) =>
                        setCriterionForm({
                          ...criterionForm,
                          thresholdValue: parseFloat(e.target.value) || 0,
                        })
                      }
                      className="text-xs h-9 font-mono font-bold"
                    />
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="font-semibold text-slate-700">
                    Ý nghĩa kinh tế & Hướng dẫn nhận định
                  </label>
                  <Input
                    value={criterionForm.description || ""}
                    onChange={(e) =>
                      setCriterionForm({
                        ...criterionForm,
                        description: e.target.value,
                      })
                    }
                    placeholder="Mô tả ý nghĩa của ngưỡng an toàn đối với doanh nghiệp..."
                    className="text-xs h-9"
                  />
                </div>

                <div className="flex items-center justify-between pt-2">
                  <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700">
                    <input
                      type="checkbox"
                      checked={criterionForm.isActive}
                      onChange={(e) =>
                        setCriterionForm({
                          ...criterionForm,
                          isActive: e.target.checked,
                        })
                      }
                      className="rounded border-slate-300 text-[#006c49] focus:ring-[#006c49] h-4 w-4"
                    />
                    Kích hoạt tiêu chí này trong đánh giá sức khỏe tài chính
                  </label>

                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setEditingCriterion(null)}
                      className="text-xs h-8"
                    >
                      Hủy bỏ
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => {
                        if (editingCriterion) {
                          updateCriterionMutation.mutate({
                            id: editingCriterion.id,
                            data: criterionForm,
                          });
                        }
                      }}
                      disabled={updateCriterionMutation.isPending}
                      className="bg-[#006c49] hover:bg-[#005237] text-white text-xs h-8 font-semibold gap-1 px-4 shadow-xs"
                    >
                      {updateCriterionMutation.isPending ? (
                        <>
                          <Loader2 className="h-3.5 w-3.5 animate-spin mr-1" />
                          Đang lưu...
                        </>
                      ) : (
                        <>
                          <Save className="h-3.5 w-3.5 mr-1" />
                          Lưu tiêu chí
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Danh sách Tiêu chí */}
          <Card className="bg-white border border-slate-200 shadow-xs">
            <CardHeader className="border-b border-slate-100 pb-3 flex flex-row items-center justify-between">
              <div>
                <CardTitle className="text-sm font-semibold text-slate-900">
                  Danh mục Ngưỡng An toàn Chỉ số Tài chính
                </CardTitle>
                <CardDescription className="text-xs">
                  Áp dụng trực tiếp cho 4 KPI sống còn và Ma trận rủi ro trên Dashboard
                </CardDescription>
              </div>
            </CardHeader>

            <CardContent className="p-0">
              {isCriteriaLoading ? (
                <div className="p-6 space-y-3">
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              ) : criteria.length === 0 ? (
                <div className="p-8 text-center text-slate-400 text-xs">
                  Không có tiêu chí đánh giá nào.
                </div>
              ) : (
                <div className="divide-y divide-slate-100 text-xs">
                  {criteria.map((item) => (
                    <div
                      key={item.id}
                      className="p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-slate-50/80 transition-colors"
                    >
                      <div className="space-y-1 min-w-0 flex-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="font-bold text-slate-900 text-sm">
                            {item.ratioName}
                          </span>
                          <span className="font-mono text-[11px] px-2 py-0.5 rounded bg-slate-100 text-slate-600 font-semibold">
                            {item.ratioCode}
                          </span>
                          {item.isActive ? (
                            <Badge variant="safe" className="text-[10px]">
                              HOẠT ĐỘNG
                            </Badge>
                          ) : (
                            <Badge variant="outline" className="text-[10px] text-slate-400">
                              TẠM DỪNG
                            </Badge>
                          )}
                        </div>
                        <p className="text-slate-500 text-xs leading-relaxed">
                          {item.description || item.criterionName}
                        </p>
                      </div>

                      <div className="flex items-center gap-3 shrink-0 self-end sm:self-center">
                        <div className="text-right">
                          <div className="text-[11px] text-slate-400 font-medium">
                            Ngưỡng an toàn
                          </div>
                          <div className="font-mono font-bold text-sm text-[#006c49]">
                            {formatOperator(item.comparisonOperator)}{" "}
                            {item.thresholdValue}{" "}
                            {formatUnit(item.unit)}
                          </div>
                        </div>

                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleOpenEditCriterion(item)}
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
