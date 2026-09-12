package com.acme.salary.service;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.enums.Currency;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompensationAnalyticsServiceTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private CompensationAnalyticsService compensationAnalyticsService;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        salaryRepository.deleteAll();
        employeeRepository.deleteAll();

        Employee indiaEmployee = employeeRepository.save(new Employee(
                "E1001", "Asha", "Patel", "asha@demo.com", "India", "Engineering", "Senior Software Engineer"));
        Employee usEmployee = employeeRepository.save(new Employee(
                "E1002", "John", "Adams", "john@demo.com", "United States", "Sales", "Account Manager"));
        Employee franceEmployee = employeeRepository.save(new Employee(
                "E1003", "Claire", "Moreau", "claire@demo.com", "France", "Finance", "Financial Analyst"));
        employeeRepository.save(new Employee(
                "E1004", "No", "Salary", "nosalary@demo.com", "Canada", "Support", "Customer Success"));

        salaryRepository.save(new Salary(indiaEmployee, new BigDecimal("120000"), Currency.INR,
                LocalDate.now().minusDays(30), null));
        salaryRepository.save(new Salary(usEmployee, new BigDecimal("5000"), Currency.USD,
                LocalDate.now().minusDays(10), null));
        salaryRepository.save(new Salary(franceEmployee, new BigDecimal("2000"), Currency.EUR,
                LocalDate.now().minusDays(20), null));
    }

    @Test
    void summary_shouldCountAllEmployeesAndUseOnlyCurrentSalaryData() {
        var summary = compensationAnalyticsService.getSummary();

        assertThat(summary.reportingCurrency()).isEqualTo("USD");
        assertThat(summary.totalEmployees()).isEqualTo(4L);
        assertThat(summary.averageSalary()).isEqualByComparingTo("2866.67");
        assertThat(summary.medianSalary()).isEqualByComparingTo("2160.00");
        assertThat(summary.highestSalary()).isEqualByComparingTo("5000.00");
        assertThat(summary.lowestSalary()).isEqualByComparingTo("1440.00");
    }

    @Test
    void summary_shouldDefaultToUsdAndAllowExplicitCurrencyOverride() {
        var usdSummary = compensationAnalyticsService.getSummary();
        var inrSummary = compensationAnalyticsService.getSummary(Currency.INR);

        assertThat(usdSummary.reportingCurrency()).isEqualTo("USD");
        assertThat(inrSummary.reportingCurrency()).isEqualTo("INR");
        assertThat(inrSummary.totalEmployees()).isEqualTo(usdSummary.totalEmployees());
        assertThat(inrSummary.averageSalary()).isNotNull();
        assertThat(inrSummary.averageSalary()).isNotEqualByComparingTo(usdSummary.averageSalary());
        assertThat(inrSummary.averageSalary()).isGreaterThan(usdSummary.averageSalary());
    }

    @Test
    void breakdowns_shouldAggregateCurrentSalaryValuesInRequestedCurrency() {
        var usdByCountry = compensationAnalyticsService.getByCountry();
        var inrByCountry = compensationAnalyticsService.getByCountry(Currency.INR);
        var usdByDepartment = compensationAnalyticsService.getByDepartment();
        var inrByDepartment = compensationAnalyticsService.getByDepartment(Currency.INR);
        var usdByJobTitle = compensationAnalyticsService.getByJobTitle();
        var inrByJobTitle = compensationAnalyticsService.getByJobTitle(Currency.INR);

        assertThat(usdByCountry).extracting("group").containsExactly("United States", "France", "India");
        assertThat(inrByCountry).extracting("group").containsExactly("United States", "France", "India");
        assertThat(usdByDepartment).extracting("group").contains("Engineering", "Sales", "Finance");
        assertThat(inrByDepartment).extracting("group").contains("Engineering", "Sales", "Finance");
        assertThat(usdByJobTitle).extracting("group").contains("Senior Software Engineer", "Account Manager", "Financial Analyst");
        assertThat(inrByJobTitle).extracting("group").contains("Senior Software Engineer", "Account Manager", "Financial Analyst");

        assertThat(inrByCountry.get(0).averageSalary()).isNotEqualByComparingTo(usdByCountry.get(0).averageSalary());
        assertThat(inrByDepartment.get(0).averageSalary()).isNotEqualByComparingTo(usdByDepartment.get(0).averageSalary());
        assertThat(inrByJobTitle.get(0).averageSalary()).isNotEqualByComparingTo(usdByJobTitle.get(0).averageSalary());
    }

    @Test
    void invalidCurrency_shouldReturnBadRequest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(
                "http://localhost:" + port + "/api/analytics/summary?currency=INVALID").openConnection();
        connection.setRequestMethod("GET");

        int statusCode = connection.getResponseCode();
        String response = readResponseBody(connection);

        assertThat(statusCode).isEqualTo(400);
        assertThat(response).contains("INVALID_CURRENCY");
    }

    private String readResponseBody(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    @Test
    void breakdowns_shouldAggregateCurrentSalariesByGroupInUsd() {
        var byCountry = compensationAnalyticsService.getByCountry();
        var byDepartment = compensationAnalyticsService.getByDepartment();
        var byJobTitle = compensationAnalyticsService.getByJobTitle();

        assertThat(byCountry)
                .extracting("group")
                .containsExactly("United States", "France", "India");
        assertThat(byCountry.get(0).averageSalary()).isEqualByComparingTo("5000.00");
        assertThat(byCountry.get(2).averageSalary()).isEqualByComparingTo("1440.00");

        assertThat(byDepartment)
                .extracting("group")
                .contains("Engineering", "Sales", "Finance");
        assertThat(byDepartment.stream().anyMatch(item -> item.group().equals("Engineering") && item.employeeCount() == 1)).isTrue();

        assertThat(byJobTitle)
                .extracting("group")
                .contains("Senior Software Engineer", "Account Manager", "Financial Analyst");
    }
}
