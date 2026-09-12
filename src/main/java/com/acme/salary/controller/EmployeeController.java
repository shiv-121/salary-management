package com.acme.salary.controller;

import com.acme.salary.dto.PageResponse;
import com.acme.salary.dto.employee.CreateEmployeeRequest;
import com.acme.salary.dto.employee.EmployeeResponse;
import com.acme.salary.dto.employee.UpdateEmployeeRequest;
import com.acme.salary.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Employee endpoints.
 * Handles HTTP requests and delegates business logic to EmployeeService.
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/employees
     * Retrieve employees with optional pagination, sorting, searching, and filtering.
     *
     * Query parameters:
     * - page: Page number (0-indexed), default 0
     * - size: Page size, default 20, max 100
     * - sort: Sort field and direction (e.g., "lastName,asc"), default "lastName,asc"
     * - search: Keyword search across employeeCode, firstName, lastName, email
     * - country: Filter by country using case-insensitive partial match
     * - department: Filter by department using case-insensitive partial match
     * - jobTitle: Filter by job title using case-insensitive partial match
     *
     * @param page Page number
     * @param size Page size
     * @param sort Sort specification
     * @param search Keyword search
     * @param country Country filter
     * @param department Department filter
     * @param jobTitle Job title filter
     * @return PageResponse with employees and pagination metadata with HTTP 200
     */
    @GetMapping
    @Operation(
            summary = "List employees",
            description = "Retrieve employees with optional pagination, sorting, searching, and filtering. " +
                    "Database-level operations support efficiently querying millions of records."
    )
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
            @Parameter(name = "size", description = "Page size (max 100)", example = "20"),
            @Parameter(name = "sort", description = "Sort field and direction (e.g., 'lastName,asc')", example = "lastName,asc"),
            @Parameter(name = "search", description = "Search keyword (matches employeeCode, firstName, lastName, email)", example = "john"),
            @Parameter(name = "country", description = "Filter by country using case-insensitive partial match", example = "Ind"),
            @Parameter(name = "department", description = "Filter by department using case-insensitive partial match", example = "Eng"),
            @Parameter(name = "jobTitle", description = "Filter by job title using case-insensitive partial match", example = "Software")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters (e.g., page size exceeds 100, invalid sort field)")
    })
    public ResponseEntity<PageResponse<EmployeeResponse>> getEmployees(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "jobTitle", required = false) String jobTitle) {

        PageResponse<EmployeeResponse> employees = employeeService.getEmployees(
                page, size, sort, search, country, department, jobTitle);
        return ResponseEntity.ok(employees);
    }

    /**
     * GET /api/employees/{id}
     * Retrieve an employee by ID.
     *
     * @param id Employee ID
     * @return EmployeeResponse with HTTP 200, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by their unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Employee ID", example = "1")
            @PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * POST /api/employees
     * Create a new employee.
     *
     * @param request CreateEmployeeRequest
     * @return EmployeeResponse with HTTP 201 Created
     */
    @PostMapping
    @Operation(summary = "Create employee", description = "Create a new employee record. " +
            "employeeCode and email must be unique across the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or duplicate employeeCode/email")
    })
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse createdEmployee = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    /**
     * PUT /api/employees/{id}
     * Update an existing employee.
     *
     * @param id Employee ID
     * @param request UpdateEmployeeRequest
     * @return EmployeeResponse with HTTP 200, or 404 if not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee record. " +
            "All fields are optional for partial updates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or duplicate email"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "Employee ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
}
