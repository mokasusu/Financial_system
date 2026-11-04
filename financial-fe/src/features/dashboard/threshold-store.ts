import { useState, useEffect, useCallback } from "react";

export type RatioCategory = "LIQUIDITY" | "SOLVENCY" | "PROFITABILITY" | "EFFICIENCY";
export type EvaluationDirection = "HIGHER_IS_BETTER" | "LOWER_IS_BETTER";
export type RatioHealthStatus = "SAFE" | "WARNING" | "DANGER" | "INSUFFICIENT_DATA";

export interface RatioThresholdConfig {
  code: string;
  name: string;
  shortName: string;
  category: RatioCategory;
  categoryName: string;
  unit: "TIMES" | "PERCENT" | "DAYS";
  direction: EvaluationDirection;
  safeThreshold: number;       // Mốc Xanh (An toàn)
  warningThreshold: number;    // Mốc Vàng (Lưu ý)
  industryBenchmark: number;   // Ngưỡng ngành cho biểu đồ 8 tỷ số
  isCoreKpi: boolean;          // Thuộc 4 KPI cốt lõi
  isKeyRatio: boolean;         // Thuộc 8 tỷ số trọng yếu hiển thị biểu đồ
  formula: string;
  description: string;
}

export const DEFAULT_RATIO_THRESHOLDS: RatioThresholdConfig[] = [
  // --- 1. NHÓM THANH KHOẢN (LIQUIDITY) ---
  {
    code: "CURRENT_RATIO",
    name: "Khả năng thanh toán hiện hành",
    shortName: "Thanh toán hiện hành",
    category: "LIQUIDITY",
    categoryName: "Thanh khoản",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 1.5,
    warningThreshold: 1.0,
    industryBenchmark: 1.5,
    isCoreKpi: true,
    isKeyRatio: true,
    formula: "TSNH / Nợ ngắn hạn",
    description: "Đo lường năng lực thanh toán nợ ngắn hạn bằng tài sản ngắn hạn",
  },
  {
    code: "QUICK_RATIO",
    name: "Khả năng thanh toán nhanh",
    shortName: "Thanh toán nhanh",
    category: "LIQUIDITY",
    categoryName: "Thanh khoản",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 1.0,
    warningThreshold: 0.7,
    industryBenchmark: 1.0,
    isCoreKpi: false,
    isKeyRatio: true,
    formula: "(TSNH - Hàng tồn kho) / Nợ ngắn hạn",
    description: "Khả năng hoàn trả nợ ngắn hạn mà không cần thanh lý hàng tồn kho",
  },
  {
    code: "CASH_RATIO",
    name: "Khả năng thanh toán tức thời",
    shortName: "Thanh toán tiền mặt",
    category: "LIQUIDITY",
    categoryName: "Thanh khoản",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 0.5,
    warningThreshold: 0.2,
    industryBenchmark: 0.5,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "Tiền & Tương đương tiền / Nợ ngắn hạn",
    description: "Khả năng chi trả nợ đến hạn ngay lập tức bằng tiền mặt hiện có",
  },

  // --- 2. NHÓM ĐÒN BẨY & NỢ (SOLVENCY) ---
  {
    code: "DEBT_TO_EQUITY",
    name: "Hệ số nợ trên vốn chủ sở hữu (D/E)",
    shortName: "Nợ / Vốn CSH",
    category: "SOLVENCY",
    categoryName: "Đòn bẩy & Khả năng trả nợ",
    unit: "TIMES",
    direction: "LOWER_IS_BETTER",
    safeThreshold: 1.0,
    warningThreshold: 1.5,
    industryBenchmark: 1.0,
    isCoreKpi: true,
    isKeyRatio: true,
    formula: "Nợ phải trả / Vốn chủ sở hữu",
    description: "Mức độ đòn bẩy tài chính và tỷ lệ tài trợ bằng nợ vay so với vốn chủ",
  },
  {
    code: "DEBT_RATIO",
    name: "Hệ số nợ trên tổng tài sản",
    shortName: "Hệ số nợ",
    category: "SOLVENCY",
    categoryName: "Đòn bẩy & Khả năng trả nợ",
    unit: "PERCENT",
    direction: "LOWER_IS_BETTER",
    safeThreshold: 50.0,
    warningThreshold: 65.0,
    industryBenchmark: 50.0,
    isCoreKpi: false,
    isKeyRatio: true,
    formula: "Nợ phải trả / Tổng tài sản",
    description: "Tỷ trọng nợ phải trả trong tổng cơ cấu nguồn vốn tài sản của doanh nghiệp",
  },
  {
    code: "EQUITY_RATIO",
    name: "Hệ số tự tài trợ (Vốn CSH)",
    shortName: "Hệ số vốn CSH",
    category: "SOLVENCY",
    categoryName: "Đòn bẩy & Khả năng trả nợ",
    unit: "PERCENT",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 40.0,
    warningThreshold: 25.0,
    industryBenchmark: 40.0,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "Vốn chủ sở hữu / Tổng tài sản",
    description: "Mức độ tự chủ tài chính từ nguồn vốn chủ sở hữu của doanh nghiệp",
  },

  // --- 3. NHÓM HIỆU SUẤT SINH LỜI (PROFITABILITY) ---
  {
    code: "ROA",
    name: "Tỷ suất sinh lời trên tổng tài sản (ROA)",
    shortName: "ROA (Sinh lời tài sản)",
    category: "PROFITABILITY",
    categoryName: "Hiệu suất sinh lời",
    unit: "PERCENT",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 5.0,
    warningThreshold: 2.5,
    industryBenchmark: 5.0,
    isCoreKpi: true,
    isKeyRatio: true,
    formula: "LNST / Tổng tài sản",
    description: "Hiệu quả tạo ra lợi nhuận ròng trên mỗi đồng tài sản đầu tư",
  },
  {
    code: "ROE",
    name: "Tỷ suất sinh lời trên vốn chủ sở hữu (ROE)",
    shortName: "ROE (Sinh lời vốn CSH)",
    category: "PROFITABILITY",
    categoryName: "Hiệu suất sinh lời",
    unit: "PERCENT",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 12.0,
    warningThreshold: 8.0,
    industryBenchmark: 12.0,
    isCoreKpi: true,
    isKeyRatio: true,
    formula: "LNST / Vốn chủ sở hữu",
    description: "Tỷ suất lợi nhuận thu về cho các cổ đông và chủ sở hữu doanh nghiệp",
  },
  {
    code: "NET_PROFIT_MARGIN",
    name: "Tỷ suất lợi nhuận ròng (Net Margin)",
    shortName: "Biên lợi nhuận ròng",
    category: "PROFITABILITY",
    categoryName: "Hiệu suất sinh lời",
    unit: "PERCENT",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 8.0,
    warningThreshold: 4.0,
    industryBenchmark: 8.0,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "LNST / Doanh thu thuần",
    description: "Tỷ lệ lợi nhuận sau thuế giữ lại được trên mỗi đồng doanh thu thuần",
  },
  {
    code: "GROSS_PROFIT_MARGIN",
    name: "Tỷ suất lợi nhuận gộp (Gross Margin)",
    shortName: "Biên lợi nhuận gộp",
    category: "PROFITABILITY",
    categoryName: "Hiệu suất sinh lời",
    unit: "PERCENT",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 20.0,
    warningThreshold: 12.0,
    industryBenchmark: 20.0,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "Lợi nhuận gộp / Doanh thu thuần",
    description: "Năng lực sinh lời gộp sau khi trừ chi phí giá vốn hàng bán",
  },

  // --- 4. NHÓM HIỆU QUẢ HOẠT ĐỘNG (EFFICIENCY) ---
  {
    code: "ASSET_TURNOVER",
    name: "Vòng quay tổng tài sản",
    shortName: "Vòng quay tài sản",
    category: "EFFICIENCY",
    categoryName: "Hiệu quả hoạt động",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 1.2,
    warningThreshold: 0.8,
    industryBenchmark: 1.2,
    isCoreKpi: false,
    isKeyRatio: true,
    formula: "Doanh thu thuần / Tổng tài sản",
    description: "Số vòng quay tài sản luân chuyển để tạo ra doanh thu trong kỳ",
  },
  {
    code: "INVENTORY_TURNOVER",
    name: "Vòng quay hàng tồn kho",
    shortName: "Vòng quay tồn kho",
    category: "EFFICIENCY",
    categoryName: "Hiệu quả hoạt động",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 4.0,
    warningThreshold: 2.5,
    industryBenchmark: 4.0,
    isCoreKpi: false,
    isKeyRatio: true,
    formula: "Giá vốn hàng bán / Hàng tồn kho BQ",
    description: "Tốc độ luân chuyển và khả năng giải phóng hàng tồn kho",
  },
  {
    code: "RECEIVABLES_TURNOVER",
    name: "Vòng quay các khoản phải thu",
    shortName: "Vòng quay phải thu",
    category: "EFFICIENCY",
    categoryName: "Hiệu quả hoạt động",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 6.0,
    warningThreshold: 3.5,
    industryBenchmark: 6.0,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "Doanh thu thuần / Phải thu BQ",
    description: "Tốc độ thu hồi tiền bán hàng từ khách hàng và đối tác",
  },
  {
    code: "PAYABLES_TURNOVER",
    name: "Vòng quay các khoản phải trả",
    shortName: "Vòng quay phải trả",
    category: "EFFICIENCY",
    categoryName: "Hiệu quả hoạt động",
    unit: "TIMES",
    direction: "HIGHER_IS_BETTER",
    safeThreshold: 4.0,
    warningThreshold: 2.5,
    industryBenchmark: 4.0,
    isCoreKpi: false,
    isKeyRatio: false,
    formula: "Giá vốn hàng bán / Phải trả BQ",
    description: "Tốc độ thanh toán các khoản nợ nhà cung cấp của doanh nghiệp",
  },
];

const STORAGE_KEY = "financial_system_ratio_thresholds_v2";
const THRESHOLD_CHANGE_EVENT = "financial_system_thresholds_updated";

/**
 * Lấy danh sách cấu hình mốc tham chiếu từ localStorage hoặc fallback về mặc định TT200
 */
export function getSavedRatioThresholds(): RatioThresholdConfig[] {
  if (typeof window === "undefined") {
    return DEFAULT_RATIO_THRESHOLDS;
  }
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return DEFAULT_RATIO_THRESHOLDS;
    }
    const parsed = JSON.parse(raw) as Partial<RatioThresholdConfig>[];
    // Merge với defaults để luôn đảm bảo đầy đủ các thuộc tính và đủ 14 chỉ số
    return DEFAULT_RATIO_THRESHOLDS.map((def) => {
      const match = parsed.find((p) => p.code === def.code);
      if (!match) return def;
      return {
        ...def,
        ...match,
        safeThreshold: match.safeThreshold !== undefined ? Number(match.safeThreshold) : def.safeThreshold,
        warningThreshold: match.warningThreshold !== undefined ? Number(match.warningThreshold) : def.warningThreshold,
        industryBenchmark: match.industryBenchmark !== undefined ? Number(match.industryBenchmark) : def.industryBenchmark,
        isKeyRatio: match.isKeyRatio !== undefined ? Boolean(match.isKeyRatio) : def.isKeyRatio,
      };
    });
  } catch (e) {
    console.error("Failed to load ratio thresholds from localStorage:", e);
    return DEFAULT_RATIO_THRESHOLDS;
  }
}

/**
 * Lưu danh sách cấu hình mốc tham chiếu và phát thông báo cập nhật cho toàn ứng dụng
 */
export function saveRatioThresholds(configs: RatioThresholdConfig[]): void {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(configs));
    window.dispatchEvent(new Event(THRESHOLD_CHANGE_EVENT));
  } catch (e) {
    console.error("Failed to save ratio thresholds to localStorage:", e);
  }
}

/**
 * Khôi phục toàn bộ 14 chỉ số về mặc định chuẩn TT200
 */
export function resetRatioThresholdsToDefault(): RatioThresholdConfig[] {
  if (typeof window !== "undefined") {
    try {
      localStorage.removeItem(STORAGE_KEY);
      window.dispatchEvent(new Event(THRESHOLD_CHANGE_EVENT));
    } catch (e) {
      console.error("Failed to reset ratio thresholds:", e);
    }
  }
  return DEFAULT_RATIO_THRESHOLDS;
}

/**
 * Đánh giá giá trị thực tế của một chỉ số theo mốc An toàn và Lưu ý (Xanh / Vàng / Đỏ)
 */
export interface EvaluationResult {
  status: RatioHealthStatus;
  label: "AN TOÀN" | "LƯU Ý" | "CẢNH BÁO" | "CHƯA ĐỦ DỮ LIỆU";
  colorClass: string;        // Tailwind text & bg color
  badgeVariant: "safe" | "warning" | "danger" | "outline";
  numericValue: number | null;
  formattedValue: string;
}

export function evaluateRatioHealth(
  rawVal: number | null | undefined,
  config: RatioThresholdConfig
): EvaluationResult {
  if (rawVal === null || rawVal === undefined) {
    return {
      status: "INSUFFICIENT_DATA",
      label: "CHƯA ĐỦ DỮ LIỆU",
      colorClass: "bg-slate-200 text-slate-700 border-slate-300",
      badgeVariant: "outline",
      numericValue: null,
      formattedValue: "—",
    };
  }

  // Chuẩn hóa tỷ lệ phần trăm nếu backend trả về dạng thập phân (ví dụ 0.12 -> 12%)
  let numVal = Number(rawVal);
  if (config.unit === "PERCENT") {
    // Nếu giá trị tuyệt đối <= 1.0 (trừ trường hợp = 0), nhiều khả năng là dạng thập phân 0.05
    if (Math.abs(numVal) <= 1.0 && numVal !== 0) {
      numVal = numVal * 100;
    }
  }

  const { safeThreshold, warningThreshold, direction, unit } = config;
  let status: RatioHealthStatus = "SAFE";

  if (direction === "HIGHER_IS_BETTER") {
    if (numVal >= safeThreshold) {
      status = "SAFE";
    } else if (numVal >= warningThreshold) {
      status = "WARNING";
    } else {
      status = "DANGER";
    }
  } else {
    // LOWER_IS_BETTER (ví dụ Nợ / Vốn CSH, Hệ số nợ)
    if (numVal <= safeThreshold) {
      status = "SAFE";
    } else if (numVal <= warningThreshold) {
      status = "WARNING";
    } else {
      status = "DANGER";
    }
  }

  const formattedValue =
    unit === "PERCENT"
      ? `${numVal.toFixed(2)}%`
      : unit === "DAYS"
      ? `${Math.round(numVal)} ngày`
      : `${numVal.toFixed(2)}x`;

  switch (status) {
    case "SAFE":
      return {
        status: "SAFE",
        label: "AN TOÀN",
        colorClass: "bg-[#22c55e] text-white",
        badgeVariant: "safe",
        numericValue: numVal,
        formattedValue,
      };
    case "WARNING":
      return {
        status: "WARNING",
        label: "LƯU Ý",
        colorClass: "bg-[#f59e0b] text-white",
        badgeVariant: "warning",
        numericValue: numVal,
        formattedValue,
      };
    case "DANGER":
    default:
      return {
        status: "DANGER",
        label: "CẢNH BÁO",
        colorClass: "bg-[#ef4444] text-white",
        badgeVariant: "danger",
        numericValue: numVal,
        formattedValue,
      };
  }
}

/**
 * Custom hook React cho phép các component đăng ký lắng nghe tự động khi ngưỡng thay đổi
 */
export function useRatioThresholds() {
  const [thresholds, setThresholds] = useState<RatioThresholdConfig[]>(() => getSavedRatioThresholds());

  useEffect(() => {
    const handleUpdate = () => {
      setThresholds(getSavedRatioThresholds());
    };
    window.addEventListener(THRESHOLD_CHANGE_EVENT, handleUpdate);
    return () => {
      window.removeEventListener(THRESHOLD_CHANGE_EVENT, handleUpdate);
    };
  }, []);

  const updateThreshold = useCallback((code: string, updates: Partial<RatioThresholdConfig>) => {
    setThresholds((prev) => {
      const next = prev.map((item) => (item.code === code ? { ...item, ...updates } : item));
      saveRatioThresholds(next);
      return next;
    });
  }, []);

  const updateAllThresholds = useCallback((newThresholds: RatioThresholdConfig[]) => {
    setThresholds(newThresholds);
    saveRatioThresholds(newThresholds);
  }, []);

  const resetToDefault = useCallback(() => {
    const defaults = resetRatioThresholdsToDefault();
    setThresholds(defaults);
  }, []);

  const getConfig = useCallback(
    (code: string): RatioThresholdConfig => {
      return thresholds.find((t) => t.code === code) || DEFAULT_RATIO_THRESHOLDS.find((t) => t.code === code)!;
    },
    [thresholds]
  );

  return {
    thresholds,
    updateThreshold,
    updateAllThresholds,
    resetToDefault,
    getConfig,
  };
}

