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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    public SalaryService(
            SalaryRepository salaryRepository,
            EmployeeRepository employeeRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Returns salary history ordered from newest to oldest.
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
     * Returns the employee's currently active salary.
     */
    @Transactional(readOnly = true)
    public SalaryResponse getCurrentSalary(Long employeeId) {
        verifyEmployeeExists(employeeId);

        Salary salary = salaryRepository.findCurrentSalaryByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new SalaryNotFoundException(
                                "No active salary found for employee ID: " + employeeId
                        )
                );

        return mapToResponse(salary);
    }

    /**
     * Creates a new salary record while preserving salary history.
     * The previous active salary is automatically closed when required.
     */
    public SalaryResponse createSalary(
            Long employeeId,
            CreateSalaryRequest request) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with ID: " + employeeId
                        )
                );

        validateSalaryAmount(request.amount());
        validateEffectiveDates(request.effectiveFrom(), request.effectiveTo());

        Currency currency = validateAndParseCurrency(request.currency());

        List<Salary> existingHistory =
                salaryRepository.findSalaryHistoryByEmployeeId(employeeId);

        // A new record must represent an actual compensation change.
        validateSalaryIsDifferentFromCurrent(
                request.amount(),
                currency,
                existingHistory
        );

        // Existing closed periods must not overlap with the new period.
        detectAndThrowOverlapViolation(
                request.effectiveFrom(),
                request.effectiveTo(),
                existingHistory
        );

        // Close the currently active salary if the new salary starts later.
        closePreviousActiveSalaryIfNeeded(
                request.effectiveFrom(),
                existingHistory
        );

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

    private void verifyEmployeeExists(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(
                    "Employee not found with ID: " + employeeId
            );
        }
    }

    private void validateSalaryAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidSalaryException(
                    "Salary amount must be positive"
            );
        }
    }

    private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom == null) {
            throw new InvalidSalaryException("effectiveFrom is required");
        }

        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new InvalidSalaryException(
                    "effectiveTo must be greater than or equal to effectiveFrom"
            );
        }
    }

    private Currency validateAndParseCurrency(String currencyStr) {
        try {
            return Currency.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSalaryException(
                    "Invalid currency: " + currencyStr
            );
        }
    }

    /**
     * Rejects a new salary when amount and currency are identical
     * to the employee's current salary.
     *
     * Historical salaries are ignored so a previous salary can
     * legitimately be reused later.
     */
    private void validateSalaryIsDifferentFromCurrent(
            BigDecimal newAmount,
            Currency newCurrency,
            List<Salary> existingHistory) {

        existingHistory.stream()
                .filter(salary -> salary.getEffectiveTo() == null)
                .findFirst()
                .ifPresent(currentSalary -> {

                    boolean sameAmount =
                            currentSalary.getAmount().compareTo(newAmount) == 0;

                    boolean sameCurrency =
                            currentSalary.getCurrency() == newCurrency;

                    if (sameAmount && sameCurrency) {
                        throw new InvalidSalaryException(
                                "New salary must be different from the current salary."
                        );
                    }
                });
    }

    private void detectAndThrowOverlapViolation(
            LocalDate newEffectiveFrom,
            LocalDate newEffectiveTo,
            List<Salary> existingHistory) {

        for (Salary existing : existingHistory) {

            // Active salary is handled separately by the closure logic.
            if (existing.getEffectiveTo() == null) {
                continue;
            }

            if (periodsOverlap(
                    existing.getEffectiveFrom(),
                    existing.getEffectiveTo(),
                    newEffectiveFrom,
                    newEffectiveTo)) {

                throw new InvalidSalaryException(
                        "Salary period overlaps with existing salary: "
                                + existing.getEffectiveFrom()
                                + " to "
                                + existing.getEffectiveTo()
                );
            }
        }
    }

    private boolean periodsOverlap(
            LocalDate start1,
            LocalDate end1,
            LocalDate start2,
            LocalDate end2) {

        if (start2.compareTo(start1) >= 0
                && (end1 == null || start2.compareTo(end1) <= 0)) {
            return true;
        }

        if (end2 != null
                && end2.compareTo(start1) >= 0
                && (end1 == null || end2.compareTo(end1) <= 0)) {
            return true;
        }

        return start2.compareTo(start1) < 0
                && (end2 == null
                || (end1 != null && end2.compareTo(end1) > 0));
    }

    /**
     * Closes the current salary one day before the new salary starts.
     */
    private void closePreviousActiveSalaryIfNeeded(
            LocalDate newEffectiveFrom,
            List<Salary> existingHistory) {

        Salary activeSalary = existingHistory.stream()
                .filter(s -> s.getEffectiveTo() == null)
                .findFirst()
                .orElse(null);

        if (activeSalary != null) {

            if (newEffectiveFrom.compareTo(
                    activeSalary.getEffectiveFrom()) <= 0) {

                throw new InvalidSalaryException(
                        "New salary must start after the currently active salary period"
                );
            }

            activeSalary.setEffectiveTo(
                    newEffectiveFrom.minusDays(1)
            );

            salaryRepository.save(activeSalary);
        }
    }

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