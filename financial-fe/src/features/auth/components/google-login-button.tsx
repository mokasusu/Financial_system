"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import { authService } from "@/features/auth/services";
import { companyService } from "@/features/company/services";
import { useAuthStore } from "@/stores/auth-store";
import { useCompanyStore } from "@/stores/company-store";

interface GoogleCredentialResponse {
  credential: string;
  select_by?: string;
}

interface GoogleLoginButtonProps {
  text?: "signin_with" | "signup_with" | "continue_with";
  onError?: (message: string) => void;
  disabled?: boolean;
}

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: GoogleCredentialResponse) => void;
            auto_select?: boolean;
            cancel_on_tap_outside?: boolean;
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              type?: "standard" | "icon";
              theme?: "outline" | "filled_blue" | "filled_black";
              size?: "large" | "medium" | "small";
              text?: "signin_with" | "signup_with" | "continue_with" | "signin";
              shape?: "rectangular" | "pill" | "circle" | "square";
              logo_alignment?: "left" | "center";
              width?: string | number;
              locale?: string;
            }
          ) => void;
          prompt?: () => void;
        };
      };
    };
  }
}

export function GoogleLoginButton({
  text = "signin_with",
  onError,
  disabled = false,
}: GoogleLoginButtonProps) {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);
  const setCurrentCompany = useCompanyStore((state) => state.setCurrentCompany);

  const containerRef = useRef<HTMLDivElement>(null);
  const [isVerifying, setIsVerifying] = useState(false);
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  const handleCredentialResponse = useCallback(
    async (response: GoogleCredentialResponse) => {
      if (!response.credential) {
        onError?.("Không nhận được chứng thực từ Google. Vui lòng thử lại.");
        return;
      }

      try {
        setIsVerifying(true);

        // 1. Xác thực ID token với Spring Boot backend
        const authResponse = await authService.loginWithGoogle({
          idToken: response.credential,
        });

        setAuth(authResponse);

        // 2. Kiểm tra hồ sơ công ty của người dùng để điều hướng
        try {
          const company = await companyService.getMyCompany();
          setCurrentCompany(company);
          router.push("/dashboard");
        } catch (companyErr: unknown) {
          const status = (companyErr as { status?: number })?.status;
          if (status === 404) {
            router.push("/onboarding");
          } else {
            router.push("/dashboard");
          }
        }
      } catch (err: unknown) {
        const message =
          (err as { message?: string })?.message ||
          "Đăng nhập bằng tài khoản Google không thành công. Vui lòng thử lại.";
        onError?.(message);
      } finally {
        setIsVerifying(false);
      }
    },
    [onError, router, setAuth, setCurrentCompany]
  );

  useEffect(() => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

    if (!clientId) {
      console.warn(
        "[GoogleAuth] Thiếu biến môi trường NEXT_PUBLIC_GOOGLE_CLIENT_ID"
      );
      return;
    }

    const initGoogleSignIn = () => {
      if (!window.google?.accounts?.id || !containerRef.current) return;

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: handleCredentialResponse,
        cancel_on_tap_outside: true,
      });

      // Clear previous button render if any
      containerRef.current.innerHTML = "";

      window.google.accounts.id.renderButton(containerRef.current, {
        type: "standard",
        theme: "outline",
        size: "large",
        text: text,
        shape: "rectangular",
        logo_alignment: "left",
        width: 360,
      });

      setIsScriptLoaded(true);
    };

    // Nếu script đã có sẵn trong trang
    if (window.google?.accounts?.id) {
      initGoogleSignIn();
      return;
    }

    // Nếu chưa có, nạp script Google Identity Services
    const existingScript = document.getElementById("google-gsi-script");
    if (!existingScript) {
      const script = document.createElement("script");
      script.id = "google-gsi-script";
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = () => {
        initGoogleSignIn();
      };
      script.onerror = () => {
        console.error("[GoogleAuth] Không thể tải Google Identity Services SDK");
      };
      document.body.appendChild(script);
    } else {
      existingScript.addEventListener("load", initGoogleSignIn);
      return () => {
        existingScript.removeEventListener("load", initGoogleSignIn);
      };
    }
  }, [handleCredentialResponse, text]);

  return (
    <div className="relative w-full flex flex-col items-center justify-center min-h-[44px]">
      {isVerifying ? (
        <div className="w-full h-10 border border-slate-200 rounded-md bg-slate-50 flex items-center justify-center text-xs text-slate-600 font-medium gap-2 shadow-xs">
          <Loader2 className="h-4 w-4 animate-spin text-[#006c49]" />
          Đang xác thực tài khoản Google...
        </div>
      ) : (
        <div
          ref={containerRef}
          className={`w-full flex justify-center [&>div]:w-full ${
            disabled ? "pointer-events-none opacity-60" : ""
          } ${!isScriptLoaded ? "invisible h-0" : ""}`}
        />
      )}

      {/* Placeholder trong lúc chờ script GIS tải xong */}
      {!isScriptLoaded && !isVerifying && (
        <div className="w-full h-10 border border-slate-200 rounded-md bg-white flex items-center justify-center text-xs text-slate-500 font-normal shadow-xs animate-pulse">
          Đang khởi tạo Google Sign-In...
        </div>
      )}
    </div>
  );
}

