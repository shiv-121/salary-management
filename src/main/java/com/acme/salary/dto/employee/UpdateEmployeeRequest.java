package com.acme.salary.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an employee (all fields optional for partial updates)")
public record UpdateEmployeeRequest(
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        @Schema(description = "Employee's first name (optional)", example = "John")
        String firstName,

        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        @Schema(description = "Employee's last name (optional)", example = "Doe")
        String lastName,

        @Email(message = "Email should be valid")
        @Schema(description = "Employee's email address (optional, must be unique if provided)", example = "john.doe@acme.com")
        String email,

        @Size(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
        @Schema(description = "Employee's country (optional)", example = "India")
        String country,

        @Size(min = 1, max = 100, message = "Department must be between 1 and 100 characters")
        @Schema(description = "Employee's department (optional)", example = "Engineering")
        String department,

        @Size(min = 1, max = 100, message = "Job title must be between 1 and 100 characters")
        @Schema(description = "Employee's job title (optional)", example = "Senior Software Engineer")
        String jobTitle
) {
}
