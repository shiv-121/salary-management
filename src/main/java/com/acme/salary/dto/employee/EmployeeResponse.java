package com.acme.salary.dto.employee;

import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String country,
        String department,
        String jobTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
