"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Loader2, AlertCircle } from "lucide-react";
import { authService } from "@/features/auth/services";
import { companyService } from "@/features/company/services";
import { useAuthStore } from "@/stores/auth-store";
import { useCompanyStore } from "@/stores/company-store";

function OAuth2CallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const handleCallback = async () => {
      const accessToken = searchParams.get("accessToken");
      const refreshToken = searchParams.get("refreshToken");
      const error = searchParams.get("error");

      if (error) {
        setErrorMessage("Đăng nhập thất bại: " + error);
        return;
      }

      if (!accessToken || !refreshToken) {
        setErrorMessage("Không tìm thấy mã xác thực từ máy chủ.");
        return;
      }

      try {
        // Tạm lưu token vào auth store để interceptor đính kèm Authorization header
        setAuth({
          accessToken,
          refreshToken,
          tokenType: "Bearer",
          user: {
            id: 0,
            fullName: "",
            email: "",
            status: "ACTIVE",
            provider: "GOOGLE",
          },
        });

        // Lấy thông tin user chính xác từ backend qua token vừa cấp
        const user = await authService.getCurrentUser();
        setAuth({
          accessToken,
          refreshToken,
          tokenType: "Bearer",
          user,
        });

        // Kiểm tra hồ sơ công ty để điều hướng thông minh
        try {
          const company = await companyService.getMyCompany();
          setCurrentCompany(company);
          router.replace("/dashboard");
        } catch (companyErr: unknown) {
          const status = (companyErr as { status?: number })?.status;
          if (status === 404) {
            router.replace("/onboarding");
          } else {
            router.replace("/dashboard");
          }
        }
      } catch (err: unknown) {
        const message =
          (err as { message?: string })?.message ||
          "Đã xảy ra lỗi trong quá trình xác thực phiên đăng nhập.";
        setErrorMessage(message);
      }
    };

    handleCallback();
  }, [searchParams, router, setAuth, setCurrentCompany]);

  if (errorMessage) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
        <div className="max-w-md w-full bg-white p-6 rounded-xl border border-rose-200 shadow-sm text-center space-y-4">
          <div className="w-12 h-12 rounded-full bg-rose-50 flex items-center justify-center mx-auto text-rose-600">
            <AlertCircle className="w-6 h-6" />
          </div>
          <h2 className="text-base font-semibold text-slate-800">
            Xác thực Google không thành công
          </h2>
          <p className="text-xs text-slate-600">{errorMessage}</p>
          <button
            onClick={() => router.push("/login")}
            className="w-full py-2 px-4 bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-medium rounded-lg transition"
          >
            Quay lại trang đăng nhập
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
      <div className="max-w-sm w-full bg-white p-6 rounded-xl border border-slate-200 shadow-xs text-center space-y-3">
        <Loader2 className="w-8 h-8 animate-spin text-[#006c49] mx-auto" />
        <h2 className="text-sm font-semibold text-slate-800">
          Đang hoàn tất đăng nhập Google...
        </h2>
        <p className="text-xs text-slate-500">
          Vui lòng đợi trong giây lát, hệ thống đang đồng bộ dữ liệu chuyên viên.
        </p>
      </div>
    </div>
  );
}

export default function OAuth2CallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-slate-50">
          <Loader2 className="w-8 h-8 animate-spin text-[#006c49]" />
        </div>
      }
    >
      <OAuth2CallbackContent />
    </Suspense>
  );
}

