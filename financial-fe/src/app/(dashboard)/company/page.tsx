"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCompanyStore } from "@/stores/company-store";
import { companyService } from "@/features/company/services";
import {
  companySchema,
  type CompanyFormData,
} from "@/features/company/schemas";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Building2,
  ShieldCheck,
  MapPin,
  Phone,
  Mail,
  Globe,
  Calendar,
  Edit3,
  X,
  Save,
  Loader2,
  AlertCircle,
  CheckCircle2,
  Briefcase,
  User,
} from "lucide-react";

const COMPANY_TYPES = [
  {
    value: "JOINT_STOCK_COMPANY",
    label: "Công ty Cổ phần (JSC)",
  },
  {
    value: "SINGLE_MEMBER_LIMITED_LIABILITY_COMPANY",
    label: "Công ty TNHH Một Thành viên (1TV)",
  },
  {
    value: "MULTI_MEMBER_LIMITED_LIABILITY_COMPANY",
    label: "Công ty TNHH Hai Thành viên trở lên (2TV)",
  },
  {
    value: "PARTNERSHIP_COMPANY",
    label: "Công ty Hợp danh",
  },
  {
    value: "PRIVATE_ENTERPRISE",
    label: "Doanh nghiệp Tư nhân",
  },
  {
    value: "OTHER",
    label: "Loại hình khác",
  },
];

export default function CompanyProfilePage() {
  const currentCompany = useCompanyStore((state) => state.currentCompany);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const [isEditing, setIsEditing] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CompanyFormData>({
    resolver: zodResolver(companySchema),
    values: currentCompany
      ? {
          taxCode: currentCompany.taxCode,
          companyName: currentCompany.companyName,
          companyNameEn: currentCompany.companyNameEn || "",
          shortName: currentCompany.shortName || "",
          companyType: currentCompany.companyType,
          mainBusinessLine: currentCompany.mainBusinessLine,
          establishedDate: currentCompany.establishedDate || "",
          legalRepresentative: currentCompany.legalRepresentative || "",
          address: currentCompany.address,
          ward: currentCompany.ward,
          province: currentCompany.province,
          phone: currentCompany.phone || "",
          email: currentCompany.email || "",
          website: currentCompany.website || "",
        }
      : undefined,
  });

  const handleStartEdit = () => {
    setErrorMessage(null);
    setSuccessMessage(null);
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    reset();
    setErrorMessage(null);
    setIsEditing(false);
  };

  const onSubmit = async (data: CompanyFormData) => {
    try {
      setIsSubmitting(true);
      setErrorMessage(null);
      setSuccessMessage(null);

      const payload = {
        taxCode: data.taxCode,
        companyName: data.companyName,
        companyNameEn: data.companyNameEn || undefined,
        shortName: data.shortName || undefined,
        companyType: data.companyType,
        mainBusinessLine: data.mainBusinessLine,
        establishedDate: data.establishedDate || undefined,
        legalRepresentative: data.legalRepresentative || undefined,
        address: data.address,
        ward: data.ward,
        province: data.province,
        phone: data.phone || undefined,
        email: data.email || undefined,
        website: data.website || undefined,
      };

      const updatedCompany = await companyService.updateMyCompany(payload);
      setCurrentCompany(updatedCompany);
      setSuccessMessage("Hồ sơ doanh nghiệp đã được cập nhật thành công.");
      setIsEditing(false);
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Cập nhật hồ sơ doanh nghiệp thất bại. Vui lòng thử lại.";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!currentCompany) {
    return (
      <div className="p-8 text-center text-slate-500">
        Chưa có thông tin doanh nghiệp. Vui lòng hoàn tất màn hình khởi tạo hồ sơ.
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-12">
      {/* Tiêu đề trang & Nút hành động */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Hồ sơ Doanh nghiệp Thẩm định
          </h1>
          <p className="text-sm text-slate-500">
            Thông tin pháp lý và thông số định danh phục vụ đối soát chuẩn mực kế toán và thẩm định BCTC
          </p>
        </div>

        {!isEditing && (
          <Button
            onClick={handleStartEdit}
            className="bg-[#006c49] hover:bg-[#005237] text-white shadow-xs font-semibold text-xs gap-1.5 shrink-0"
          >
            <Edit3 className="h-3.5 w-3.5" />
            Chỉnh sửa hồ sơ
          </Button>
        )}
      </div>

      {/* Thông báo Thành công */}
      {successMessage && (
        <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 flex items-center gap-2.5 text-xs text-emerald-900 shadow-2xs animate-in fade-in">
          <CheckCircle2 className="h-4 w-4 text-[#006c49] shrink-0" />
          <span className="font-semibold">{successMessage}</span>
        </div>
      )}

      {/* Thông báo Lỗi */}
      {errorMessage && (
        <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 shadow-2xs animate-in fade-in">
          <AlertCircle className="h-4 w-4 text-rose-600 shrink-0 mt-0.5" />
          <div className="flex-1 font-medium">{errorMessage}</div>
        </div>
      )}

      {/* Chế độ Chỉnh sửa (Form Mode) */}
      {isEditing ? (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <Card className="bg-white shadow-xs border border-slate-200">
            <CardHeader className="border-b border-slate-100 pb-4 flex flex-row items-center justify-between">
              <div>
                <CardTitle className="text-base font-semibold text-slate-900">
                  Cập nhật Thông tin Pháp lý Doanh nghiệp
                </CardTitle>
                <CardDescription>
                  Vui lòng rà soát chính xác Mã số thuế và tên đăng ký theo giấy phép kinh doanh
                </CardDescription>
              </div>

              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={handleCancelEdit}
                className="h-8 text-xs text-slate-500 hover:text-slate-800"
              >
                <X className="h-4 w-4 mr-1" />
                Hủy
              </Button>
            </CardHeader>

            <CardContent className="p-6 space-y-6 text-xs">
              {/* Nhóm 1: Thông tin Pháp lý cốt lõi */}
              <div className="space-y-3">
                <h3 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] text-[#006c49]">
                  1. Thông tin Định danh & Pháp lý
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Mã số thuế (MST) <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="0101234567"
                      className={`font-mono ${errors.taxCode ? "border-rose-400" : ""}`}
                      {...register("taxCode")}
                    />
                    {errors.taxCode && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.taxCode.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Loại hình doanh nghiệp <span className="text-rose-500">*</span>
                    </label>
                    <select
                      className={`flex h-9 w-full rounded-lg border bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-[#006c49] ${
                        errors.companyType ? "border-rose-400" : "border-input"
                      }`}
                      {...register("companyType")}
                    >
                      {COMPANY_TYPES.map((t) => (
                        <option key={t.value} value={t.value}>
                          {t.label}
                        </option>
                      ))}
                    </select>
                    {errors.companyType && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.companyType.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1 sm:col-span-2">
                    <label className="font-semibold text-slate-700">
                      Tên doanh nghiệp đầy đủ (tiếng Việt) <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Công ty Cổ phần Sữa Việt Nam"
                      className={errors.companyName ? "border-rose-400" : ""}
                      {...register("companyName")}
                    />
                    {errors.companyName && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.companyName.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">Tên tiếng Anh</label>
                    <Input
                      placeholder="Vietnam Dairy Products Joint Stock Company"
                      {...register("companyNameEn")}
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">Tên viết tắt / Giao dịch</label>
                    <Input placeholder="VINAMILK" {...register("shortName")} />
                  </div>

                  <div className="space-y-1 sm:col-span-2">
                    <label className="font-semibold text-slate-700">
                      Ngành nghề kinh doanh chính <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Chế biến và kinh doanh các sản phẩm từ sữa"
                      className={errors.mainBusinessLine ? "border-rose-400" : ""}
                      {...register("mainBusinessLine")}
                    />
                    {errors.mainBusinessLine && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.mainBusinessLine.message}
                      </p>
                    )}
                  </div>
                </div>
              </div>

              {/* Nhóm 2: Đại diện & Ngày thành lập */}
              <div className="space-y-3 pt-3 border-t border-slate-100">
                <h3 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] text-[#006c49]">
                  2. Đại diện Pháp luật & Niên độ
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Người đại diện theo pháp luật
                    </label>
                    <Input
                      placeholder="Mai Kiều Liên"
                      {...register("legalRepresentative")}
                    />
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">
                      Ngày thành lập doanh nghiệp
                    </label>
                    <Input type="date" {...register("establishedDate")} />
                  </div>
                </div>
              </div>

              {/* Nhóm 3: Địa chỉ Trụ sở chính */}
              <div className="space-y-3 pt-3 border-t border-slate-100">
                <h3 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] text-[#006c49]">
                  3. Địa chỉ Trụ sở chính
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="space-y-1 sm:col-span-3">
                    <label className="font-semibold text-slate-700">
                      Địa chỉ chi tiết (Số nhà, đường/phố) <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Số 10 Tân Trào"
                      className={errors.address ? "border-rose-400" : ""}
                      {...register("address")}
                    />
                    {errors.address && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.address.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1 sm:col-span-1">
                    <label className="font-semibold text-slate-700">
                      Phường / Xã <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Phường Tân Phú"
                      className={errors.ward ? "border-rose-400" : ""}
                      {...register("ward")}
                    />
                    {errors.ward && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.ward.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1 sm:col-span-2">
                    <label className="font-semibold text-slate-700">
                      Tỉnh / Thành phố <span className="text-rose-500">*</span>
                    </label>
                    <Input
                      placeholder="Thành phố Hồ Chí Minh"
                      className={errors.province ? "border-rose-400" : ""}
                      {...register("province")}
                    />
                    {errors.province && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.province.message}
                      </p>
                    )}
                  </div>
                </div>
              </div>

              {/* Nhóm 4: Thông tin Liên hệ */}
              <div className="space-y-3 pt-3 border-t border-slate-100">
                <h3 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] text-[#006c49]">
                  4. Kênh Liên hệ & Thông tin Điện tử
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">Số điện thoại</label>
                    <Input placeholder="02854155555" {...register("phone")} />
                    {errors.phone && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.phone.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">Email doanh nghiệp</label>
                    <Input
                      type="email"
                      placeholder="vinamilk@vinamilk.com.vn"
                      {...register("email")}
                    />
                    {errors.email && (
                      <p className="text-rose-500 font-medium text-[11px]">
                        {errors.email.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-1">
                    <label className="font-semibold text-slate-700">Website</label>
                    <Input
                      placeholder="https://www.vinamilk.com.vn"
                      {...register("website")}
                    />
                  </div>
                </div>
              </div>
            </CardContent>

            <div className="p-4 bg-slate-50/80 border-t border-slate-100 flex items-center justify-end gap-3 rounded-b-xl">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancelEdit}
                disabled={isSubmitting}
                className="text-xs font-semibold h-9"
              >
                Hủy bỏ
              </Button>

              <Button
                type="submit"
                disabled={isSubmitting}
                className="bg-[#006c49] hover:bg-[#005237] text-white font-semibold text-xs h-9 px-5 shadow-xs gap-1.5"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-1" />
                    Đang lưu thay đổi...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4" />
                    Lưu thay đổi
                  </>
                )}
              </Button>
            </div>
          </Card>
        </form>
      ) : (
        /* Chế độ Xem (View Mode) */
        <Card className="bg-white shadow-xs border border-slate-200">
          <CardHeader className="border-b border-slate-100 pb-4">
            <div className="flex items-start justify-between flex-wrap gap-3">
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-xl bg-[#006c49]/10 text-[#006c49] flex items-center justify-center font-bold text-lg border border-[#006c49]/20 shadow-2xs">
                  <Building2 className="h-6 w-6" />
                </div>
                <div>
                  <CardTitle className="text-lg font-bold text-slate-900">
                    {currentCompany.companyName}
                  </CardTitle>
                  <div className="flex items-center gap-2 mt-1">
                    <Badge variant="outline" className="font-mono text-xs">
                      MST: {currentCompany.taxCode}
                    </Badge>
                    <Badge variant="safe" className="text-xs">
                      <ShieldCheck className="h-3 w-3 mr-1" />
                      Đã xác thực pháp lý
                    </Badge>
                  </div>
                </div>
              </div>
            </div>
          </CardHeader>

          <CardContent className="p-6 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs">
              <div className="space-y-4">
                <div>
                  <span className="text-slate-400 font-medium block">Tên tiếng Anh</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.companyNameEn || "—"}
                  </span>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Tên viết tắt / Giao dịch</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.shortName || "—"}
                  </span>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Loại hình doanh nghiệp</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.companyTypeDisplayName || currentCompany.companyType}
                  </span>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Ngành nghề kinh doanh chính</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.mainBusinessLine}
                  </span>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Người đại diện pháp luật</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.legalRepresentative || "—"}
                  </span>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Ngày thành lập</span>
                  <span className="text-sm font-semibold text-slate-800">
                    {currentCompany.establishedDate || "—"}
                  </span>
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <span className="text-slate-400 font-medium block">Địa chỉ trụ sở chính</span>
                  <div className="flex items-start gap-1.5 mt-0.5">
                    <MapPin className="h-4 w-4 text-slate-400 shrink-0 mt-0.5" />
                    <span className="text-sm font-semibold text-slate-800 leading-snug">
                      {currentCompany.address}, {currentCompany.ward}, {currentCompany.province}
                    </span>
                  </div>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Số điện thoại liên hệ</span>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <Phone className="h-4 w-4 text-slate-400 shrink-0" />
                    <span className="text-sm font-semibold text-slate-800">
                      {currentCompany.phone || "—"}
                    </span>
                  </div>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Email doanh nghiệp</span>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <Mail className="h-4 w-4 text-slate-400 shrink-0" />
                    <span className="text-sm font-semibold text-slate-800">
                      {currentCompany.email || "—"}
                    </span>
                  </div>
                </div>

                <div>
                  <span className="text-slate-400 font-medium block">Website doanh nghiệp</span>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <Globe className="h-4 w-4 text-slate-400 shrink-0" />
                    <span className="text-sm font-semibold text-[#006c49]">
                      {currentCompany.website ? (
                        <a
                          href={
                            currentCompany.website.startsWith("http")
                              ? currentCompany.website
                              : `https://${currentCompany.website}`
                          }
                          target="_blank"
                          rel="noreferrer"
                          className="hover:underline"
                        >
                          {currentCompany.website}
                        </a>
                      ) : (
                        "—"
                      )}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
