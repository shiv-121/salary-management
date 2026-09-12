package com.acme.salary.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new employee")
public record CreateEmployeeRequest(
        @NotBlank(message = "Employee code is required")
        @Schema(description = "Unique business identifier for the employee", example = "EMP000001")
        String employeeCode,

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        @Schema(description = "Employee's first name", example = "John")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        @Schema(description = "Employee's last name", example = "Doe")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Schema(description = "Employee's unique email address", example = "john.doe@acme.com")
        String email,

        @NotBlank(message = "Country is required")
        @Size(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
        @Schema(description = "Employee's country", example = "India")
        String country,

        @NotBlank(message = "Department is required")
        @Size(min = 1, max = 100, message = "Department must be between 1 and 100 characters")
        @Schema(description = "Employee's department", example = "Engineering")
        String department,

        @NotBlank(message = "Job title is required")
        @Size(min = 1, max = 100, message = "Job title must be between 1 and 100 characters")
        @Schema(description = "Employee's job title", example = "Senior Software Engineer")
        String jobTitle
) {
}
