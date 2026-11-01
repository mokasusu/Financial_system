# FINANCIAL SYSTEM - FRONTEND (financial-fe)

Giao diện người dùng cho Hệ thống Thẩm định & Đánh giá Sức khỏe Tài chính Doanh nghiệp.

[![Next.js 16](https://img.shields.io/badge/Next.js-16%20(Turbopack)-000000?style=flat-square&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![React 19](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript 5](https://img.shields.io/badge/TypeScript-5.0-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS v4](https://img.shields.io/badge/Tailwind-CSS%20v4-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)

---

## 1. Tính năng Phân hệ Giao diện

* **Quy trình 7 Bước Thẩm định BCTC (`/statements/pipeline` & `/statements/[id]/analysis`):**
  * Stepper trực quan 7 bước: Tải lên $\to$ OCR $\to$ Rà soát $\to$ Tính toán $\to$ AI Đánh giá $\to$ Hiệu chỉnh $\to$ Lưu trữ.
  * Tự động nhận diện Chuẩn mực (TT200/TT133) & Niên độ kế toán từ OCR (không bắt buộc chọn thủ công).
  * Split-screen đối soát song song PDF scan và bảng chỉ tiêu kế toán draft.
* **Dashboard Tổng quan Sức khỏe Tài chính (`/dashboard`):**
  * Phân tích theo phiên BCTC (`financialStatementId`) linh hoạt.
  * Single Source of Truth TopHeader hiển thị Tên Cty, MST, dropdown chọn BCTC và nút tải mới.
  * 4 Core KPI Cards, Ma trận Cảnh báo Rủi ro Khối 3C (Color-Coded Heatmap), Cây phân rã DuPont Khối 4 và Nhận xét Thẩm định viên Khối 5.
  * Màn hình Upload Dropzone trực quan khi tài khoản chưa có dữ liệu.
* **Hồ sơ Doanh nghiệp (`/company`):**
  * Xem và Chỉnh sửa thông tin pháp nhân doanh nghiệp (`PUT /api/companies/me`).
* **Tiêu chí & Quy tắc Tài chính (`/settings`):**
  * Tùy biến ngưỡng 4 Core KPIs và 7 quy tắc cân bằng TT200/TT133.

---

## 2. Khởi chạy Giao diện (Getting Started)

### Cài đặt thư viện
```bash
npm install
```

### Cấu hình biến môi trường
Tạo file `.env.local` từ `.env.example`:
```bash
cp .env.example .env.local
```
*Ghi chú: Mặc định để trống `NEXT_PUBLIC_API_URL=` để tận dụng Next.js Proxy Rewrites (`next.config.ts`) tự động forward request sang `http://127.0.0.1:8080/api/*`, triệt tiêu hoàn toàn vấn đề CORS.*

### Chạy chế độ Phát triển (Dev Mode)
```bash
npm run dev
```
Truy cập: [http://localhost:3000](http://localhost:3000)

### Kiểm tra & Build Production
```bash
npm run build
```

---

## 3. Tài liệu Tham khảo
* Xem tài liệu đầy đủ tại thư mục gốc: [README.md](../README.md)
* [Đặc tả UI/UX Design Tokens & System](docs/UI_UX_DESIGN_SPEC.md)
* [Đặc tả Kiến trúc Thẩm định theo Phiên (Session-Based Analysis)](docs/SESSION_BASED_ANALYSIS_SPEC.md)
