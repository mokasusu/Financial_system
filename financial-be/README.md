# FINANCIAL SYSTEM - BACKEND (financial-be)

Hệ thống Xử lý, Tính toán & Thẩm định Sức khỏe Tài chính Doanh nghiệp.

[![Java 21](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![OpenAPI / Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=flat-square&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)

---

## 1. Kiến trúc Backend

* **Strict Server-Side Calculation:** Mọi phép tính 14 tỷ số tài chính, phương trình cân đối TT200/TT133, phân rã DuPont và ma trận cảnh báo rủi ro được xử lý độc quyền tại Backend sử dụng `BigDecimal` với `RoundingMode.HALF_UP`.
* **Sơ đồ Kiến trúc Hệ thống:** Xem chi tiết sơ đồ luồng tổng thể và máy trạng thái tại [README.md - Sơ đồ Kiến trúc Hệ thống](../README.md#3-sơ-đồ-kiến-trúc-hệ-thống-system-architecture).
* **Domain-Driven Modules:**
  * `auth`: Xác thực JWT Stateless (Access + Refresh Token), Google OAuth2.
  * `company`: Quản lý hồ sơ công ty và chuẩn mực kế toán.
  * `financialstatement`: Quản lý BCTC, nạp file PDF/Scan, quy trình 7 bước.
  * `financialindicator`: Quản lý chỉ tiêu kế toán thô trích xuất từ OCR.
  * `calculationengine`: Engine tính toán 14 tỷ số và DuPont.
  * `validation`: Engine kiểm tra 7 phương trình cân bằng kế toán.
  * `riskanalysis`: Engine phân tích và xây dựng ma trận cảnh báo rủi ro.
  * `assessment`: Thẩm định và chấm điểm tổng hợp (0 - 100).
  * `aianalysis`: Kết nối OpenRouter LLM sinh nhận định chuyên gia.
  * `dashboard`: API tổng hợp dữ liệu Overview theo phiên BCTC.
  * `evaluationcriteria`: Quản lý ngưỡng chỉ số tài chính tùy biến.

---

## 2. Khởi chạy Backend

### Cấu hình môi trường
Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```
Cập nhật thông tin kết nối MySQL và các API Key (JWT, OCR, OpenRouter).

### Chạy ứng dụng
* **Windows (PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **Linux/macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

### Kiểm tra Swagger UI
* URL: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* API Docs JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 3. Tài liệu Kỹ thuật Chi tiết
* Xem tài liệu đầy đủ tại thư mục gốc: [README.md](../README.md)
* [Hướng dẫn Tích hợp API Frontend](docs/FRONTEND_INTEGRATION_GUIDE.md)
* [Đặc tả Thiết kế Swagger API](docs/SWAGGER_API_DESIGN.md)

