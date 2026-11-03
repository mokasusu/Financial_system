import { apiClient } from "@/lib/api-client";
import type { ApiResponse, PageResponse } from "@/types/api";
import type {
  FinancialStatement,
  StatementItem,
  StatementUploadResponse,
  StatementStatusResponse,
} from "./types";

export const statementService = {
  upload: async (
    file: File,
    period: string = "current",
    enableAi: boolean = true,
    storeImages: boolean = true
  ): Promise<StatementUploadResponse> => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("period", period);
    formData.append("enableAi", String(enableAi));
    formData.append("storeImages", String(storeImages));

    const response = await apiClient.post<ApiResponse<StatementUploadResponse>>(
      "/api/financial-statements/upload",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );

    if (!response.data.data) {
      throw new Error(response.data.message || "Tải lên tài liệu BCTC thất bại");
    }
    return response.data.data;
  },

  extract: async (id: number): Promise<StatementStatusResponse> => {
    const response = await apiClient.post<ApiResponse<StatementStatusResponse>>(
      `/api/financial-statements/${id}/extract`
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Kích hoạt bóc tách OCR thất bại");
    }
    return response.data.data;
  },

  getStatus: async (id: number): Promise<StatementStatusResponse> => {
    const response = await apiClient.get<ApiResponse<StatementStatusResponse>>(
      `/api/financial-statements/${id}/status`
    );
    if (!response.data.data) {
      throw new Error("Không thể lấy trạng thái xử lý BCTC");
    }
    return response.data.data;
  },

  getStatementById: async (id: number): Promise<FinancialStatement> => {
    const response = await apiClient.get<ApiResponse<FinancialStatement>>(
      `/api/financial-statements/${id}`
    );
    if (!response.data.data) {
      throw new Error("Không tìm thấy thông tin BCTC");
    }
    return response.data.data;
  },

  getItems: async (id: number): Promise<StatementItem[]> => {
    const response = await apiClient.get<ApiResponse<StatementItem[]>>(
      `/api/financial-statements/${id}/items`
    );
    return response.data.data || [];
  },

  updateItems: async (
    id: number,
    items: Partial<StatementItem>[]
  ): Promise<StatementItem[]> => {
    const payload = {
      items: items.map((i) => ({
        id: i.id,
        currentValue: i.currentValue ?? i.current ?? 0,
        previousValue: i.previousValue ?? i.previous ?? null,
      })),
    };
    const response = await apiClient.put<ApiResponse<StatementItem[]>>(
      `/api/financial-statements/${id}/items`,
      payload
    );
    return response.data.data || [];
  },

  confirm: async (id: number): Promise<FinancialStatement> => {
    const response = await apiClient.post<ApiResponse<FinancialStatement>>(
      `/api/financial-statements/${id}/confirm`
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Xác nhận chốt số liệu thất bại");
    }
    return response.data.data;
  },

  listStatements: async (
    page: number = 0,
    size: number = 10
  ): Promise<PageResponse<FinancialStatement>> => {
    const response = await apiClient.get<
      ApiResponse<PageResponse<FinancialStatement>>
    >("/api/financial-statements", {
      params: { page, size },
    });
    return (
      response.data.data || {
        items: [],
        pageNumber: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
        isFirst: true,
        isLast: true,
      }
    );
  },

  deleteStatement: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/api/financial-statements/${id}`);
  },

  finalizeStatement: async (id: number): Promise<FinancialStatement> => {
    const response = await apiClient.post<ApiResponse<FinancialStatement>>(
      `/api/financial-statements/${id}/finalize`
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Lưu và phê duyệt BCTC thất bại");
    }
    return response.data.data;
  },
};

// Analysis Pipeline Services for Steps 4, 5, 6, 7
export const pipelineService = {
  triggerValidation: async (statementId: number) => {
    const res = await apiClient.post(
      `/api/financial-statements/${statementId}/validations`
    );
    return res.data;
  },

  triggerCalculation: async (statementId: number) => {
    const res = await apiClient.post(
      `/api/financial-statements/${statementId}/calculations`
    );
    return res.data;
  },

  triggerAssessment: async (statementId: number) => {
    const res = await apiClient.post(
      `/api/financial-statements/${statementId}/assessments`
    );
    return res.data;
  },

  triggerRiskAnalysis: async (statementId: number) => {
    const res = await apiClient.post(
      `/api/financial-statements/${statementId}/risk-analyses`
    );
    return res.data;
  },

  triggerAIGeneration: async (assessmentId: number) => {
    const res = await apiClient.post(
      `/api/ai-analyses/assessments/${assessmentId}/generate`
    );
    return res.data;
  },

  generateAIByStatement: async (statementId: number) => {
    const res = await apiClient.post<ApiResponse<any>>(
      `/api/ai-analyses/statements/${statementId}/generate`
    );
    return res.data;
  },

  updateAIFinalCommentByStatement: async (statementId: number, finalComment: string) => {
    const res = await apiClient.put<ApiResponse<any>>(
      `/api/ai-analyses/statements/${statementId}/final-comment`,
      { finalComment }
    );
    return res.data;
  },

  getAIAnalysisStatus: async (analysisId: number) => {
    const res = await apiClient.get(`/api/ai-analyses/${analysisId}`);
    return res.data;
  },

  updateAIFinalComment: async (analysisId: number, finalComment: string) => {
    const res = await apiClient.put(
      `/api/ai-analyses/${analysisId}/final-comment`,
      { finalComment }
    );
    return res.data;
  },

  approveAIAnalysis: async (analysisId: number) => {
    const res = await apiClient.post(`/api/ai-analyses/${analysisId}/approve`);
    return res.data;
  },

  rejectAIAnalysis: async (analysisId: number) => {
    const res = await apiClient.post(`/api/ai-analyses/${analysisId}/reject`);
    return res.data;
  },
};

