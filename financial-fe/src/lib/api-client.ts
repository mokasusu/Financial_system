import axios, {
  AxiosError,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import { useAuthStore } from "@/stores/auth-store";
import type { ApiResponse, ApiError } from "@/types/api";
import type { AuthResponse } from "@/features/auth/types";

const baseURL = process.env.NEXT_PUBLIC_API_URL?.trim() || "";

export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000,
});

// Flag to prevent infinite retry loops and queue concurrent requests
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

// Request Interceptor: Attach JWT Bearer Token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken;
    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle Silent Refresh on 401
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    if (!originalRequest) {
      return Promise.reject(normalizeError(error));
    }

    const isAuthEndpoint =
      originalRequest.url?.includes("/api/auth/login") ||
      originalRequest.url?.includes("/api/auth/register") ||
      originalRequest.url?.includes("/api/auth/refresh");

    // Check if 401 Unauthorized and not already retrying
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !isAuthEndpoint
    ) {
      const refreshToken = useAuthStore.getState().refreshToken;

      if (!refreshToken) {
        useAuthStore.getState().logout();
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
        return Promise.reject(normalizeError(error));
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(normalizeError(err)));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshResponse = await axios.post<ApiResponse<AuthResponse>>(
          `${baseURL}/api/auth/refresh`,
          { refreshToken },
          { headers: { "Content-Type": "application/json" } }
        );

        const newAuthData = refreshResponse.data.data;
        if (!newAuthData?.accessToken) {
          throw new Error("Invalid token refresh payload");
        }

        useAuthStore.getState().setTokens(
          newAuthData.accessToken,
          newAuthData.refreshToken || refreshToken
        );

        originalRequest.headers.Authorization = `Bearer ${newAuthData.accessToken}`;
        processQueue(null, newAuthData.accessToken);

        return apiClient(originalRequest);
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        useAuthStore.getState().logout();
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
        return Promise.reject(normalizeError(refreshErr as AxiosError<ApiResponse>));
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(normalizeError(error));
  }
);

function normalizeError(error: AxiosError<ApiResponse>): ApiError {
  const responseData = error.response?.data;
  let message = responseData?.message || error.message || "Đã có lỗi xảy ra trong quá trình xử lý.";

  if (!responseData?.message) {
    if (error.response?.status === 500) {
      message = "Máy chủ phản hồi lỗi (500). Vui lòng kiểm tra lại dịch vụ backend hoặc thử lại sau giây lát.";
    } else if (error.response?.status === 404) {
      message = "Không tìm thấy tài nguyên yêu cầu (404).";
    } else if (error.code === "ERR_NETWORK" || !error.response) {
      message = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.";
    }
  }

  return {
    success: false,
    message,
    errorCode: responseData?.errorCode,
    status: error.response?.status,
    timestamp: responseData?.timestamp,
  };
}

export default apiClient;

