# ĐẶC TẢ THIẾT KẾ: HỆ THỐNG QUẢN LÝ PHIÊN PHÂN TÍCH BCTC & TINH GỌN GIAO DIỆN

**Dự án:** Hệ thống Thẩm định & Đánh giá Sức khỏe Tài chính Doanh nghiệp  
**Phiên bản:** 2.0 (Session-based Statement Architecture)  
**Tài liệu tham chiếu:** Thông tư 200/2014/TT-BTC, Thông tư 133/2016/TT-BTC  

---

## 1. Tóm tắt Yêu cầu & Mục tiêu (Understanding Summary)

* **Quản lý Phiên BCTC theo Tệp Đã Tải Lên:** Lưu trữ từng phiên báo cáo tài chính đã upload và bóc tách thành công; cho phép người dùng chọn xem lại bất kỳ phiên nào theo đúng **Tên Tệp (`fileName`)** để nạp dữ liệu tức thì vào toàn bộ 6 khối Dashboard.
* **Tinh gọn Thanh Top Header:**
  * Bên trái: Tên Công ty hiển thị to, rõ, font đậm trang trọng (`text-base sm:text-lg font-bold text-slate-900`); Mã số thuế hiển thị nhỏ gọn, màu xám mờ ngay bên dưới (`text-xs text-slate-400 font-mono`).
  * Bên phải: Dropdown chọn tệp BCTC duy nhất hiển thị icon file cùng tên file (`fileName`).
  * **Lược bỏ 100% thông tin thừa/trùng lặp:** Gỡ bỏ dropdown chọn Chuẩn mực, Kỳ/Năm và nhãn "Đã xác thực / ShieldCheck" trên Header.
* **Tự động hóa Thông tin Chuẩn mực & Kỳ:**
  * Hiển thị bên dưới Header (trong Khối 1 của Dashboard) hoàn toàn từ kết quả bóc tách OCR của file được chọn, không bắt người dùng chọn tay.
* **Trạng thái Chưa có BCTC (Empty State):**
  * Dashboard hiển thị trực tiếp Khung kéo-thả Upload BCTC (Dropzone) ngay tại trung tâm màn hình, hỗ trợ PDF/Ảnh đến 25MB.
* **Quy trình 7 bước (Pipeline Stepper):**
  * Nhấp mục "Phân tích BCTC" trên Sidebar đi thẳng vào giao diện Tải lên mới (Bước 1).
  * Thanh tiến trình 7 bước dính sát dưới Header: Bước hiện tại sáng rõ viền `#006c49`, các bước tiếp theo làm mờ (`opacity-35 pointer-events-none`).

---

## 2. Giả định Kỹ thuật & Phi chức năng (Assumptions & NFRs)

1. **Nguyên tắc cốt lõi (Không vi phạm):** Zero client-side calculations — toàn bộ 4 KPI, 14 tỷ số, 7 đẳng thức TT200 và nhận định AI đều được tính toán độc quyền bởi backend `financial-be`.
2. **Backend Endpoints:**
   * Lấy danh sách tệp: `GET /api/financial-statements` (trả về danh sách có `id`, `fileName`, `asOfDate`, `accountingStandard`).
   * Lấy dữ liệu Dashboard theo tệp: `GET /api/dashboard/overview?financialStatementId={id}`.
3. **Hiệu năng:** Chuyển đổi giữa các phiên BCTC dưới 300ms qua cơ chế cache của TanStack Query và Zustand Store.

---

## 3. Nhật ký Quyết định (Decision Log)

| STT | Vấn đề / Thành phần | Quyết định Lựa chọn | Phương án Đã xem xét | Lý do Lựa chọn |
|---|---|---|---|---|
| 1 | Quản lý phiên BCTC | Dùng Zustand `useStatementStore` kết hợp TanStack Query | URL query param đơn thuần | Đảm bảo mọi component (Header, Dashboard, Sidebar) luôn đồng bộ phiên BCTC đang active mà không bị mất query param khi chuyển trang. |
| 2 | Bộ chọn trên Header | Chọn theo **Tên Tệp (`fileName`)**; loại bỏ chọn Chuẩn mực & Năm thủ công | Giữ lại dropdown chọn năm và chuẩn mực | Mỗi file BCTC đã có sẵn kỳ và chuẩn mực kế toán bóc tách từ OCR. Chọn trực tiếp tên file giúp giao diện sạch, chuyên nghiệp, loại bỏ nhập liệu thừa. |
| 3 | Trạng thái Dashboard chưa có BCTC | Nhúng trực tiếp Khung kéo-thả Upload BCTC tại Dashboard | Bắt người dùng mở pop-up hoặc chuyển hướng | Xóa bỏ rào cản thao tác, giúp chuyên viên tải tài liệu đầu tiên ngay lập tức. |
| 4 | Màn hình Phân tích BCTC | Đi thẳng vào giao diện Upload (Bước 1), Stepper 7 bước khóa các bước sau | Mở trang bảng danh sách lịch sử BCTC trước | Tiết kiệm 1 thao tác click, định hướng người dùng tập trung hoàn thành tuần tự 7 bước thẩm định. |
| 5 | Ma trận 14 Tỷ số Rủi ro (Khối 3C) | Hiển thị Số liệu ở trên, **Tên chỉ số** ở dưới; **loại bỏ tag chữ 'An toàn/Lưu ý'** | Hiển thị chữ 'An toàn/Lưu ý' thay vì tên chỉ số | Màu nền ô (Xanh/Vàng/Đỏ) đã chú thích trạng thái; hiển thị tên chỉ số dưới số liệu giúp chuyên viên nhận diện ngay nghiệp vụ mà không bị trùng lặp. |
| 6 | Giao diện Upload BCTC (Bước 1) | **Loại bỏ hoàn toàn ô chọn Thông tư và Năm tài chính** | Bắt người dùng chọn năm tài chính và thông tư trước | Công nghệ OCR & AI tự động đọc và nhận dạng chính xác Chuẩn mực kế toán (TT200/TT133) và Kỳ báo cáo trực tiếp từ văn bản BCTC. Loại bỏ thao tác nhập tay thừa. |

---

## 4. Kiến trúc Thành phần Chi tiết (Component Architecture)

### 4.1. Zustand Store (`src/stores/statement-store.ts`)
* `statements: FinancialStatementResponse[]`: Danh sách BCTC của công ty.
* `activeStatementId: number | null`: ID BCTC đang xem.
* `activeStatement: FinancialStatementResponse | null`: Thông tin chi tiết BCTC đang active.
* `setActiveStatementId(id: number)`: Cập nhật BCTC đang xem và kích hoạt Dashboard reload.

### 4.2. TopHeader Component (`src/components/layout/top-header.tsx`)
* Left:
  * Company Name: `text-base sm:text-lg font-bold text-slate-900`
  * Tax Code: `text-xs text-slate-400 font-mono`
* Right:
  * Select dropdown: `<FileText className="h-4 w-4 text-[#006c49]" />` + `{fileName}`
  * Option items: danh sách file BCTC + Nút `+ Tải BCTC mới` (`/statements/pipeline`).
  * User menu.

### 4.3. Dashboard Page & Ma trận 14 Tỷ số (`src/app/(dashboard)/dashboard/page.tsx`)
* Lắng nghe `activeStatementId` từ `useStatementStore`.
* Gọi `dashboardService.getOverview(selectedPeriod, activeStatementId)`.
* Hiển thị metadata (Kỳ, Chuẩn mực TT200/TT133, Tệp nguồn) tại Khối 1 lấy từ dữ liệu OCR của `activeStatement`.
* Nếu `statements.length === 0`: Hiển thị `UploadDropzoneCard` ngay giữa Dashboard.
* **Khối 3C - Ma trận Cảnh báo Rủi ro 14 Tỷ số:**
  * Mỗi ô hiển thị 2 dòng:
    * Dòng trên: Giá trị số liệu (`text-xs font-mono font-bold`, ví dụ: `1.96x`, `145 ngày`, `9.24%`).
    * Dòng dưới: Tên chỉ số tài chính (`text-[10px] font-medium opacity-95 truncate mt-0.5`, ví dụ: `Current Ratio`, `Vòng quay tồn kho`, `Biên LN Ròng`).
  * Tuyệt đối không để tag chữ `"An toàn"` hay `"Lưu ý"` vì mã màu nền (Xanh/Vàng/Đỏ) đã thể hiện trực quan trạng thái theo bảng chú thích bên dưới.

### 4.4. Pipeline Stepper (`src/features/statements/components/pipeline-stepper.tsx`)
* Chuẩn hóa 7 bước nằm ngang với tên ngắn gọn, không lặp lại số thứ tự (số thứ tự nằm gọn trong icon circle `[ 1 ]` đến `[ 7 ]`):
  1. `Tải lên` (Active khi step = 1)
  2. `OCR` (Active khi step = 2)
  3. `Rà soát` (Active khi step = 3)
  4. `Tính toán` (Active khi step = 4)
  5. `AI Đánh giá` (Active khi step = 5)
  6. `Hiệu chỉnh` (Active khi step = 6)
  7. `Lưu trữ` (Active khi step = 7)
* Bước hiện tại: Viền sáng `#006c49`, font đậm. Bước đã qua: màu xanh ngọc nhạt `✓`. Bước chưa tới: `opacity-40 pointer-events-none`.

---

## 5. Bảng Chuẩn hóa Rà soát Loại bỏ Thông tin Trùng lặp (UI Deduplication)

| Vị trí / Thành phần | Trước khi chuẩn hóa | Sau khi loại bỏ trùng lặp | Nguyên tắc & Lý do |
| :--- | :--- | :--- | :--- |
| **TopHeader** | Tên Cty + MST + Dropdown BCTC | **Single Source of Truth** cho Tên Cty, MST và Tệp BCTC | Đặt cố định ở đỉnh màn hình, luôn hiển thị |
| **Sidebar** | Có khung thông tin Doanh nghiệp & MST | **Loại bỏ hoàn toàn khung doanh nghiệp** | Tránh lặp lại với TopHeader ngay phía trên bên phải |
| **Dashboard (Khối 1)** | Tiêu đề chứa `{Tên Cty} - DASHBOARD...`, dòng dưới có MST và Tên tệp nguồn | Tiêu đề rút gọn chuẩn: `"BÁO CÁO TỔNG QUAN SỨC KHỎE TÀI CHÍNH"`. Dòng dưới chỉ giữ `Kỳ BCTC` và `Chuẩn mực` | Loại bỏ lặp Tên Cty, MST, Tên file (đã có trên TopHeader) |
| **Thanh tiến trình 7 bước** | Badge `1` đi kèm label `"1. Tải lên BCTC"` dẫn đến hiển thị `1 1.upload...` | Badge `[ 1 ]` đi kèm label `"Tải lên"` sạch sẽ, ngăn cách bằng mũi tên `ChevronRight` | Khắc phục lỗi lặp số thứ tự bước, nhãn ngắn gọn, tinh gọn |
| **Trang Tải lên (`/pipeline`)** | Card header ghi `"Bước 1: Tải lên tài liệu..."` ngay dưới Stepper | Rút gọn thành `"Tải lên tài liệu Báo cáo Tài chính (PDF / Scan)"` | Stepper phía trên đã ghi rõ trạng thái `[ 1 ] Tải lên` |

