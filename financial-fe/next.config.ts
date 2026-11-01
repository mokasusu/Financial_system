import type { NextConfig } from "next";

// Lấy backend API URL từ các biến môi trường phổ biến (hỗ trợ cả Vercel và local)
const rawApiUrl =
  process.env.API_URL ||
  process.env.BACKEND_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  process.env.NEXT_PUBLIC_BACKEND_URL ||
  "http://localhost:8080";

// Chuẩn hóa URL: loại bỏ khoảng trắng, dấu / cuối và hậu tố /api nếu người dùng nhập thừa
const backendUrl = rawApiUrl.trim().replace(/\/+$/, "").replace(/\/api$/, "");

const nextConfig: NextConfig = {
  /* config options here */
  cacheComponents: true,
  partialPrefetching: true,
  reactCompiler: true,

  // Đảm bảo client bundle luôn có NEXT_PUBLIC_API_URL ngay cả khi trên Vercel người dùng đặt tên API_URL
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
