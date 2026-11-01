import { apiClient } from "@/lib/api-client";
import type { ApiResponse, PageResponse } from "@/types/api";
import type {
  EvaluationCriterion,
  UpdateCriterionRequest,
  ValidationRule,
  UpdateValidationRuleRequest,
} from "./types";

export const ruleSettingsService = {
  // 1. Evaluation Criteria APIs
  listCriteria: async (
    page: number = 0,
    size: number = 50
  ): Promise<EvaluationCriterion[]> => {
    const response = await apiClient.get<
      ApiResponse<PageResponse<EvaluationCriterion>>
    >("/api/evaluation-criteria", {
      params: { page, size },
    });
    return response.data.data?.items || [];
  },

  updateCriterion: async (
    id: number,
    payload: UpdateCriterionRequest
  ): Promise<EvaluationCriterion> => {
    const response = await apiClient.put<ApiResponse<EvaluationCriterion>>(
      `/api/evaluation-criteria/${id}`,
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể cập nhật tiêu chí");
    }
    return response.data.data;
  },

  toggleCriterionStatus: async (
    id: number,
    isActive: boolean
  ): Promise<EvaluationCriterion> => {
    const response = await apiClient.patch<ApiResponse<EvaluationCriterion>>(
      `/api/evaluation-criteria/${id}/status`,
      { isActive }
    );
    if (!response.data.data) {
      throw new Error("Không thể cập nhật trạng thái tiêu chí");
    }
    return response.data.data;
  },

  // 2. Validation Rules APIs
  listValidationRules: async (
    page: number = 0,
    size: number = 50
  ): Promise<ValidationRule[]> => {
    const response = await apiClient.get<
      ApiResponse<PageResponse<ValidationRule>>
    >("/api/validation-rules", {
      params: { page, size },
    });
    return response.data.data?.items || [];
  },

  updateValidationRule: async (
    id: number,
    payload: UpdateValidationRuleRequest
  ): Promise<ValidationRule> => {
    const response = await apiClient.put<ApiResponse<ValidationRule>>(
      `/api/validation-rules/${id}`,
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể cập nhật quy tắc kiểm định");
    }
    return response.data.data;
  },

  toggleValidationRuleStatus: async (
    id: number,
    isActive: boolean
  ): Promise<ValidationRule> => {
    const response = await apiClient.patch<ApiResponse<ValidationRule>>(
      `/api/validation-rules/${id}/status`,
      { isActive }
    );
    if (!response.data.data) {
      throw new Error("Không thể cập nhật trạng thái quy tắc kiểm định");
    }
    return response.data.data;
  },
};

