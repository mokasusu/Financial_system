package com.financial.modules.company.dto;

import com.financial.modules.company.enums.CompanyStatus;
import com.financial.modules.company.enums.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Company profile response")
public class CompanyResponse {

    @Schema(description = "Company ID", example = "1")
    private Long id;

    @Schema(description = "Owner user ID", example = "1")
    private Long ownerId;

    @Schema(description = "Owner full name", example = "Alice Johnson")
    private String ownerName;

    @Schema(description = "Vietnamese enterprise tax code (MST)", example = "0101234567")
    private String taxCode;

    @Schema(description = "Registered company name in Vietnamese", example = "Công ty Cổ phần Công nghệ Y tế Sao Mai")
    private String companyName;

    @Schema(description = "Company name in English", example = "Sao Mai Health Technology Joint Stock Company")
    private String companyNameEn;

    @Schema(description = "Abbreviated company name / trading name", example = "SAOMAI TECH")
    private String shortName;

    @Schema(description = "Vietnamese enterprise registration type", example = "JOINT_STOCK_COMPANY")
    private CompanyType companyType;

    @Schema(description = "Vietnamese display label for enterprise type", example = "Công ty cổ phần")
    private String companyTypeDisplayName;

    @Schema(description = "Main business line / industry", example = "Lập trình máy vi tính và tư vấn giải pháp y tế số")
    private String mainBusinessLine;

    @Schema(description = "Date of establishment", example = "2020-05-15")
    private LocalDate establishedDate;

    @Schema(description = "Full name of legal representative", example = "Nguyễn Văn An")
    private String legalRepresentative;

    @Schema(description = "Detailed street address", example = "Số 123 Đường Cầu Giấy")
    private String address;

    @Schema(description = "Ward / commune", example = "Phường Dịch Vọng Hậu")
    private String ward;

    @Schema(description = "Province / municipality", example = "Thành phố Hà Nội")
    private String province;

    @Schema(description = "Contact phone number", example = "+842438889999")
    private String phone;

    @Schema(description = "Corporate contact email", example = "contact@saomaitech.vn")
    private String email;

    @Schema(description = "Company website URL", example = "https://saomaitech.vn")
    private String website;

    @Schema(description = "Operating status", example = "ACTIVE")
    private CompanyStatus status;

    @Schema(description = "Vietnamese display label for operating status", example = "Đang hoạt động")
    private String statusDisplayName;

    @Schema(description = "Record creation timestamp", example = "2026-10-31T23:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-10-31T23:30:00")
    private LocalDateTime updatedAt;
}

