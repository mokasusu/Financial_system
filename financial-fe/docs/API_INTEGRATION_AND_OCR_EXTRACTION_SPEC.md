# ĐẶC TẢ CHUẨN HÓA DỮ LIỆU THẬT TỪ API & TÍCH HỢP OCR EXTRACTION (/api/v1/extract)
## SPECIFICATION: REAL API DATA INTEGRATION & ASYNC OCR AUTO-TRANSITION

---

## 1. Tóm tắt Mục tiêu & Phạm vi (Understanding Summary)
* **Mục tiêu:** Rà soát và loại bỏ **100% dữ liệu mẫu / fix cứng / hardcoded Vinamilk fallback** trên toàn bộ giao diện Frontend (`financial-fe`). Chuyển hoàn toàn sang nạp và kết xuất dữ liệu động trả về từ API Backend (`financial-be`).
* **Tích hợp OCR Extraction:** Chuẩn hóa luồng bóc tách dữ liệu BCTC qua endpoint `/api/v1/extract` của dịch vụ OCR. Tại Bước 2 (OCR), Frontend tự động thăm dò tiến trình xử lý từ Backend và **tự động chuyển sang Bước 3 (Rà soát)** ngay khi bóc tách hoàn tất.
* **Nguyên tắc Cốt lõi:**
  * **Zero Client-Side Calculation:** Mọi tỷ số tài chính, phương trình cân đối và điểm số tổng hợp đều được xử lý độc quyền tại Backend.
  * **No Mocking / Zero Fake Data:** Tuyệt đối không hiển thị số liệu giả định khi người dùng tải BCTC thực tế. Nếu phiên BCTC chưa chạy tính toán/AI, hiển thị dấu `—` hoặc Empty Card kèm nút dẫn tới bước thực thi.

---

## 2. Các Giả định Kỹ thuật (Assumptions)
* **Bất đồng bộ (Async Background Job):** Bóc tách OCR là tác vụ tốn thời gian (10s – 60s), do đó xử lý ngầm qua ThreadPool `ocrTaskExecutor` trên Backend để chống HTTP 504 Gateway Timeout.
* **Polling 2.5s:** Frontend Bước 2 thăm dò trạng thái qua `GET /api/financial-statements/{id}/status` mỗi 2.5 giây.
* **Bảo mật:** Tất cả các endpoint đều yêu cầu xác thực JWT Bearer Token, kiểm tra quyền sở hữu BCTC của công ty thuộc về tài khoản người dùng đang đăng nhập.

---

## 3. Nhật ký Quyết định (Decision Log)

| Quyết định | Phương án lựa chọn | Lý do chọn |
| :--- | :--- | :--- |
| **Kiến trúc bóc tách OCR** | Phương án 1: Async Background Job + Polling 2.5s + Tự động chuyển bước 3 | Tránh HTTP 504 Timeout khi tài liệu nhiều trang; người dùng F5 không bị mất tiến trình. |
| **Endpoint OCR Microservice** | Đổi sang `/api/v1/extract` trong `OcrApiClient` | Khớp chuẩn endpoint bóc tách dữ liệu của dịch vụ OCR Microservice. |
| **Endpoint kích hoạt OCR Backend** | Bổ sung `POST /api/financial-statements/{id}/extract` | Cho phép FE kích hoạt lại hoặc chạy bóc tách theo yêu cầu khi cần retry. |
| **Dữ liệu Điểm tổng hợp Khối 1** | Bổ sung `overallScore` & `healthClassification` vào `DashboardOverviewResponse` | Giữ nguyên tắc Zero Client Calculation; FE chỉ cần gọi 1 API `GET /api/dashboard/overview`. |
| **Loại bỏ dữ liệu giả lập** | Gỡ bỏ 100% demo Vinamilk fallback & nút Demo; hiển thị `—` khi chưa có dữ liệu | Đảm bảo tính trung thực và chuyên nghiệp của dữ liệu BCTC thực tế. |

---

## 4. Thiết kế Kỹ thuật Chi tiết (Detailed Technical Design)

### 4.1. Phân hệ Backend (`financial-be`)

#### A. Chuẩn hóa OCR Client & Controller
1. **[`OcrApiClient.java`](file:///c:/Users/admin/Desktop/A_Ky1_Nam4/Cong_nghe_tai_chinh/Financial_system/financial-be/src/main/java/com/financial/modules/financialstatement/client/OcrApiClient.java):**
   - Đổi URI WebClient từ `/api/v1/analyze` sang `/api/v1/extract`.
   - Gửi multipart file cùng các tham số `period`, `enable_ai=true`, `store_images=true`.
2. **[`FinancialStatementController.java`](file:///c:/Users/admin/Desktop/A_Ky1_Nam4/Cong_nghe_tai_chinh/Financial_system/financial-be/src/main/java/com/financial/modules/financialstatement/controller/FinancialStatementController.java):**
   - Thêm endpoint `POST /api/financial-statements/{id}/extract`: Trả về `202 Accepted`, kích hoạt lại bóc tách OCR ngầm cho BCTC.
   - Endpoint `GET /api/financial-statements/{id}/status`: Trả về trạng thái tiến trình (`QUEUED`, `PROCESSING`, `REVIEWING_DATA`, `COMPLETED`, `FAILED`).
   - Endpoint `GET /api/financial-statements/{id}/items`: Trả về danh sách chỉ tiêu kế toán đã trích xuất từ OCR.
   - Endpoint `PUT /api/financial-statements/{id}/items`: Lưu danh sách chỉ tiêu sau khi chuyên viên chỉnh sửa.

#### B. Mở rộng Dashboard Overview Response
1. **[`DashboardOverviewResponse.java`](file:///c:/Users/admin/Desktop/A_Ky1_Nam4/Cong_nghe_tai_chinh/Financial_system/financial-be/src/main/java/com/financial/modules/dashboard/dto/DashboardOverviewResponse.java):**
   ```java
   @Schema(description = "Overall financial health score (0-100)", example = "88.5")
   private BigDecimal overallScore;

   @Schema(description = "Overall financial health classification level", example = "SAFE")
   private HealthClassification healthClassification;

   @Schema(description = "Display name of health classification", example = "AN TOÀN")
   private String healthClassificationDisplayName;
   ```
2. **[`DashboardServiceImpl.java`](file:///c:/Users/admin/Desktop/A_Ky1_Nam4/Cong_nghe_tai_chinh/Financial_system/financial-be/src/main/java/com/financial/modules/dashboard/service/impl/DashboardServiceImpl.java):**
   - Map `overallScore`, `healthClassification`, và `healthClassificationDisplayName` từ thực thể `Assessment` mới nhất của BCTC vào `DashboardOverviewResponse`.

---

### 4.2. Phân hệ Frontend (`financial-fe`)

#### A. Pipeline Phân tích 7 Bước (`/statements/[id]/analysis`)
1. **Bước 2: Bóc tách OCR & Tự động Chuyển bước:**
   - Polling trạng thái qua `statementService.getStatus(statementId)` với `refetchInterval: 2500ms`.
   - Hiển thị animation quét tài liệu và trạng thái thật (`QUEUED` / `PROCESSING`).
   - `useEffect` phát hiện khi `status === "REVIEWING_DATA"` hoặc `"COMPLETED"`, **tự động gọi `goToStep(3)`**.
   - Nếu `status === "FAILED"`: Hiển thị thông báo lỗi từ backend kèm nút "Thử lại bóc tách OCR".
2. **Bước 3: Rà soát & Đối soát Chỉ tiêu Thực:**
   - Xóa bỏ toàn bộ các dòng demo mẫu fix cứng Vinamilk (`35.421 tỷ`, v.v.).
   - Nạp dữ liệu thực tế từ `GET /api/financial-statements/{id}/indicators`.
   - Cho phép chuyên viên chỉnh sửa trực tiếp trên bảng draft và lưu chốt qua `PUT /api/financial-statements/{id}/indicators/bulk`.
   - Cột xem trước: Tích hợp thông tin tài liệu thực và nút mở file gốc.
3. **Bước 5 & 6: AI Thẩm định & Hiệu chỉnh:**
   - Nạp nhận xét thực tế từ `GET /api/financial-statements/{id}/ai-analysis`.
   - Cho phép thẩm định viên biên tập nhận xét và lưu lại.

#### B. Dashboard Không Dữ liệu Giả Lập (`/dashboard`)
1. **Loại bỏ Nút Demo Vinamilk:**
   - Xóa `showSamplePreview` và nút "Xem giao diện mẫu (Vinamilk Demo)". Nếu chưa có BCTC, chỉ hiển thị Upload Dropzone.
2. **Khối 1 Global Context Header:**
   - Nhận `overallScore` và `healthClassification` từ `overview` trả về.
   - Nếu chưa có assessment: Hiển thị `— / 100` và trạng thái `CHƯA ĐÁNH GIÁ`.
3. **Khối 3C Risk Heatmap & 3D Validation:**
   - Xóa bỏ `defaultMatrix` (16 tỷ số mẫu) và `defaultValidationRules` (7 quy tắc mẫu).
   - Render dữ liệu thực từ `overview.financialRatios` và `overview.validation`.
   - Nếu chưa tính toán: Hiển thị Empty Card hướng dẫn kích hoạt Bước 4.
4. **Khối 5 AI Commentary:**
   - Xóa bỏ các đoạn văn bản mẫu Vinamilk. Render nội dung thực từ `overview.aiCommentary`.

---

## 5. Kế hoạch Kiểm thử & Xác minh (Verification Plan)
1. **Kiểm thử Biên dịch:**
   - Backend: `./mvnw test-compile` thành công 100%.
   - Frontend: `npm run build` thành công 100% với 0 lỗi TypeScript / ESLint.
2. **Kiểm thử Luồng Tải lên & OCR:**
   - Tải lên 1 tệp BCTC $\to$ Bước 2 chuyển sang polling $\to$ Khi OCR hoàn tất, tự động chuyển sang Bước 3 $\to$ Hiển thị đúng danh sách chỉ tiêu đã trích xuất từ OCR $\to$ Chốt số liệu $\to$ Tính toán Bước 4 $\to$ Sinh nhận định AI Bước 5 $\to$ Phê duyệt Bước 7 $\to$ Điều hướng về Dashboard với đầy đủ số liệu thực tế.

