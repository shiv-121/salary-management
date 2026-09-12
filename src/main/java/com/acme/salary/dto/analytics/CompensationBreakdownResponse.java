package com.acme.salary.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Salary aggregation for an employee group such as a country, department, or job title")
public record CompensationBreakdownResponse(
        @Schema(description = "Grouped dimension name, such as country or department", example = "India")
        String group,

        @Schema(description = "Number of employees in this group with a current salary", example = "1200")
        long employeeCount,

        @Schema(description = "Average salary for the group in the reporting currency", example = "54000.00", nullable = true)
        BigDecimal averageSalary,

        @Schema(description = "Median salary for the group in the reporting currency", example = "51000.00", nullable = true)
        BigDecimal medianSalary
) {
}
