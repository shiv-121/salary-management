package com.acme.salary.controller;

import com.acme.salary.dto.PageResponse;
import com.acme.salary.dto.employee.CreateEmployeeRequest;
import com.acme.salary.dto.employee.EmployeeResponse;
import com.acme.salary.dto.employee.UpdateEmployeeRequest;
import com.acme.salary.service.EmployeeService;
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
     * - country: Filter by country (exact match)
     * - department: Filter by department (exact match)
     * - jobTitle: Filter by job title (exact match)
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
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
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
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
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
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }
}
