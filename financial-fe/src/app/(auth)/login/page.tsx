"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2, AlertCircle } from "lucide-react";
import { AuthLayout } from "@/features/auth/components/auth-layout";
import { loginSchema, type LoginFormData } from "@/features/auth/schemas";
import { authService } from "@/features/auth/services";
import { companyService } from "@/features/company/services";
import { useAuthStore } from "@/stores/auth-store";
import { useCompanyStore } from "@/stores/company-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getBaseURL } from "@/lib/api-client";

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
      rememberMe: false,
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      setIsLoading(true);
      setErrorMessage(null);

      // 1. Authenticate user
      const authResponse = await authService.login({
        email: data.email,
        password: data.password,
      });

      setAuth(authResponse);

      // 2. Check if user already owns a company
      try {
        const company = await companyService.getMyCompany();
        setCurrentCompany(company);
        router.push("/dashboard");
      } catch (companyErr: unknown) {
        // If 404 COMPANY_NOT_FOUND, redirect to onboarding
        const status = (companyErr as { status?: number })?.status;
        if (status === 404) {
          router.push("/onboarding");
        } else {
          router.push("/dashboard");
        }
      }
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản hoặc mật khẩu.";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Đăng nhập hệ thống"
      subtitle="Nhập thông tin xác thực để truy cập không gian làm việc thẩm định"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {errorMessage && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 animate-in fade-in duration-200">
            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
            <div className="flex-1">{errorMessage}</div>
          </div>
        )}

        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-slate-700">
            Địa chỉ Email <span className="text-rose-500">*</span>
          </label>
          <Input
            type="email"
            placeholder="chuyenvien@nganhang.vn"
            autoComplete="email"
            disabled={isLoading}
            className={errors.email ? "border-rose-400 focus-visible:ring-rose-400" : ""}
            {...register("email")}
          />
          {errors.email && (
            <p className="text-xs text-rose-500 font-medium">{errors.email.message}</p>
          )}
        </div>

        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <label className="text-xs font-semibold text-slate-700">
              Mật khẩu <span className="text-rose-500">*</span>
            </label>
          </div>
          <div className="relative">
            <Input
              type={showPassword ? "text" : "password"}
              placeholder="••••••••"
              autoComplete="current-password"
              disabled={isLoading}
              className={errors.password ? "border-rose-400 focus-visible:ring-rose-400 pr-10" : "pr-10"}
              {...register("password")}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
              tabIndex={-1}
            >
              {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          {errors.password && (
            <p className="text-xs text-rose-500 font-medium">{errors.password.message}</p>
          )}
        </div>

        <div className="flex items-center justify-between text-xs">
          <label className="flex items-center gap-2 cursor-pointer select-none text-slate-600">
            <input
              type="checkbox"
              className="rounded border-slate-300 text-[#006c49] focus:ring-[#006c49]"
              {...register("rememberMe")}
            />
            <span>Ghi nhớ đăng nhập</span>
          </label>
        </div>

        <Button
          type="submit"
          className="w-full bg-[#006c49] hover:bg-[#00563a] text-white font-medium h-10 shadow-xs"
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Đang xác thực...
            </>
          ) : (
            "Đăng nhập"
          )}
        </Button>

        <div className="relative my-4">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200" />
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-white px-2 text-slate-400">hoặc</span>
          </div>
        </div>

        <Button
          type="button"
          variant="outline"
          className="w-full h-10 border-slate-200 text-slate-700 hover:bg-slate-50 font-normal"
          onClick={() => {
            // OAuth2 endpoint supported by financial-be
            const apiBase = getBaseURL();
            window.location.href = `${apiBase}/oauth2/authorization/google`;
          }}
        >
          <svg className="h-4 w-4 mr-2" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          Đăng nhập với Google
        </Button>

        <p className="text-center text-xs text-slate-500 pt-2">
          Chưa có tài khoản chuyên viên?{" "}
          <Link
            href="/register"
            className="font-semibold text-[#006c49] hover:underline"
          >
            Đăng ký tài khoản mới
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}

