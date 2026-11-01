"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Building2, Loader2, AlertCircle, ArrowRight, ShieldCheck } from "lucide-react";
import { companySchema, type CompanyFormData } from "@/features/company/schemas";
import { companyService } from "@/features/company/services";
import { useAuthStore } from "@/stores/auth-store";
import { useCompanyStore } from "@/stores/company-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";

export default function OnboardingPage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const [isLoading, setIsLoading] = useState(false);
  const [isCheckingCompany, setIsCheckingCompany] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Check authentication & existing company status on mount
  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/login");
      return;
    }

    const checkCompany = async () => {
      try {
        const company = await companyService.getMyCompany();
        if (company) {
          setCurrentCompany(company);
          router.push("/dashboard");
        }
      } catch {
        // Expected 404 for new users needing onboarding
      } finally {
        setIsCheckingCompany(false);
      }
    };

    checkCompany();
  }, [isAuthenticated, router, setCurrentCompany]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CompanyFormData>({
    resolver: zodResolver(companySchema),
    defaultValues: {
      taxCode: "",
      companyName: "",
      shortName: "",
      companyType: "JOINT_STOCK_COMPANY",
      mainBusinessLine: "",
      legalRepresentative: "",
      address: "",
      ward: "",
      province: "Thành phố Hà Nội",
      phone: "",
      email: "",
      website: "",
    },
  });

  const onSubmit = async (data: CompanyFormData) => {
    try {
      setIsLoading(true);
      setErrorMessage(null);

      const createdCompany = await companyService.createCompany({
        taxCode: data.taxCode,
        companyName: data.companyName,
        shortName: data.shortName || undefined,
        companyType: data.companyType,
        mainBusinessLine: data.mainBusinessLine,
        legalRepresentative: data.legalRepresentative || undefined,
        address: data.address,
        ward: data.ward,
        province: data.province,
        phone: data.phone || undefined,
        email: data.email || undefined,
        website: data.website || undefined,
      });

      setCurrentCompany(createdCompany);
      router.push("/dashboard");
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Không thể lưu thông tin doanh nghiệp. Mã số thuế có thể đã tồn tại.";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  if (isCheckingCompany) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-[#006c49]" />
          <p className="text-sm font-medium text-slate-600">Đang kiểm tra hồ sơ doanh nghiệp...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50/70 py-12 px-4 sm:px-6 lg:px-8 flex flex-col justify-center">
      <div className="max-w-3xl mx-auto w-full">
        {/* Brand header */}
        <div className="text-center mb-8 space-y-2">
          <div className="inline-flex h-12 w-12 rounded-xl bg-[#006c49] text-white items-center justify-center shadow-md mb-2">
            <Building2 className="h-7 w-7" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Khởi tạo Hồ sơ Doanh nghiệp Thẩm định
          </h1>
          <p className="text-sm text-slate-500 max-w-lg mx-auto">
            Hệ thống yêu cầu mỗi chuyên viên thiết lập thông tin định danh doanh nghiệp trước khi thực hiện bóc tách BCTC và phân tích rủi ro.
          </p>
        </div>

        <Card className="shadow-sm border-slate-200">
          <CardHeader className="border-b border-slate-100 bg-white">
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Thông tin Pháp lý Doanh nghiệp</CardTitle>
                <CardDescription>
                  Các trường có dấu <span className="text-rose-500 font-semibold">*</span> là bắt buộc theo quy định xác thực
                </CardDescription>
              </div>
              <div className="hidden sm:flex items-center gap-1.5 text-xs text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
                <ShieldCheck className="h-3.5 w-3.5" />
                Dữ liệu bảo mật
              </div>
            </div>
          </CardHeader>

          <CardContent className="p-6">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {errorMessage && (
                <div className="p-3.5 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 animate-in fade-in duration-200">
                  <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
                  <div className="flex-1 font-medium">{errorMessage}</div>
                </div>
              )}

              {/* Nhóm 1: Định danh */}
              <div className="space-y-4">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  1. Định danh doanh nghiệp
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">
                      Mã số thuế (MST) <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="0101234567"
                      disabled={isLoading}
                      className={errors.taxCode ? "border-rose-400" : ""}
                      {...register("taxCode")}
                    />
                    {errors.taxCode ? (
                      <p className="text-xs text-rose-500 font-medium">{errors.taxCode.message}</p>
                    ) : (
                      <p className="text-[11px] text-slate-400">10 số hoặc 13 số có dấu gạch ngang</p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">
                      Loại hình doanh nghiệp <span className="text-rose-500">*</span>
                    </label>
                    <select
                      className="flex h-9 w-full rounded-lg border border-input bg-background px-3 py-1 text-sm shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={isLoading}
                      {...register("companyType")}
                    >
                      <option value="JOINT_STOCK_COMPANY">Công ty Cổ phần</option>
                      <option value="SINGLE_MEMBER_LIMITED_LIABILITY_COMPANY">
                        Công ty TNHH Một thành viên
                      </option>
                      <option value="MULTI_MEMBER_LIMITED_LIABILITY_COMPANY">
                        Công ty TNHH Hai thành viên trở lên
                      </option>
                      <option value="PARTNERSHIP_COMPANY">Công ty Hợp danh</option>
                      <option value="PRIVATE_ENTERPRISE">Doanh nghiệp Tư nhân</option>
                      <option value="OTHER">Loại hình khác</option>
                    </select>
                    {errors.companyType && (
                      <p className="text-xs text-rose-500 font-medium">{errors.companyType.message}</p>
                    )}
                  </div>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-700">
                    Tên doanh nghiệp đầy đủ (tiếng Việt) <span className="text-rose-500">*</span>
                  </label>
                  <Input
                    placeholder="Công ty Cổ phần Sữa Việt Nam"
                    disabled={isLoading}
                    className={errors.companyName ? "border-rose-400" : ""}
                    {...register("companyName")}
                  />
                  {errors.companyName && (
                    <p className="text-xs text-rose-500 font-medium">{errors.companyName.message}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">Tên viết tắt / Tên giao dịch</label>
                    <Input
                      placeholder="VINAMILK"
                      disabled={isLoading}
                      {...register("shortName")}
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">
                      Ngành nghề kinh doanh chính <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Chế biến và sản xuất sữa, đồ uống"
                      disabled={isLoading}
                      className={errors.mainBusinessLine ? "border-rose-400" : ""}
                      {...register("mainBusinessLine")}
                    />
                    {errors.mainBusinessLine && (
                      <p className="text-xs text-rose-500 font-medium">{errors.mainBusinessLine.message}</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Nhóm 2: Trụ sở & Liên hệ */}
              <div className="space-y-4 pt-4 border-t border-slate-100">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                  2. Trụ sở chính & Thông tin liên hệ
                </h3>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-700">
                    Địa chỉ chi tiết (Số nhà, đường) <span className="text-rose-500">*</span>
                  </label>
                  <Input
                    placeholder="Số 10 Tân Trào"
                    disabled={isLoading}
                    className={errors.address ? "border-rose-400" : ""}
                    {...register("address")}
                  />
                  {errors.address && (
                    <p className="text-xs text-rose-500 font-medium">{errors.address.message}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">
                      Phường / Xã <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Phường Tân Phú"
                      disabled={isLoading}
                      className={errors.ward ? "border-rose-400" : ""}
                      {...register("ward")}
                    />
                    {errors.ward && (
                      <p className="text-xs text-rose-500 font-medium">{errors.ward.message}</p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">
                      Tỉnh / Thành phố <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Thành phố Hồ Chí Minh"
                      disabled={isLoading}
                      className={errors.province ? "border-rose-400" : ""}
                      {...register("province")}
                    />
                    {errors.province && (
                      <p className="text-xs text-rose-500 font-medium">{errors.province.message}</p>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">Người đại diện pháp luật</label>
                    <Input
                      placeholder="Mai Kiều Liên"
                      disabled={isLoading}
                      {...register("legalRepresentative")}
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">Điện thoại cơ quan</label>
                    <Input
                      type="tel"
                      placeholder="02854155555"
                      disabled={isLoading}
                      {...register("phone")}
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-slate-700">Email doanh nghiệp</label>
                    <Input
                      type="email"
                      placeholder="vinamilk@vinamilk.com.vn"
                      disabled={isLoading}
                      {...register("email")}
                    />
                  </div>
                </div>
              </div>

              <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
                <Button
                  type="submit"
                  className="bg-[#006c49] hover:bg-[#00563a] text-white px-6 h-10 shadow-xs"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Đang khởi tạo doanh nghiệp...
                    </>
                  ) : (
                    <>
                      Hoàn tất & Vào Dashboard
                      <ArrowRight className="h-4 w-4 ml-2" />
                    </>
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

