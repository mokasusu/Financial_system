import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/types/api";
import type {
  DashboardOverview,
  DashboardKPI,
  DashboardCategory,
  DashboardVisualAnalytics,
  DashboardTrendResponse,
} from "./types";

export const dashboardService = {
  getOverview: async (
    period?: string,
    financialStatementId?: number
  ): Promise<DashboardOverview> => {
    const params: Record<string, string | number> = {};
    if (period) params.period = period;
    if (financialStatementId) params.financialStatementId = financialStatementId;

    const response = await apiClient.get<ApiResponse<DashboardOverview>>(
      "/api/dashboard/overview",
      { params }
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể tải dữ liệu Dashboard");
    }
    return response.data.data;
  },

  getKPIs: async (period?: string): Promise<DashboardKPI[]> => {
    const params = period ? { period } : {};
    const response = await apiClient.get<ApiResponse<DashboardKPI[]>>(
      "/api/dashboard/kpis",
      { params }
    );
    return response.data.data || [];
  },

  getFinancialRatios: async (period?: string): Promise<DashboardCategory[]> => {
    const params = period ? { period } : {};
    const response = await apiClient.get<ApiResponse<DashboardCategory[]>>(
      "/api/dashboard/financial-ratios",
      { params }
    );
    return response.data.data || [];
  },

  getVisualAnalytics: async (
    period?: string
  ): Promise<DashboardVisualAnalytics> => {
    const params = period ? { period } : {};
    const response = await apiClient.get<ApiResponse<DashboardVisualAnalytics>>(
      "/api/dashboard/visual-analytics",
      { params }
    );
    if (!response.data.data) {
      throw new Error("Không có dữ liệu phân tích trực quan");
    }
    return response.data.data;
  },

  getTrends: async (periodCount: number = 4): Promise<DashboardTrendResponse> => {
    const response = await apiClient.get<ApiResponse<DashboardTrendResponse>>(
      "/api/dashboard/trends",
      { params: { periodCount } }
    );
    if (!response.data.data) {
      throw new Error("Không có dữ liệu xu hướng");
    }
    return response.data.data;
  },
};

