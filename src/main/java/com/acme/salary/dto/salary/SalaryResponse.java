package com.acme.salary.dto.salary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SalaryResponse(
        Long id,
        Long employeeId,
        BigDecimal amount,
        String currency,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
