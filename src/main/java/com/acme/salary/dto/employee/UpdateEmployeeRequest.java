package com.acme.salary.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
        String lastName,

        @Email(message = "Email should be valid")
        String email,

        @Size(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
        String country,

        @Size(min = 1, max = 100, message = "Department must be between 1 and 100 characters")
        String department,

        @Size(min = 1, max = 100, message = "Job title must be between 1 and 100 characters")
        String jobTitle
) {
}
