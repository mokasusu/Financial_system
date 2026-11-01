# Danh sách chỉ tiêu OCR cần nhận diện

## 1. Mục đích

Danh sách này xác định các chỉ tiêu tài chính mà dịch vụ OCR cần nhận diện từ báo cáo tài chính để phục vụ hệ thống Enterprise Health Assessment System.

OCR cần nhận diện chỉ tiêu theo từng báo cáo, kỳ kế toán và chế độ kế toán (TT99, TT200, TT133). Không được tự suy diễn hoặc tự điền giá trị 0 khi không nhận diện được dữ liệu.

## 2. Danh sách chỉ tiêu

| STT | Indicator Key | Tên chỉ tiêu | Báo cáo nguồn |
|---:|---|---|---|
| 1 | `CASH_AND_EQUIVALENTS` | Tiền và các khoản tương đương tiền | Bảng cân đối kế toán |
| 2 | `CURRENT_ASSETS` | Tài sản ngắn hạn | Bảng cân đối kế toán |
| 3 | `INVENTORY` | Hàng tồn kho | Bảng cân đối kế toán |
| 4 | `RECEIVABLES` | Các khoản phải thu | Bảng cân đối kế toán |
| 5 | `NON_CURRENT_ASSETS` | Tài sản dài hạn | Bảng cân đối kế toán |
| 6 | `TOTAL_ASSETS` | Tổng cộng tài sản | Bảng cân đối kế toán |
| 7 | `CURRENT_LIABILITIES` | Nợ ngắn hạn | Bảng cân đối kế toán |
| 8 | `PAYABLES` | Các khoản phải trả | Bảng cân đối kế toán |
| 9 | `TOTAL_LIABILITIES` | Nợ phải trả | Bảng cân đối kế toán |
| 10 | `EQUITY` | Vốn chủ sở hữu | Bảng cân đối kế toán |
| 11 | `NET_REVENUE` | Doanh thu thuần về bán hàng và cung cấp dịch vụ | Báo cáo kết quả hoạt động kinh doanh |
| 12 | `COST_OF_GOODS_SOLD` | Giá vốn hàng bán | Báo cáo kết quả hoạt động kinh doanh |
| 13 | `GROSS_PROFIT` | Lợi nhuận gộp | Báo cáo kết quả hoạt động kinh doanh |
| 14 | `OPERATING_PROFIT` | Lợi nhuận thuần từ hoạt động kinh doanh | Báo cáo kết quả hoạt động kinh doanh |
| 15 | `NET_PROFIT` | Lợi nhuận sau thuế thu nhập doanh nghiệp | Báo cáo kết quả hoạt động kinh doanh |
| 16 | `NET_CASH_FROM_OPERATING_ACTIVITIES` | Lưu chuyển tiền thuần từ hoạt động kinh doanh | Báo cáo lưu chuyển tiền tệ |
| 17 | `NET_CASH_FROM_INVESTING_ACTIVITIES` | Lưu chuyển tiền thuần từ hoạt động đầu tư | Báo cáo lưu chuyển tiền tệ |
| 18 | `NET_CASH_FROM_FINANCING_ACTIVITIES` | Lưu chuyển tiền thuần từ hoạt động tài chính | Báo cáo lưu chuyển tiền tệ |
| 19 | `NET_CASH_FLOW` | Lưu chuyển tiền thuần trong kỳ | Báo cáo lưu chuyển tiền tệ |

## 3. Yêu cầu nhận diện số dư đầu kỳ và cuối kỳ

Đối với các chỉ tiêu dưới đây, OCR cần trích xuất giá trị theo cả hai thời điểm đầu kỳ và cuối kỳ khi báo cáo có cung cấp:

| STT | Indicator Key | Giá trị cần nhận diện |
|---:|---|---|
| 1 | `TOTAL_ASSETS` | Đầu kỳ và cuối kỳ |
| 2 | `EQUITY` | Đầu kỳ và cuối kỳ |
| 3 | `INVENTORY` | Đầu kỳ và cuối kỳ |
| 4 | `RECEIVABLES` | Đầu kỳ và cuối kỳ |
| 5 | `PAYABLES` | Đầu kỳ và cuối kỳ |

Đầu kỳ/cuối kỳ là chiều dữ liệu của cùng một chỉ tiêu, không tạo Indicator Key mới. Với báo cáo có cột kỳ hiện tại/kỳ trước, cần giữ nguyên ý nghĩa và kỳ tham chiếu của từng cột; không tự giả định kỳ trước luôn đồng nghĩa với số dư đầu kỳ.

## 4. Metadata bắt buộc cho từng chỉ tiêu

| Trường | Mô tả |
|---|---|
| `code` | Mã chỉ tiêu trên báo cáo (nếu có) |
| `key` | Indicator Key chuẩn hóa |
| `label` | Tên chỉ tiêu chuẩn hóa |
| `raw_label` | Tên gốc nhận diện được trên tài liệu |
| `current` | Giá trị thuộc kỳ hiện tại theo cấu trúc báo cáo |
| `previous` | Giá trị thuộc kỳ trước theo cấu trúc báo cáo |
| `unit` | Đơn vị tiền tệ hoặc đơn vị tính |
| `source_page` | Trang chứa chỉ tiêu |
| `mapping_method` | Phương pháp ánh xạ chỉ tiêu |
| `mapping_status` | Trạng thái ánh xạ |
| `confidence` | Độ tin cậy của việc ánh xạ |
| `ocr_confidence` | Độ tin cậy nhận diện ký tự/giá trị |
| `warnings` | Danh sách cảnh báo liên quan |

## 5. Metadata cấp báo cáo

OCR cần cung cấp thêm các thông tin sau nếu nhận diện được:

- `accounting_standard`: chế độ kế toán, ví dụ TT99, TT200 hoặc TT133.
- `company`: thông tin doanh nghiệp.
- `document`: loại báo cáo, kỳ báo cáo, ngày bắt đầu và ngày kết thúc kỳ.
- `validation`: kết quả kiểm tra dữ liệu ban đầu.
- `evidence_images`: ảnh minh chứng trích xuất.
- `warnings`: cảnh báo cấp báo cáo.

## 6. Quy tắc xử lý

1. Mỗi chỉ tiêu phải được gắn đúng `accounting_standard` và `statement_type`; không ánh xạ chỉ dựa trên mã số nếu mã có thể trùng giữa các báo cáo hoặc chế độ kế toán.
2. Giữ lại `raw_label` để đối chiếu với nội dung gốc.
3. Không tự điền `0` khi thiếu hoặc không nhận diện được giá trị. Dùng `null` và trạng thái phù hợp.
4. Không tự suy diễn giá trị đầu kỳ/cuối kỳ nếu tài liệu không cung cấp.
5. Không gộp phạm vi `RECEIVABLES` hoặc `PAYABLES` khi chưa có quy tắc mapping được thống nhất.
6. Nếu không chắc chắn về ánh xạ, đánh dấu cần rà soát thay vì tự chọn một chỉ tiêu.
7. OCR nên giữ lại các dòng đã nhận diện khác ngoài danh sách này để hỗ trợ mở rộng chỉ số trong tương lai.

## 7. Phạm vi sử dụng

Các chỉ tiêu trên phục vụ:
- Tính toán 14 tỷ số tài chính.
- Phân tích tăng trưởng giữa các kỳ có dữ liệu tương thích.
- Phân tích dòng tiền khi báo cáo lưu chuyển tiền tệ được cung cấp.

**Lưu ý:** Danh sách có 19 Indicator Key duy nhất. Các giá trị đầu kỳ/cuối kỳ là dữ liệu theo thời điểm của một số chỉ tiêu, không phải Indicator Key riêng.
