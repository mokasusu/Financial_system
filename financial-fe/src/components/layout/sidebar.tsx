"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  FileSpreadsheet,
  Building2,
  SlidersHorizontal,
  LogOut,
  ChevronRight,
} from "lucide-react";
import { useAuthStore } from "@/stores/auth-store";
import { cn } from "@/lib/utils";

const navigationItems = [
  {
    name: "Dashboard",
    href: "/dashboard",
    icon: LayoutDashboard,
    badge: "Tổng quan",
  },
  {
    name: "Phân tích BCTC",
    href: "/statements/pipeline",
    icon: FileSpreadsheet,
    badge: "7 bước",
  },
  {
    name: "Hồ sơ Doanh nghiệp",
    href: "/company",
    icon: Building2,
  },
  {
    name: "Tiêu chí & Quy tắc",
    href: "/settings",
    icon: SlidersHorizontal,
  },
];

export function Sidebar() {
  const pathname = usePathname();
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = () => {
    logout();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  };

  return (
    <aside className="w-64 bg-white border-r border-slate-200 flex flex-col shrink-0 h-screen sticky top-0 select-none z-20">
      {/* Brand Header */}
      <div className="h-16 px-5 border-b border-slate-100 flex items-center gap-3">
        <div className="h-9 w-9 rounded-xl bg-[#006c49] text-white flex items-center justify-center shrink-0 shadow-xs">
          <Building2 className="h-5 w-5" />
        </div>
        <div className="overflow-hidden">
          <span className="font-bold text-sm text-[#006c49] tracking-tight block truncate">
            FINANCIAL HEALTH
          </span>
          <span className="text-[11px] text-slate-400 block truncate">
            Enterprise Assessment
          </span>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider px-3 mb-2">
          Phân hệ chức năng
        </div>

        {navigationItems.map((item) => {
          const isActive =
            pathname === item.href ||
            (item.href === "/statements/pipeline" && pathname.startsWith("/statements")) ||
            (item.href !== "/dashboard" && pathname.startsWith(item.href));
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-medium transition-all group",
                isActive
                  ? "bg-[#006c49] text-white shadow-xs font-semibold"
                  : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
              )}
            >
              <div className="flex items-center gap-3 min-w-0">
                <Icon
                  className={cn(
                    "h-4 w-4 shrink-0 transition-colors",
                    isActive ? "text-white" : "text-slate-500 group-hover:text-slate-700"
                  )}
                />
                <span className="truncate">{item.name}</span>
              </div>

              {item.badge && (
                <span
                  className={cn(
                    "text-[10px] px-1.5 py-0.5 rounded font-normal shrink-0",
                    isActive
                      ? "bg-white/20 text-white"
                      : "bg-slate-100 text-slate-600 group-hover:bg-slate-200"
                  )}
                >
                  {item.badge}
                </span>
              )}
            </Link>
          );
        })}
      </nav>

      {/* User Footer Profile & Logout */}
      <div className="p-3 border-t border-slate-100 bg-slate-50/50 space-y-2">
        <div className="flex items-center gap-2.5 px-2 py-1.5">
          <div className="h-8 w-8 rounded-full bg-[#006c49]/10 text-[#006c49] font-bold text-xs flex items-center justify-center shrink-0 border border-[#006c49]/20">
            {user?.fullName?.charAt(0) || "U"}
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-xs font-semibold text-slate-900 truncate">
              {user?.fullName || "Chuyên viên tài chính"}
            </div>
            <div className="text-[11px] text-slate-400 truncate">
              {user?.email || "chuyenvien@nganhang.vn"}
            </div>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-medium text-rose-600 hover:bg-rose-50 transition-colors"
        >
          <LogOut className="h-3.5 w-3.5" />
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
}

