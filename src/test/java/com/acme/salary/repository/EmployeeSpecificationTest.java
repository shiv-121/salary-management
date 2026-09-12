package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import com.acme.salary.service.EmployeeService;
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

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employeeRepository.saveAll(List.of(
                new Employee("EMP001", "Alice", "Brown", "alice@india.com", "India", "Engineering", "Senior Software Engineer"),
                new Employee("EMP002", "Bob", "Green", "bob@usa.com", "United States", "Finance", "Financial Analyst"),
                new Employee("EMP003", "Charlie", "Black", "charlie@india.com", "India", "Sales", "Account Manager"),
                new Employee("EMP004", "Dana", "White", "dana@canada.com", "Canada", "Engineering", "Data Scientist"),
                new Employee("EMP005", "John", "Smith", "john@france.com", "France", "Finance", "Senior Software Engineer"),
                new Employee("EMP006", "Shivam", "Sharma", "shivam.sharma@acme.example", "India", "Engineering", "Senior Software Engineer")
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
                .containsExactlyInAnyOrder("EMP001", "EMP003", "EMP006");

        Page<Employee> upperCaseResults = employeeRepository.findAll(
                EmployeeSpecification.filterByCountry("IND"),
                PageRequest.of(0, 10)
        );

        assertThat(upperCaseResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP003", "EMP006");
    }

    @Test
    void departmentPartialMatch_shouldFindMatchingSubstringIgnoringCase() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByDepartment("Eng"),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactlyInAnyOrder("EMP001", "EMP004", "EMP006");

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
                .containsExactlyInAnyOrder("EMP001", "EMP005", "EMP006");

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
                .containsExactly("EMP001", "EMP006");
        assertThat(results.getTotalElements()).isEqualTo(2);
    }

    @Test
    void globalSearch_shouldRemainCaseInsensitivePartialMatchAcrossNamesAndEmail() {
        Page<Employee> firstNameResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("ali"),
                PageRequest.of(0, 10)
        );

        assertThat(firstNameResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP001");

        Page<Employee> lastNameResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("Sharma"),
                PageRequest.of(0, 10)
        );

        assertThat(lastNameResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");

        Page<Employee> fullNameResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("Shivam Sharma"),
                PageRequest.of(0, 10)
        );

        assertThat(fullNameResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");

        Page<Employee> caseInsensitiveFullNameResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("shivam sharma"),
                PageRequest.of(0, 10)
        );

        assertThat(caseInsensitiveFullNameResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");

        Page<Employee> partialFullNameResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("Shivam Shar"),
                PageRequest.of(0, 10)
        );

        assertThat(partialFullNameResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");

        Page<Employee> whitespaceResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("  Shivam   Sharma  "),
                PageRequest.of(0, 10)
        );

        assertThat(whitespaceResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");

        Page<Employee> emailResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("@USA.COM"),
                PageRequest.of(0, 10)
        );

        assertThat(emailResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP002");

        Page<Employee> employeeCodeResults = employeeRepository.findAll(
                EmployeeSpecification.searchByKeyword("EMP006"),
                PageRequest.of(0, 10)
        );

        assertThat(employeeCodeResults.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");
    }

    @Test
    void blankFilters_shouldReturnAllApplicableRecords() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.combineFilters(null, "   ", "\t", null),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent()).hasSize(6);
        assertThat(results.getTotalElements()).isEqualTo(6);
    }

    @Test
    void combinedSearchAndFilters_shouldUseAndSemantics() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.combineFilters("shivam sharma", "india", "engineering", null),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP006");
    }

    @Test
    void paginationAndSorting_shouldRemainValidAfterFiltering() {
        Page<Employee> results = employeeRepository.findAll(
                EmployeeSpecification.filterByCountry("ind"),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "lastName"))
        );

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getTotalElements()).isEqualTo(3);
        assertThat(results.getTotalPages()).isEqualTo(2);
        assertThat(results.getContent())
                .extracting(Employee::getLastName)
                .containsExactly("Black", "Brown");
    }

    @Test
    void nameSort_shouldSortByFirstNameThenLastNameServerSide() {
        employeeRepository.deleteAll();
        employeeRepository.saveAll(List.of(
                new Employee("EMP101", "Alex", "Smith", "alex.smith@demo.com", "India", "Engineering", "Lead Engineer"),
                new Employee("EMP102", "Alex", "Brown", "alex.brown@demo.com", "India", "Engineering", "Senior Engineer"),
                new Employee("EMP103", "Dana", "White", "dana.white@demo.com", "Canada", "Finance", "Analyst"),
                new Employee("EMP104", "Charlie", "Black", "charlie.black@demo.com", "United States", "Sales", "Manager")
        ));

        var asc = employeeService.getEmployees(0, 10, "name,asc", null, null, null, null);
        assertThat(asc.content())
                .extracting(employee -> employee.firstName() + " " + employee.lastName())
                .containsExactly("Alex Brown", "Alex Smith", "Charlie Black", "Dana White");

        var desc = employeeService.getEmployees(0, 10, "name,desc", null, null, null, null);
        assertThat(desc.content())
                .extracting(employee -> employee.firstName() + " " + employee.lastName())
                .containsExactly("Dana White", "Charlie Black", "Alex Smith", "Alex Brown");
    }
}
