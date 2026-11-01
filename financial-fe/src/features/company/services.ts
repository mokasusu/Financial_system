import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/types/api";
import type { Company, CreateCompanyRequest } from "./types";

export const companyService = {
  getMyCompany: async (): Promise<Company> => {
    const response = await apiClient.get<ApiResponse<Company>>(
      "/api/companies/me"
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không tìm thấy thông tin doanh nghiệp");
    }
    return response.data.data;
  },

  createCompany: async (payload: CreateCompanyRequest): Promise<Company> => {
    const response = await apiClient.post<ApiResponse<Company>>(
      "/api/companies",
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể khởi tạo doanh nghiệp");
    }
    return response.data.data;
  },

  updateMyCompany: async (payload: CreateCompanyRequest): Promise<Company> => {
    const response = await apiClient.put<ApiResponse<Company>>(
      "/api/companies/me",
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể cập nhật hồ sơ doanh nghiệp");
    }
    return response.data.data;
  },

  getCompanyById: async (id: number): Promise<Company> => {
    const response = await apiClient.get<ApiResponse<Company>>(
      `/api/companies/${id}`
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Không tìm thấy công ty");
    }
    return response.data.data;
  },
};

