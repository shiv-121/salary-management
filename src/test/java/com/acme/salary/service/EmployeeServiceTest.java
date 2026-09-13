package com.acme.salary.service;

import com.acme.salary.dto.employee.CreateEmployeeRequest;
import com.acme.salary.dto.employee.UpdateEmployeeRequest;
import com.acme.salary.entity.Employee;
import com.acme.salary.exception.EmployeeNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldRejectDuplicateEmployeeCodeWhenCreatingEmployee() {
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP001",
                "Alice",
                "Brown",
                "alice@demo.com",
                "India",
                "Engineering",
                "Senior Software Engineer"
        );

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void shouldRejectDuplicateEmailWhenCreatingEmployee() {
        when(employeeRepository.existsByEmail("alice@demo.com")).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP001",
                "Alice",
                "Brown",
                "alice@demo.com",
                "India",
                "Engineering",
                "Senior Software Engineer"
        );

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void shouldRejectDuplicateEmailWhenUpdatingEmployee() {
        Employee employee = new Employee("EMP001", "Alice", "Brown", "alice@old.com", "India", "Engineering", "Senior Software Engineer");
        employee.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("alice@new.com")).thenReturn(true);

        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "Alice",
                "Brown",
                "alice@new.com",
                "India",
                "Engineering",
                "Senior Software Engineer"
        );

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void shouldRejectPageSizeAboveMaximum() {
        assertThatThrownBy(() -> employeeService.getEmployees(0, 101, "lastName,asc", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 100");
    }

    @Test
    void shouldMapNameSortToFirstNameThenLastName() {
        Employee alexSmith = new Employee("EMP101", "Alex", "Smith", "alex.smith@demo.com", "India", "Engineering", "Engineer");
        alexSmith.setId(10L);
        Employee alexBrown = new Employee("EMP102", "Alex", "Brown", "alex.brown@demo.com", "India", "Engineering", "Engineer");
        alexBrown.setId(11L);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alexSmith, alexBrown), PageRequest.of(0, 10), 2));

        employeeService.getEmployees(0, 10, "name,desc", null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(any(Specification.class), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort().toList())
                .extracting(Sort.Order::getProperty)
                .containsExactly("firstName", "lastName");
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("firstName").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }
}
