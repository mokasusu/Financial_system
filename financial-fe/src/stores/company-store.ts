import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { Company } from "@/features/company/types";

interface CompanyState {
  currentCompany: Company | null;
  selectedPeriod: string;
  accountingStandard: "TT200" | "TT133" | "TT99";
  setCurrentCompany: (company: Company | null) => void;
  setSelectedPeriod: (period: string) => void;
  setAccountingStandard: (standard: "TT200" | "TT133" | "TT99") => void;
  resetCompany: () => void;
}

export const useCompanyStore = create<CompanyState>()(
  persist(
    (set) => ({
      currentCompany: null,
      selectedPeriod: "2024",
      accountingStandard: "TT200",

      setCurrentCompany: (company) => set({ currentCompany: company }),
      setSelectedPeriod: (period) => set({ selectedPeriod: period }),
      setAccountingStandard: (standard) => set({ accountingStandard: standard }),
      resetCompany: () =>
        set({
          currentCompany: null,
          selectedPeriod: "2024",
          accountingStandard: "TT200",
        }),
    }),
    {
      name: "financial-company-storage",
      storage: createJSONStorage(() => localStorage),
    }
  )
);

