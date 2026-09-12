package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmployeeSpecificationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employeeRepository.saveAll(List.of(
                new Employee("EMP001", "Alice", "Brown", "alice@india.com", "India", "Engineering", "Senior Software Engineer"),
                new Employee("EMP002", "Bob", "Green", "bob@usa.com", "United States", "Finance", "Financial Analyst"),
                new Employee("EMP003", "Charlie", "Black", "charlie@india.com", "India", "Sales", "Account Manager"),
                new Employee("EMP004", "Dana", "White", "dana@canada.com", "Canada", "Engineering", "Data Scientist"),
                new Employee("EMP005", "John", "Smith", "john@france.com", "France", "Finance", "Senior Software Engineer")
        ));
    }

    @Test
    void countryPartialMatch_shouldFindMatchingSubstringIgnoringCase() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByCountry("Ind"),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP003");

        Page<Employee> upperCaseResults = employeeRepository.findAll(
                EmployeeSpecification.filterByCountry("IND"),
                PageRequest.of(0, 10)
        );

        assertThat(upperCaseResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP003");
    }

    @Test
    void departmentPartialMatch_shouldFindMatchingSubstringIgnoringCase() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByDepartment("Eng"),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP004");

        Page<Employee> lowerCaseResults = employeeRepository.findAll(
                EmployeeSpecification.filterByDepartment("fin"),
                PageRequest.of(0, 10)
        );

        assertThat(lowerCaseResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP002", "EMP005");
    }

    @Test
    void jobTitlePartialMatch_shouldFindMatchingSubstringIgnoringCase() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByJobTitle("software"),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP005");

        Page<Employee> analystResults = employeeRepository.findAll(
                EmployeeSpecification.filterByJobTitle("Analyst"),
                PageRequest.of(0, 10)
        );

        assertThat(analystResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP002");
    }

    @Test
    void combinedFilters_shouldUseAndSemantics() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.combineFilters(null, "Ind", "Eng", null),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP001");
        assertThat(results.getTotalElements()).isEqualTo(1);
    }

    @Test
    void globalSearch_shouldRemainCaseInsensitivePartialMatchAcrossNamesAndEmail() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("ali"),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP001");

        Page<Employee> emailResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("@USA.COM"),
                PageRequest.of(0, 10)
        );

        assertThat(emailResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP002");
    }

    @Test
    void blankFilters_shouldReturnAllApplicableRecords() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.combineFilters(null, "   ", "\t", null),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(5);
        assertThat(results.getTotalElements()).isEqualTo(5);
    }

    @Test
    void paginationAndSorting_shouldRemainValidAfterFiltering() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByCountry("ind"),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "lastName"))
        );

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getTotalPages()).isEqualTo(1);
        assertThat(results.getContent())
                .extracting(Employee::getLastName)
                .containsExactly("Black", "Brown");
    }
}
