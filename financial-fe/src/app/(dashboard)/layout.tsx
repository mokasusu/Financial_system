"use client";

import { useEffect, useState, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/auth-store";
import { useCompanyStore } from "@/stores/company-store";
import { companyService } from "@/features/company/services";
import { Sidebar } from "@/components/layout/sidebar";
import { TopHeader } from "@/components/layout/top-header";
import { Loader2 } from "lucide-react";

interface DashboardLayoutProps {
  children: ReactNode;
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const currentCompany = useCompanyStore((state) => state.currentCompany);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/login");
      return;
    }

    const checkCompany = async () => {
      if (currentCompany) {
        setIsInitializing(false);
        return;
      }

      try {
        const company = await companyService.getMyCompany();
        setCurrentCompany(company);
        setIsInitializing(false);
      } catch (err: unknown) {
        const status = (err as { status?: number })?.status;
        if (status === 404) {
          // Người dùng chưa tạo doanh nghiệp, chuyển ngay tới trang Onboarding
          router.replace("/onboarding");
          return;
        }
        setIsInitializing(false);
      }
    };

    checkCompany();
  }, [isAuthenticated, currentCompany, router, setCurrentCompany]);

  if (isInitializing) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-[#006c49]" />
          <p className="text-sm font-medium text-slate-600">Đang khởi tạo không gian làm việc...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex bg-slate-50/60 antialiased">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <TopHeader />
        <main className="flex-1 p-6 overflow-y-auto">{children}</main>
      </div>
    </div>
  );
}

