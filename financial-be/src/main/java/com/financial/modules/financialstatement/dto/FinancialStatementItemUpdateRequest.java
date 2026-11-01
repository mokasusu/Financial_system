package com.financial.modules.financialstatement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for editing financial indicator draft values")
public class FinancialStatementItemUpdateRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    @Schema(description = "List of indicator item updates")
    private List<ItemUpdate> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single indicator item update entry")
    public static class ItemUpdate {

        @NotNull(message = "Item ID is required")
        @Schema(description = "Indicator item ID to update", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "Updated current period financial value", example = "30000000000000")
        private BigDecimal currentValue;

        @Schema(description = "Updated previous period financial value", example = "28500000000000")
        private BigDecimal previousValue;
    }
}

