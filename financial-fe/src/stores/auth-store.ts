import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import type { User, AuthResponse } from "@/features/auth/types";

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (authData: AuthResponse) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      isLoading: false,

      setAuth: (authData: AuthResponse) =>
        set({
          accessToken: authData.accessToken,
          refreshToken: authData.refreshToken,
          user: authData.user,
          isAuthenticated: true,
        }),

      setTokens: (accessToken: string, refreshToken: string) =>
        set((state) => ({
          accessToken,
          refreshToken,
          isAuthenticated: Boolean(accessToken),
          user: state.user,
        })),

      setUser: (user: User) =>
        set({
          user,
        }),

      logout: () =>
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: "financial-auth-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

