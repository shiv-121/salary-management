package com.acme.salary.dto.salary;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSalaryRequest(
        @NotNull(message = "Salary amount is required")
        @Positive(message = "Salary amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Effective from date is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {
}
