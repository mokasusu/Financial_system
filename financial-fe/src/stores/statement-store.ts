import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { FinancialStatement } from "@/features/statements/types";

interface StatementState {
  statements: FinancialStatement[];
  activeStatementId: number | null;
  activeStatement: FinancialStatement | null;
  setStatements: (statements: FinancialStatement[]) => void;
  setActiveStatementId: (id: number | null) => void;
  clear: () => void;
}

export const useStatementStore = create<StatementState>()(
  persist(
    (set, get) => ({
      statements: [],
      activeStatementId: null,
      activeStatement: null,

      setStatements: (statements: FinancialStatement[]) => {
        const currentActiveId = get().activeStatementId;
        // Verify if currently active statement still exists in new statements list
        const exists = statements.find((s) => s.id === currentActiveId);

        let newActive: FinancialStatement | null = null;
        let newActiveId: number | null = null;

        if (exists) {
          newActive = exists;
          newActiveId = exists.id;
        } else if (statements.length > 0) {
          // Default to the most recent statement
          newActive = statements[0];
          newActiveId = statements[0].id;
        }

        set({
          statements,
          activeStatement: newActive,
          activeStatementId: newActiveId,
        });
      },

      setActiveStatementId: (id: number | null) => {
        if (!id) {
          set({ activeStatementId: null, activeStatement: null });
          return;
        }
        const found = get().statements.find((s) => s.id === id) || null;
        set({
          activeStatementId: id,
          activeStatement: found,
        });
      },

      clear: () =>
        set({
          statements: [],
          activeStatementId: null,
          activeStatement: null,
        }),
    }),
    {
      name: "statement-storage",
      partialize: (state) => ({
        activeStatementId: state.activeStatementId,
      }),
    }
  )
);
