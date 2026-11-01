import type { ReactNode } from "react";
import Link from "next/link";
import { ShieldCheck, FileCheck2, Sparkles, Building2 } from "lucide-react";

interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle: string;
}

export function AuthLayout({ children, title, subtitle }: AuthLayoutProps) {
  return (
    <div className="min-h-screen w-full flex flex-col lg:flex-row">
      {/* Left Column: Brand Showcase (50% on lg screens) */}
      <div className="relative hidden lg:flex lg:w-1/2 bg-[#006c49] text-white p-12 flex-col justify-between overflow-hidden">
        {/* Subtle grid pattern overlay */}
        <div className="absolute inset-0 bg-[radial-gradient(#ffffff_1px,transparent_1px)] [background-size:24px_24px] opacity-10 pointer-events-none" />

        {/* Top Logo */}
        <div className="relative z-10 flex items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-white/10 backdrop-blur-md flex items-center justify-center border border-white/20">
            <Building2 className="h-6 w-6 text-white" />
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight">FINANCIAL HEALTH</span>
            <span className="block text-xs text-emerald-200">Enterprise Assessment Platform</span>
          </div>
        </div>

        {/* Center Content & Value Pillars */}
        <div className="relative z-10 max-w-lg space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-xs font-medium text-emerald-100">
            <Sparkles className="h-3.5 w-3.5 text-emerald-300" />
            Nền tảng Thẩm định Tài chính Thế hệ Mới
          </div>

          <h1 className="text-3xl font-extrabold tracking-tight leading-tight">
            Đánh giá Sức khỏe Tài chính Doanh nghiệp Chuẩn xác & Toàn diện
          </h1>

          <p className="text-sm text-emerald-100/90 leading-relaxed">
            Hỗ trợ chuyên viên phân tích tín dụng, kiểm toán viên và giám đốc tài chính tự động hóa quy trình bóc tách BCTC, kiểm định đẳng thức kế toán TT200 và nhận định chuyên sâu từ AI.
          </p>

          <div className="space-y-4 pt-4 border-t border-white/15">
            <div className="flex items-start gap-3">
              <div className="h-7 w-7 rounded-lg bg-white/15 flex items-center justify-center shrink-0 mt-0.5">
                <FileCheck2 className="h-4 w-4 text-emerald-200" />
              </div>
              <div>
                <h4 className="text-sm font-semibold">Bóc tách OCR & Soát số liệu tức thì</h4>
                <p className="text-xs text-emerald-200/80">Nhận dạng bảng cân đối kế toán từ file scan/PDF nhiều trang với độ chính xác cao.</p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="h-7 w-7 rounded-lg bg-white/15 flex items-center justify-center shrink-0 mt-0.5">
                <ShieldCheck className="h-4 w-4 text-emerald-200" />
              </div>
              <div>
                <h4 className="text-sm font-semibold">Kiểm định 7 Đẳng thức Kế toán TT200</h4>
                <p className="text-xs text-emerald-200/80">Phát hiện sai lệch số dư, chênh lệch tài sản và nợ phải trả theo thời gian thực.</p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="h-7 w-7 rounded-lg bg-white/15 flex items-center justify-center shrink-0 mt-0.5">
                <Sparkles className="h-4 w-4 text-emerald-200" />
              </div>
              <div>
                <h4 className="text-sm font-semibold">Trợ lý AI Phân tích & Cảnh báo Rủi ro</h4>
                <p className="text-xs text-emerald-200/80">Tổng hợp 5 khối nhận định tài chính chuyên gia và đề xuất hành động tối ưu dòng tiền.</p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer Notice */}
        <div className="relative z-10 text-xs text-emerald-200/70 flex items-center justify-between">
          <span>Hệ thống bảo mật tiêu chuẩn ngân hàng</span>
          <span>© 2026 Enterprise Financial System</span>
        </div>
      </div>

      {/* Right Column: Form Canvas (50% on lg screens) */}
      <div className="flex-1 flex flex-col justify-center items-center p-6 sm:p-10 lg:p-16 bg-[#ffffff]">
        <div className="w-full max-w-md space-y-6">
          {/* Mobile Header */}
          <div className="lg:hidden flex items-center gap-2 mb-4">
            <div className="h-8 w-8 rounded-lg bg-[#006c49] flex items-center justify-center text-white">
              <Building2 className="h-5 w-5" />
            </div>
            <span className="font-bold text-[#006c49] tracking-tight">FINANCIAL HEALTH</span>
          </div>

          <div className="space-y-1">
            <h2 className="text-2xl font-bold tracking-tight text-slate-900">{title}</h2>
            <p className="text-sm text-slate-500">{subtitle}</p>
          </div>

          {children}
        </div>
      </div>
    </div>
  );
}

