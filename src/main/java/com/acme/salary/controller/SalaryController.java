package com.acme.salary.controller;

import com.acme.salary.dto.salary.CreateSalaryRequest;
import com.acme.salary.dto.salary.SalaryResponse;
import com.acme.salary.service.SalaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Salary endpoints.
 * Handles HTTP requests and delegates business logic to SalaryService.
 */
@RestController
@RequestMapping("/api/employees/{employeeId}/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    /**
     * GET /api/employees/{employeeId}/salaries
     * Retrieve complete salary history for an employee.
     * Results are sorted by effectiveFrom descending (most recent first).
     *
     * @param employeeId Employee ID
     * @return List of SalaryResponse objects with HTTP 200, or 404 if employee not found
     */
    @GetMapping
    public ResponseEntity<List<SalaryResponse>> getSalaryHistory(@PathVariable Long employeeId) {
        List<SalaryResponse> salaryHistory = salaryService.getSalaryHistory(employeeId);
        return ResponseEntity.ok(salaryHistory);
    }

    /**
     * GET /api/employees/{employeeId}/salaries/current
     * Retrieve the currently active salary for an employee.
     *
     * @param employeeId Employee ID
     * @return SalaryResponse with HTTP 200, or 404 if employee not found or no active salary exists
     */
    @GetMapping("/current")
    public ResponseEntity<SalaryResponse> getCurrentSalary(@PathVariable Long employeeId) {
        SalaryResponse currentSalary = salaryService.getCurrentSalary(employeeId);
        return ResponseEntity.ok(currentSalary);
    }

    /**
     * POST /api/employees/{employeeId}/salaries
     * Create a new salary record for an employee.
     *
     * If a currently active salary exists and the new salary starts after it,
     * the previous salary will be automatically closed.
     *
     * @param employeeId Employee ID
     * @param request CreateSalaryRequest
     * @return SalaryResponse with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<SalaryResponse> createSalary(
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateSalaryRequest request) {
        SalaryResponse createdSalary = salaryService.createSalary(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSalary);
    }
}
