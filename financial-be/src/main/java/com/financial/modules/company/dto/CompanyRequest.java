package com.financial.modules.company.dto;

import com.financial.modules.company.enums.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Company creation and update payload")
public class CompanyRequest {

    @NotBlank(message = "Tax code is required")
    @Pattern(regexp = "^[0-9]{10}(-[0-9]{3})?$", message = "Tax code must follow Vietnamese format (10 digits or 13 digits with hyphen, e.g. 0123456789 or 0123456789-001)")
    @Schema(description = "Vietnamese enterprise tax code (MST)", example = "0101234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taxCode;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Schema(description = "Official registered company name in Vietnamese", example = "Công ty Cổ phần Công nghệ Y tế Sao Mai", requiredMode = Schema.RequiredMode.REQUIRED)
    private String companyName;

    @Size(max = 255, message = "English company name must not exceed 255 characters")
    @Schema(description = "Company name in English", example = "Sao Mai Health Technology Joint Stock Company")
    private String companyNameEn;

    @Size(max = 100, message = "Short name must not exceed 100 characters")
    @Schema(description = "Abbreviated company name / trading name", example = "SAOMAI TECH")
    private String shortName;

    @NotNull(message = "Company type is required")
    @Schema(description = "Vietnamese enterprise registration type", example = "JOINT_STOCK_COMPANY", requiredMode = Schema.RequiredMode.REQUIRED)
    private CompanyType companyType;

    @NotBlank(message = "Main business line is required")
    @Size(max = 255, message = "Main business line must not exceed 255 characters")
    @Schema(description = "Main business line / industry", example = "Lập trình máy vi tính và tư vấn giải pháp y tế số", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mainBusinessLine;

    @PastOrPresent(message = "Established date cannot be in the future")
    @Schema(description = "Date of enterprise establishment", example = "2020-05-15")
    private LocalDate establishedDate;

    @Size(max = 255, message = "Legal representative name must not exceed 255 characters")
    @Schema(description = "Full name of legal representative", example = "Nguyễn Văn An")
    private String legalRepresentative;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Detailed street address", example = "Số 123 Đường Cầu Giấy", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @NotBlank(message = "Ward is required")
    @Size(max = 100, message = "Ward must not exceed 100 characters")
    @Schema(description = "Current Vietnamese ward or commune name", example = "Phường Dịch Vọng Hậu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ward;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    @Schema(description = "Current Vietnamese province or municipality name", example = "Thành phố Hà Nội", requiredMode = Schema.RequiredMode.REQUIRED)
    private String province;

    @Pattern(regexp = "^[0-9+()\\s-]{7,20}$", message = "Invalid phone number format")
    @Schema(description = "Contact phone number", example = "+842438889999")
    private String phone;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Corporate contact email", example = "contact@saomaitech.vn")
    private String email;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Schema(description = "Company website URL", example = "https://saomaitech.vn")
    private String website;
}

