package com.acme.salary.service;

import com.acme.salary.dto.salary.CreateSalaryRequest;
import com.acme.salary.dto.salary.SalaryResponse;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.enums.Currency;
import com.acme.salary.exception.EmployeeNotFoundException;
import com.acme.salary.exception.InvalidSalaryException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock
    private SalaryRepository salaryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SalaryService salaryService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("EMP001", "Alice", "Brown", "alice@example.com", "India", "Engineering", "Senior Software Engineer");
        employee.setId(1L);
    }

    @Test
    void shouldCreateInitialSalaryForEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of());

        Salary savedSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2026, 1, 1), null);
        savedSalary.setId(101L);
        when(salaryRepository.save(any(Salary.class))).thenReturn(savedSalary);

        SalaryResponse response = salaryService.createSalary(
                1L,
                new CreateSalaryRequest(new BigDecimal("70000.00"), "USD", LocalDate.of(2026, 1, 1), null)
        );

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.employeeId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo("70000.00");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.effectiveTo()).isNull();

        ArgumentCaptor<Salary> salaryCaptor = ArgumentCaptor.forClass(Salary.class);
        verify(salaryRepository).save(salaryCaptor.capture());
        assertThat(salaryCaptor.getValue().getEffectiveTo()).isNull();
        assertThat(salaryCaptor.getValue().getAmount()).isEqualByComparingTo("70000.00");
    }

    @Test
    void shouldRejectZeroSalaryAmount() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(BigDecimal.ZERO, "USD", LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void shouldRejectNegativeSalaryAmount() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(new BigDecimal("-1.00"), "USD", LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void shouldRejectInvalidCurrency() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(new BigDecimal("70000.00"), "ZZZ", LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("Invalid currency");
    }

    @Test
    void shouldRequireEffectiveFromDate() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(new BigDecimal("70000.00"), "USD", null, null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("effectiveFrom");
    }

    @Test
    void shouldCloseCurrentSalaryWhenNewSalaryBecomesEffective() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Salary currentSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2025, 1, 1), null);
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(currentSalary));

        Salary newSalary = new Salary(employee, new BigDecimal("75000.00"), Currency.USD, LocalDate.of(2026, 1, 1), null);
        newSalary.setId(202L);
        when(salaryRepository.save(any(Salary.class))).thenReturn(newSalary);

        SalaryResponse response = salaryService.createSalary(
                1L,
                new CreateSalaryRequest(new BigDecimal("75000.00"), "USD", LocalDate.of(2026, 1, 1), null)
        );

        assertThat(response.amount()).isEqualByComparingTo("75000.00");
        assertThat(response.effectiveTo()).isNull();
        assertThat(currentSalary.getEffectiveTo()).isEqualTo(LocalDate.of(2025, 12, 31));

        verify(salaryRepository, times(2)).save(any(Salary.class));
    }

    @Test
    void shouldRejectOverlappingSalaryPeriod() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Salary historicalSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(historicalSalary));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(new BigDecimal("75000.00"), "USD", LocalDate.of(2025, 6, 1), null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("overlaps");
    }

    @Test
    void shouldRejectSameAmountAndCurrencyAsCurrentSalary() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Salary currentSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2025, 1, 1), null);
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(currentSalary));

        assertThatThrownBy(() -> salaryService.createSalary(1L,
                        new CreateSalaryRequest(new BigDecimal("70000.00"), "USD", LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(InvalidSalaryException.class)
                .hasMessageContaining("must be different");
    }

    @Test
    void shouldAllowSameAmountWithDifferentCurrency() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Salary currentSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2025, 1, 1), null);
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(currentSalary));

        Salary pendingSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.EUR, LocalDate.of(2026, 1, 1), null);
        pendingSalary.setId(202L);
        when(salaryRepository.save(any(Salary.class))).thenReturn(pendingSalary);

        SalaryResponse response = salaryService.createSalary(1L,
                new CreateSalaryRequest(new BigDecimal("70000.00"), "EUR", LocalDate.of(2026, 1, 1), null));

        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.amount()).isEqualByComparingTo("70000.00");
    }

    @Test
    void shouldAllowSameAmountAndCurrencyForHistoricalSalary() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Salary historicalSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        Salary currentSalary = new Salary(employee, new BigDecimal("75000.00"), Currency.USD, LocalDate.of(2025, 1, 1), null);
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(historicalSalary, currentSalary));

        Salary newSalary = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2026, 1, 1), null);
        newSalary.setId(301L);
        when(salaryRepository.save(any(Salary.class))).thenReturn(newSalary);

        SalaryResponse response = salaryService.createSalary(1L,
                new CreateSalaryRequest(new BigDecimal("70000.00"), "USD", LocalDate.of(2026, 1, 1), null));

        assertThat(response.amount()).isEqualByComparingTo("70000.00");
        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void shouldReturnSalaryHistoryInExpectedOrder() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        Salary newest = new Salary(employee, new BigDecimal("85000.00"), Currency.USD, LocalDate.of(2026, 1, 1), null);
        newest.setId(11L);
        Salary older = new Salary(employee, new BigDecimal("70000.00"), Currency.USD, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        older.setId(12L);
        when(salaryRepository.findSalaryHistoryByEmployeeId(1L)).thenReturn(List.of(newest, older));

        List<SalaryResponse> history = salaryService.getSalaryHistory(1L);

        assertThat(history).extracting(SalaryResponse::id).containsExactly(11L, 12L);
    }

    @Test
    void shouldThrowWhenEmployeeNotFoundForSalaryHistory() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> salaryService.getSalaryHistory(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void shouldBeTransactionalAtServiceBoundary() {
        assertThat(SalaryService.class).hasAnnotation(Transactional.class);
    }
}
