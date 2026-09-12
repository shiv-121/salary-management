package com.acme.salary.dto.salary;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to create a new salary record")
public record CreateSalaryRequest(
        @NotNull(message = "Salary amount is required")
        @Positive(message = "Salary amount must be positive")
        @Schema(description = "Salary amount in specified currency (must be positive)", example = "82000.00")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Schema(description = "ISO 4217 currency code", example = "USD", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
        String currency,

        @NotNull(message = "Effective from date is required")
        @Schema(description = "Date when salary becomes effective", example = "2026-01-01")
        LocalDate effectiveFrom,

        @Schema(description = "Date when salary ends (leave null for currently active salary)", example = "2026-12-31", nullable = true)
        LocalDate effectiveTo
) {
}
