package com.acme.salary.service;

import com.acme.salary.dto.salary.CreateSalaryRequest;
import com.acme.salary.dto.salary.SalaryResponse;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.enums.Currency;
import com.acme.salary.exception.EmployeeNotFoundException;
import com.acme.salary.exception.InvalidSalaryException;
import com.acme.salary.exception.SalaryNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Salary operations.
 * Handles business logic, validation, and salary history management.
 */
@Service
@Transactional
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    public SalaryService(SalaryRepository salaryRepository, EmployeeRepository employeeRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get complete salary history for an employee.
     * Results are sorted by effectiveFrom descending (most recent first).
     *
     * @param employeeId Employee ID
     * @return List of SalaryResponse objects
     * @throws EmployeeNotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public List<SalaryResponse> getSalaryHistory(Long employeeId) {
        verifyEmployeeExists(employeeId);
        
        return salaryRepository.findSalaryHistoryByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get the currently active salary for an employee.
     * The current salary is the one where effectiveTo IS NULL.
     *
     * @param employeeId Employee ID
     * @return SalaryResponse
     * @throws EmployeeNotFoundException if employee not found
     * @throws SalaryNotFoundException if no active salary exists
     */
    @Transactional(readOnly = true)
    public SalaryResponse getCurrentSalary(Long employeeId) {
        verifyEmployeeExists(employeeId);
        
        Salary salary = salaryRepository.findCurrentSalaryByEmployeeId(employeeId)
                .orElseThrow(() -> new SalaryNotFoundException("No active salary found for employee ID: " + employeeId));
        
        return mapToResponse(salary);
    }

    /**
     * Create a new salary record for an employee.
     * 
     * Business logic:
     * - Employee must exist
     * - Salary amount must be positive
     * - Currency must be valid
     * - No overlapping salary periods allowed
     * - If a currently active salary exists with effectiveFrom before the new salary's effectiveFrom,
     *   it will be closed automatically (effectiveTo set to new salary's effectiveFrom - 1 day)
     *
     * @param employeeId Employee ID
     * @param request CreateSalaryRequest
     * @return SalaryResponse
     * @throws EmployeeNotFoundException if employee not found
     * @throws InvalidSalaryException if salary data is invalid or overlaps with existing
     */
    public SalaryResponse createSalary(Long employeeId, CreateSalaryRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + employeeId));

        // Validate salary amount
        validateSalaryAmount(request.amount());

        // Validate and parse currency
        Currency currency = validateAndParseCurrency(request.currency());

        // Get existing salary history
        List<Salary> existingHistory = salaryRepository.findSalaryHistoryByEmployeeId(employeeId);

        // Check for overlapping periods
        detectAndThrowOverlapViolation(request.effectiveFrom(), request.effectiveTo(), existingHistory);

        // Handle closing of previously active salary if needed
        closePreviousActiveSalaryIfNeeded(employeeId, request.effectiveFrom(), existingHistory);

        // Create and save new salary
        Salary newSalary = new Salary(
                employee,
                request.amount(),
                currency,
                request.effectiveFrom(),
                request.effectiveTo()
        );

        Salary savedSalary = salaryRepository.save(newSalary);
        return mapToResponse(savedSalary);
    }

    /**
     * Verify that an employee exists.
     *
     * @param employeeId Employee ID
     * @throws EmployeeNotFoundException if employee not found
     */
    private void verifyEmployeeExists(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
        }
    }

    /**
     * Validate that salary amount is positive.
     *
     * @param amount Salary amount
     * @throws InvalidSalaryException if amount is not positive
     */
    private void validateSalaryAmount(java.math.BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidSalaryException("Salary amount must be positive");
        }
    }

    /**
     * Validate and parse currency string to enum.
     *
     * @param currencyStr Currency string
     * @return Currency enum value
     * @throws InvalidSalaryException if currency is invalid
     */
    private Currency validateAndParseCurrency(String currencyStr) {
        try {
            return Currency.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSalaryException("Invalid currency: " + currencyStr);
        }
    }

    /**
     * Detect if a new salary period overlaps with existing periods.
     * Throws InvalidSalaryException if overlap is detected.
     * 
     * Note: Active salaries (where effectiveTo IS NULL) are skipped here
     * because they are handled by the closure logic - if a new salary starts
     * after an active salary, the active salary will be closed automatically.
     *
     * @param newEffectiveFrom Start date of new salary
     * @param newEffectiveTo End date of new salary (can be null for currently active)
     * @param existingHistory Existing salary records for the employee
     * @throws InvalidSalaryException if overlap detected
     */
    private void detectAndThrowOverlapViolation(LocalDate newEffectiveFrom, LocalDate newEffectiveTo, List<Salary> existingHistory) {
        for (Salary existing : existingHistory) {
            // Skip active salary (effectiveTo IS NULL) - it will be handled by closure logic
            if (existing.getEffectiveTo() == null) {
                continue;
            }
            
            if (periodsOverlap(existing.getEffectiveFrom(), existing.getEffectiveTo(), newEffectiveFrom, newEffectiveTo)) {
                throw new InvalidSalaryException(
                        "Salary period overlaps with existing salary: " +
                        existing.getEffectiveFrom() + " to " + existing.getEffectiveTo()
                );
            }
        }
    }

    /**
     * Check if two date periods overlap.
     * Null end date means the period is currently active (extends to infinity).
     *
     * @param start1 Start of period 1
     * @param end1 End of period 1 (null means ongoing)
     * @param start2 Start of period 2
     * @param end2 End of period 2 (null means ongoing)
     * @return true if periods overlap
     */
    private boolean periodsOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // Period 1: [start1, end1 or infinity]
        // Period 2: [start2, end2 or infinity]
        
        // Case 1: start2 is within period 1
        if (start2.compareTo(start1) >= 0 && (end1 == null || start2.compareTo(end1) <= 0)) {
            return true;
        }
        
        // Case 2: end2 is within period 1 (only if end2 is not null)
        if (end2 != null && end2.compareTo(start1) >= 0 && (end1 == null || end2.compareTo(end1) <= 0)) {
            return true;
        }
        
        // Case 3: period 2 wraps period 1 (start2 < start1 and (end2 is null or end2 > end1))
        if (start2.compareTo(start1) < 0 && (end2 == null || (end1 != null && end2.compareTo(end1) > 0))) {
            return true;
        }
        
        return false;
    }

    /**
     * Close the previously active salary if a new salary is being created after it.
     * 
     * If an existing salary has effectiveTo IS NULL (currently active) and the new salary's
     * effectiveFrom is after the existing salary's effectiveFrom, then close the existing
     * salary by setting its effectiveTo to (new effectiveFrom - 1 day).
     *
     * @param employeeId Employee ID
     * @param newEffectiveFrom Start date of new salary
     * @param existingHistory Existing salary records for the employee
     * @throws InvalidSalaryException if the existing active salary cannot be closed
     */
    private void closePreviousActiveSalaryIfNeeded(Long employeeId, LocalDate newEffectiveFrom, List<Salary> existingHistory) {
        Salary activeSalary = existingHistory.stream()
                .filter(s -> s.getEffectiveTo() == null)
                .findFirst()
                .orElse(null);

        if (activeSalary != null) {
            // Verify that new salary starts after the active salary
            if (newEffectiveFrom.compareTo(activeSalary.getEffectiveFrom()) <= 0) {
                throw new InvalidSalaryException(
                        "New salary must start after the currently active salary period"
                );
            }

            // Close the active salary by setting effectiveTo to (newEffectiveFrom - 1 day)
            activeSalary.setEffectiveTo(newEffectiveFrom.minusDays(1));
            salaryRepository.save(activeSalary);
        }
    }

    /**
     * Map Salary entity to SalaryResponse DTO.
     *
     * @param salary Salary entity
     * @return SalaryResponse
     */
    private SalaryResponse mapToResponse(Salary salary) {
        return new SalaryResponse(
                salary.getId(),
                salary.getEmployee().getId(),
                salary.getAmount(),
                salary.getCurrency().name(),
                salary.getEffectiveFrom(),
                salary.getEffectiveTo(),
                salary.getCreatedAt(),
                salary.getUpdatedAt()
        );
    }
}
