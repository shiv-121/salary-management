package com.acme.salary.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank(message = "Employee code is required")
        String employeeCode,

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Country is required")
        @Size(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
        String country,

        @NotBlank(message = "Department is required")
        @Size(min = 1, max = 100, message = "Department must be between 1 and 100 characters")
        String department,

        @NotBlank(message = "Job title is required")
        @Size(min = 1, max = 100, message = "Job title must be between 1 and 100 characters")
        String jobTitle
) {
}
