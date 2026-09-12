package com.acme.salary.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Employee details response")
public record EmployeeResponse(
        @Schema(description = "Unique employee ID", example = "1")
        Long id,

        @Schema(description = "Unique employee code (business identifier)", example = "EMP000001")
        String employeeCode,

        @Schema(description = "Employee's first name", example = "John")
        String firstName,

        @Schema(description = "Employee's last name", example = "Doe")
        String lastName,

        @Schema(description = "Employee's email address (unique)", example = "john.doe@acme.com")
        String email,

        @Schema(description = "Employee's country", example = "India")
        String country,

        @Schema(description = "Employee's department", example = "Engineering")
        String department,

        @Schema(description = "Employee's job title", example = "Senior Software Engineer")
        String jobTitle,

        @Schema(description = "Record creation timestamp", example = "2026-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Record last modification timestamp", example = "2026-01-15T10:30:00")
        LocalDateTime updatedAt
) {
}
