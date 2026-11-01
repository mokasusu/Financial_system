import type { NextConfig } from "next";

// Helper tìm biến môi trường không phân biệt hoa thường (hỗ trợ api_url, API_URL, NEXT_PUBLIC_API_URL,...)
function getEnv(key: string): string | undefined {
  if (process.env[key]) return process.env[key];
  const target = key.toLowerCase();
  for (const k of Object.keys(process.env)) {
    if (k.toLowerCase() === target && process.env[k]) {
      return process.env[k];
    }
  }
  return undefined;
}

const isProduction =
  process.env.NODE_ENV === "production" || process.env.VERCEL === "1";

// Lấy backend API URL từ các biến môi trường phổ biến (hỗ trợ cả Vercel và local)
const rawApiUrl =
  getEnv("NEXT_PUBLIC_API_URL") ||
  getEnv("API_URL") ||
  getEnv("NEXT_PUBLIC_BACKEND_URL") ||
  getEnv("BACKEND_URL") ||
  (isProduction
    ? "https://financial-system-jc3a.onrender.com"
    : "http://localhost:8080");

// Chuẩn hóa URL: loại bỏ khoảng trắng, dấu / cuối và hậu tố /api nếu người dùng nhập thừa
const backendUrl = rawApiUrl.trim().replace(/\/+$/, "").replace(/\/api$/, "");

const nextConfig: NextConfig = {
  /* config options here */
  cacheComponents: true,
  partialPrefetching: true,
  reactCompiler: true,

  // Đảm bảo client bundle luôn có NEXT_PUBLIC_API_URL đúng với môi trường
  env: {
    NEXT_PUBLIC_API_URL: backendUrl,
  },

  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
      {
        source: "/oauth2/:path*",
        destination: `${backendUrl}/oauth2/:path*`,
      },
    ];
  },

  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
