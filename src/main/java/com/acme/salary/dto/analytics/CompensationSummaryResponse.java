package com.acme.salary.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Compensation summary across all employees in the reporting currency")
public record CompensationSummaryResponse(
        @Schema(description = "Reporting currency used for all salary comparisons", example = "USD")
        String reportingCurrency,

        @Schema(description = "Total employee count, including those without a current salary", example = "10000")
        long totalEmployees,

        @Schema(description = "Average salary for employees with a current salary in the reporting currency", example = "72500.50", nullable = true)
        BigDecimal averageSalary,

        @Schema(description = "Median salary for employees with a current salary in the reporting currency", example = "70000.00", nullable = true)
        BigDecimal medianSalary,

        @Schema(description = "Highest current salary in the reporting currency", example = "250000.00", nullable = true)
        BigDecimal highestSalary,

        @Schema(description = "Lowest current salary in the reporting currency", example = "35000.00", nullable = true)
        BigDecimal lowestSalary
) {
}
