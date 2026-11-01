package com.financial.modules.company.controller;

import com.financial.common.config.OpenApiConfig;
import com.financial.common.response.ApiResponse;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.company.dto.CompanyRequest;
import com.financial.modules.company.dto.CompanyResponse;
import com.financial.modules.company.dto.CompanyStatusUpdateRequest;
import com.financial.modules.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "3. Company Management", description = "Endpoints for managing the authenticated user's company")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class CompanyController {

        private final CompanyService companyService;

        public CompanyController(CompanyService companyService) {
                this.companyService = companyService;
        }

        @PostMapping
        @Operation(summary = "Create company", description = "Creates a company for the currently authenticated user. Each user can own exactly one company.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Company created successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed for request data"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already owns a company or tax code already registered")
        })
        public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        @Valid @RequestBody CompanyRequest request) {
                CompanyResponse response = companyService.createCompany(userPrincipal.getId(), request);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Company created successfully", response));
        }

        @GetMapping({ "/me", "/my-company" })
        @Operation(summary = "Get my company", description = "Retrieves the company profile belonging to the currently authenticated user.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found for authenticated user")
        })
        public ResponseEntity<ApiResponse<CompanyResponse>> getMyCompany(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                CompanyResponse response = companyService.getMyCompany(userPrincipal.getId());
                return ResponseEntity.ok(ApiResponse.success("Company retrieved successfully", response));
        }

        @PutMapping("/me")
        @Operation(summary = "Update my company", description = "Updates the company information belonging to the currently authenticated user. Ownership and ID cannot be changed.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed for request data"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found for authenticated user"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Updated tax code already registered to another company")
        })
        public ResponseEntity<ApiResponse<CompanyResponse>> updateMyCompany(
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        @Valid @RequestBody CompanyRequest request) {
                CompanyResponse response = companyService.updateMyCompany(userPrincipal.getId(), request);
                return ResponseEntity.ok(ApiResponse.success("Company updated successfully", response));
        }

        @PatchMapping("/me/status")
        @Operation(summary = "Update company status", description = "Updates operating status (ACTIVE, INACTIVE) for the authenticated user's company.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company status updated successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found for authenticated user")
        })
        public ResponseEntity<ApiResponse<CompanyResponse>> updateMyCompanyStatus(
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        @Valid @RequestBody CompanyStatusUpdateRequest request) {
                CompanyResponse response = companyService.updateMyCompanyStatus(userPrincipal.getId(),
                                request.getStatus());
                return ResponseEntity.ok(ApiResponse.success("Company status updated successfully", response));
        }

        @DeleteMapping("/me")
        @Operation(summary = "Delete my company (soft delete)", description = "Performs a soft delete on the authenticated user's company by setting deleted to true. The company will no longer appear in normal queries.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company deleted successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found for authenticated user")
        })
        public ResponseEntity<ApiResponse<Void>> deleteMyCompany(
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                companyService.deleteMyCompany(userPrincipal.getId());
                return ResponseEntity.ok(ApiResponse.success("Company deleted successfully", null));
        }

}

        
                        
                        
                        
        
        
                        
                
                
        
