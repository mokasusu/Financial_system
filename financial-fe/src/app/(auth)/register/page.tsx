"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Loader2, AlertCircle, CheckCircle2 } from "lucide-react";
import { AuthLayout } from "@/features/auth/components/auth-layout";
import { registerSchema, type RegisterFormData } from "@/features/auth/schemas";
import { authService } from "@/features/auth/services";
import { useAuthStore } from "@/stores/auth-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function RegisterPage() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: "",
      email: "",
      phoneNumber: "",
      password: "",
      confirmPassword: "",
    },
  });

  const passwordValue = watch("password", "");

  const onSubmit = async (data: RegisterFormData) => {
    try {
      setIsLoading(true);
      setErrorMessage(null);

      const authResponse = await authService.register({
        fullName: data.fullName,
        email: data.email,
        password: data.password,
        phoneNumber: data.phoneNumber || undefined,
      });

      setAuth(authResponse);
      // New user won't have a company yet, route directly to Onboarding
      router.push("/onboarding");
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Đăng ký thất bại. Email có thể đã được sử dụng trong hệ thống.";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Tạo tài khoản chuyên viên"
      subtitle="Đăng ký tài khoản để bắt đầu thẩm định và đánh giá sức khỏe tài chính"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3.5">
        {errorMessage && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 animate-in fade-in duration-200">
            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
            <div className="flex-1">{errorMessage}</div>
          </div>
        )}

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">
            Họ và tên chuyên viên <span className="text-rose-500">*</span>
          </label>
          <Input
            placeholder="Nguyễn Văn A"
            disabled={isLoading}
            className={errors.fullName ? "border-rose-400" : ""}
            {...register("fullName")}
          />
          {errors.fullName && (
            <p className="text-xs text-rose-500 font-medium">{errors.fullName.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">
            Email cơ quan <span className="text-rose-500">*</span>
          </label>
          <Input
            type="email"
            placeholder="analyst@company.com"
            disabled={isLoading}
            className={errors.email ? "border-rose-400" : ""}
            {...register("email")}
          />
          {errors.email && (
            <p className="text-xs text-rose-500 font-medium">{errors.email.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">Số điện thoại liên hệ</label>
          <Input
            type="tel"
            placeholder="0912345678"
            disabled={isLoading}
            {...register("phoneNumber")}
          />
          {errors.phoneNumber && (
            <p className="text-xs text-rose-500 font-medium">{errors.phoneNumber.message}</p>
          )}
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">
            Mật khẩu <span className="text-rose-500">*</span>
          </label>
          <div className="relative">
            <Input
              type={showPassword ? "text" : "password"}
              placeholder="Tối thiểu 6 ký tự"
              disabled={isLoading}
              className={errors.password ? "border-rose-400 pr-10" : "pr-10"}
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

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-700">
            Xác nhận mật khẩu <span className="text-rose-500">*</span>
          </label>
          <Input
            type={showPassword ? "text" : "password"}
            placeholder="Nhập lại mật khẩu"
            disabled={isLoading}
            className={errors.confirmPassword ? "border-rose-400" : ""}
            {...register("confirmPassword")}
          />
          {errors.confirmPassword && (
            <p className="text-xs text-rose-500 font-medium">{errors.confirmPassword.message}</p>
          )}
        </div>

        {/* Real-time strength hint */}
        {passwordValue && (
          <div className="p-2.5 rounded-lg bg-slate-50 border border-slate-200 text-xs space-y-1 text-slate-600">
            <div className="flex items-center gap-1.5">
              <CheckCircle2
                className={`h-3.5 w-3.5 ${
                  passwordValue.length >= 6 ? "text-emerald-600" : "text-slate-300"
                }`}
              />
              <span>Tối thiểu 6 ký tự</span>
            </div>
          </div>
        )}

        <Button
          type="submit"
          className="w-full bg-[#006c49] hover:bg-[#00563a] text-white font-medium h-10 shadow-xs mt-2"
          disabled={isLoading}
        >
          {isLoading ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Đang khởi tạo tài khoản...
            </>
          ) : (
            "Đăng ký ngay"
          )}
        </Button>

        <p className="text-center text-xs text-slate-500 pt-2">
          Đã có tài khoản chuyên viên?{" "}
          <Link href="/login" className="font-semibold text-[#006c49] hover:underline">
            Đăng nhập
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}

