# TÀI LIỆU THIẾT KẾ CHUẨN HÓA OPENAPI / SWAGGER (SWAGGER_API_DESIGN)

**Dự án:** Hệ thống Đánh giá Sức khỏe Tài chính Doanh nghiệp (`financial-be`)  
**Mục tiêu:** Bổ sung và chuẩn hóa toàn diện tài liệu OpenAPI / Swagger bằng Tiếng Việt phục vụ báo cáo đồ án và tích hợp Frontend  
**Ngày phê duyệt:** 01/11/2026  
**Trạng thái:** Đã phê duyệt (Validated & Approved)

---

## 1. TỔNG KẾT HIỂU BIẾT & PHẠM VI (UNDERSTANDING SUMMARY)

* **Mục đích:** Chuẩn hóa toàn bộ tài liệu API thông qua thư viện `springdoc-openapi-starter-webmvc-ui` (OpenAPI 3.0), hiển thị trực quan trên giao diện Swagger UI (`/swagger-ui.html`).
* **Đối tượng sử dụng:**
  - Hội đồng chấm đồ án / Giảng viên hướng dẫn môn học Công nghệ tài chính.
  - Lập trình viên Frontend (Web/Mobile) phục vụ tra cứu và tích hợp.
  - Tester / QA kiểm thử API trực tiếp qua chức năng "Try it out".
* **Ngôn ngữ:** 100% Tiếng Việt chuyên ngành tài chính – kế toán và công nghệ phần mềm.
* **Phạm vi kỹ thuật:**
  - 16 REST Controllers thuộc 11 modules nghiệp vụ.
  - Các Request DTOs và Response DTOs cốt lõi.
  - Bảng mã lỗi HTTP Status (`200`, `201`, `202`, `400`, `401`, `403`, `404`, `409`).
* **Ràng buộc bất biến:**
  - Tuyệt đối không thay đổi mã logic nghiệp vụ backend.
  - Giữ nguyên cấu trúc dữ liệu JSON trả về (chỉ bổ sung metadata annotations).
  - Bảo đảm toàn bộ 374 unit và integration test tiếp tục `BUILD SUCCESS`.

---

## 2. GIẢ ĐỊNH KỸ THUẬT (ASSUMPTIONS)

1. **Thư viện tích hợp:** Tiếp tục sử dụng `springdoc-openapi-starter-webmvc-ui:3.2.0` đã có sẵn trong `pom.xml`.
2. **Cấu hình Security Scheme:** Giữ nguyên Scheme `BearerAuth` (JWT HTTP Bearer) tại `OpenApiConfig.java`.
3. **Tiêu chuẩn `@Schema` trên DTO:** Sử dụng dữ liệu mẫu thực tế của doanh nghiệp Việt Nam (ví dụ: mã số thuế 10 số `"0101234567"`, kỳ báo cáo `"2024"`, đơn vị tiền tệ VNĐ...).

---

## 3. NHẬT KÝ QUYẾT ĐỊNH (DECISION LOG)

| Mã Quyết định | Nội dung quyết định | Các phương án đã cân nhắc | Lý do lựa chọn phương án |
| :--- | :--- | :--- | :--- |
| **DEC-01** | Chọn Phương án 1: Chuẩn hóa phân tầng theo Module (Controller + DTOs) | - Phương án 2: Chỉ sửa Controller<br>- Phương án 3: Sửa 100% DTOs + Tách Tab GroupedOpenApi | Tạo ra tài liệu API hoàn hảo nhất cho việc chấm điểm đồ án, có sẵn mẫu JSON thử nghiệm ngay mà không làm phức tạp hóa UI. |
| **DEC-02** | Phân nhóm 10 phân hệ có đánh số thứ tự từ `01` đến `10` | - Nhóm theo kỹ thuật (CRUD/Report)<br>- Nhóm không đánh số thứ tự | Giúp menu Swagger UI sắp xếp tuần tự theo đúng luồng người dùng (User Journey). |
| **DEC-03** | Bộ dữ liệu mẫu `@Schema` chuẩn bối cảnh tài chính VN | - Dữ liệu mẫu tiếng Anh (`"test@example.com"`)<br>- Để trống example | Giúp giảng viên và tester thấy rõ dữ liệu nghiệp vụ thực tế khi bấm "Try it out". |

---

## 4. CHI TIẾT THIẾT KẾ (FINAL DESIGN)

### 4.1. Hệ thống Phân loại Tags (10 Phân hệ chuẩn)

| Thứ tự | Tên Tag trên Swagger UI | Danh sách Controller trực thuộc |
| :---: | :--- | :--- |
| **01** | `01. Xác thực & Tài khoản` | `AuthController`, `UserController` |
| **02** | `02. Hồ sơ Doanh nghiệp` | `CompanyController` |
| **03** | `03. Báo cáo Tài chính & OCR` | `FinancialStatementController` |
| **04** | `04. Bảng chỉ tiêu & Ánh xạ Kế toán` | `FinancialIndicatorController`, `AccountingMappingController`, `FinancialRatioController` |
| **05** | `05. Kiểm định Báo cáo Tài chính` | `ValidationController`, `ValidationRuleController` |
| **06** | `06. Tính toán Chỉ số Tài chính` | `CalculationController` |
| **07** | `07. Đánh giá Sức khỏe Doanh nghiệp`| `AssessmentController`, `EvaluationCriteriaController` |
| **08** | `08. Phân tích Rủi ro Tài chính` | `RiskAnalysisController`, `RiskRuleController` |
| **09** | `09. Trợ lý Phân tích AI` | `AIAnalysisController` |
| **10** | `10. Trung tâm Điều hành Dashboard` | `DashboardController` |

---

### 4.2. Tiêu chuẩn Annotation trên Controller Method

```java
@PostMapping("/endpoint")
@Operation(
    summary = "Tóm tắt hành động ngắn gọn bằng Tiếng Việt",
    description = "Mô tả chi tiết mục đích, điều kiện tiên quyết (ví dụ: yêu cầu quyền sở hữu BCTC đã xác nhận)."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Xử lý thành công"),
    @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc vi phạm quy tắc kế toán"),
    @ApiResponse(responseCode = "401", description = "Chưa xác thực JWT Bearer token"),
    @ApiResponse(responseCode = "404", description = "Không tìm thấy dữ liệu yêu cầu")
})
public ResponseEntity<ApiResponse<DataResponse>> handle(
    @Parameter(description = "Mô tả tham số", example = "giatri_mau")
    @RequestParam(required = false) String param
) { ... }
```

---

### 4.3. Tiêu chuẩn Annotation trên DTO Fields

```java
@Schema(description = "Mã số thuế doanh nghiệp (10 hoặc 13 số)", example = "0101234567")
private String taxCode;

@Schema(description = "Kỳ báo cáo tài chính (năm hoặc quý)", example = "2024")
private String period;

@Schema(description = "Giá trị tính toán của chỉ số", example = "1.85")
private BigDecimal value;
```

---

## 5. LỘ TRÌNH THỰC HIỆN & KIỂM THỬ

1. **Giai đoạn 1:** Cập nhật nhóm Xác thực & Doanh nghiệp (`AuthController`, `UserController`, `CompanyController` & DTOs).
2. **Giai đoạn 2:** Cập nhật nhóm BCTC & Ánh xạ kế toán (`FinancialStatementController`, `FinancialIndicatorController`, `AccountingMappingController`, `FinancialRatioController`).
3. **Giai đoạn 3:** Cập nhật nhóm Pipeline Động cơ (`ValidationController`, `CalculationController`, `AssessmentController`, `RiskAnalysisController` & Rules).
4. **Giai đoạn 4:** Cập nhật nhóm AI Analysis & Dashboard (`AIAnalysisController`, `DashboardController`).
5. **Giai đoạn 5:** Biên dịch kiểm tra cú pháp (`mvn test-compile`) và chạy toàn bộ **374 tests** (`mvn test`) đảm bảo zero regression.

