import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/types/api";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RefreshTokenRequest,
  GoogleAuthRequest,
  User,
} from "./types";

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/login",
      credentials
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Đăng nhập không thành công");
    }
    return response.data.data;
  },

  register: async (payload: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/register",
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Đăng ký không thành công");
    }
    return response.data.data;
  },

  refreshToken: async (payload: RefreshTokenRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/refresh",
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Làm mới phiên thất bại");
    }
    return response.data.data;
  },

  logout: async (refreshToken?: string | null): Promise<void> => {
    try {
      await apiClient.post<ApiResponse<void>>("/api/auth/logout", {
        refreshToken: refreshToken || "",
      });
    } catch {
      // Ignore network logout errors to always allow client cleanup
    }
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>("/api/auth/me");
    if (!response.data.data) {
      throw new Error(response.data.message || "Không thể lấy thông tin người dùng");
    }
    return response.data.data;
  },

  loginWithGoogle: async (payload: GoogleAuthRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/api/auth/google",
      payload
    );
    if (!response.data.data) {
      throw new Error(response.data.message || "Đăng nhập Google thất bại");
    }
    return response.data.data;
  },
};

