"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { useCompanyStore } from "@/stores/company-store";
import { useStatementStore } from "@/stores/statement-store";
import { statementService } from "@/features/statements/services";
import { Button } from "@/components/ui/button";
import { Bell, FileText, Plus } from "lucide-react";

export function TopHeader() {
  const currentCompany = useCompanyStore((state) => state.currentCompany);

  const statements = useStatementStore((state) => state.statements);
  const activeStatementId = useStatementStore((state) => state.activeStatementId);
  const setStatements = useStatementStore((state) => state.setStatements);
  const setActiveStatementId = useStatementStore((state) => state.setActiveStatementId);

  // Fetch list of statements belonging to current company
  const { data: statementsData } = useQuery({
    queryKey: ["financial-statements", currentCompany?.id],
    queryFn: () => statementService.listStatements(0, 50),
    enabled: Boolean(currentCompany?.id),
    staleTime: 30 * 1000,
  });

  useEffect(() => {
    if (statementsData?.items) {
      setStatements(statementsData.items);
    }
  }, [statementsData, setStatements]);

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between sticky top-0 z-10">
      {/* Khối bên trái: Tên Công ty to rõ, MST xám mờ bên dưới */}
      <div className="flex items-center gap-3 min-w-0">
        {currentCompany ? (
          <div className="flex flex-col justify-center min-w-0">
            <h2 className="text-base sm:text-lg font-bold text-slate-900 truncate max-w-xs sm:max-w-md tracking-tight leading-snug">
              {currentCompany.companyName}
            </h2>
            <div className="text-xs text-slate-400 font-mono tracking-tight">
              MST: {currentCompany.taxCode}
            </div>
          </div>
        ) : (
          <span className="text-sm font-semibold text-slate-500">
            Hệ thống Đánh giá Sức khỏe Tài chính Doanh nghiệp
          </span>
        )}
      </div>

      {/* Khối bên phải: Dropdown chọn BCTC theo Tên File (fileName) */}
      <div className="flex items-center gap-2.5">
        <div className="flex items-center gap-2 bg-slate-50/80 border border-slate-200 px-3 py-1.5 rounded-lg text-xs shadow-2xs">
          <FileText className="h-4 w-4 text-[#006c49] shrink-0" />
          {statements.length > 0 ? (
            <select
              value={activeStatementId ?? ""}
              onChange={(e) =>
                setActiveStatementId(e.target.value ? Number(e.target.value) : null)
              }
              className="bg-transparent font-semibold text-slate-800 focus:outline-none cursor-pointer max-w-[180px] sm:max-w-[260px] truncate"
              title="Chọn phiên báo cáo tài chính đã phân tích để tải lại số liệu"
            >
              {statements.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.fileName || `BCTC_${s.fiscalYear || "Kỳ"}_#${s.id}.pdf`}
                </option>
              ))}
            </select>
          ) : (
            <span className="text-slate-400 italic">Chưa có tệp BCTC</span>
          )}
        </div>

        {/* Nút tắt dẫn thẳng vào quy trình Upload BCTC mới */}
        <Link href="/statements/pipeline">
          <Button
            size="sm"
            variant="outline"
            className="h-8 border-[#006c49]/30 text-[#006c49] hover:bg-[#006c49]/10 text-xs font-semibold gap-1 px-2.5"
            title="Bắt đầu phân tích báo cáo tài chính mới"
          >
            <Plus className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">Tải BCTC mới</span>
          </Button>
        </Link>

        {/* Chuông Thông báo */}
        <button
          className="h-8 w-8 rounded-lg border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-colors relative"
          title="Thông báo hệ thống"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-[#006c49]" />
        </button>
      </div>
    </header>
  );
}
