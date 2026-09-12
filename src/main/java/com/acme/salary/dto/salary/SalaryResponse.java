package com.acme.salary.dto.salary;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Salary record details")
public record SalaryResponse(
        @Schema(description = "Unique salary record ID", example = "1")
        Long id,

        @Schema(description = "Associated employee ID", example = "1")
        Long employeeId,

        @Schema(description = "Salary amount (positive decimal)", example = "82000.00")
        BigDecimal amount,

        @Schema(description = "Currency code (ISO 4217)", example = "USD", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
        String currency,

        @Schema(description = "Date when salary becomes effective", example = "2026-01-01")
        LocalDate effectiveFrom,

        @Schema(description = "Date when salary ends (null means currently active)", example = "2026-12-31", nullable = true)
        LocalDate effectiveTo,

        @Schema(description = "Record creation timestamp", example = "2026-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Record last modification timestamp", example = "2026-01-15T10:30:00")
        LocalDateTime updatedAt
) {
}
