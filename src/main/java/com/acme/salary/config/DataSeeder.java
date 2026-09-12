package com.acme.salary.config;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Salary;
import com.acme.salary.enums.Currency;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final List<String> FIRST_NAMES = Arrays.asList(
            "Ava", "Liam", "Maya", "Noah", "Sophia", "Ethan", "Olivia", "Lucas",
            "Isabella", "Mason", "Emma", "James", "Charlotte", "Benjamin", "Harper",
            "Henry", "Amelia", "Elijah", "Evelyn", "Leo", "Zoe", "Daniel", "Grace",
            "Mateo", "Chloe", "Samuel", "Nora", "David", "Luna", "Joseph", "Aria",
            "Jackson", "Scarlett", "Gabriel", "Ella", "Carter", "Layla", "Anthony",
            "Paisley", "Wyatt", "Stella", "John", "Aurora", "Julian", "Nova", "Isaac",
            "Hannah", "Aiden", "Violet", "Nathan", "Sofia", "Owen", "Hazel", "Leo"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
            "Patel", "Nguyen", "Kim", "Johnson", "Silva", "Brown", "Garcia", "Lee",
            "Davis", "Martinez", "Wilson", "Miller", "Taylor", "Moore", "Anderson",
            "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Young",
            "Allen", "King", "Wright", "Scott", "Green", "Baker", "Adams", "Nelson",
            "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell",
            "Parker", "Evans", "Edwards", "Collins", "Stewart", "Flores", "Morris",
            "Murphy", "Rivera", "Cook", "Rogers", "Reed", "Morgan", "Bell", "Cooper"
    );

    private static final List<String> COUNTRIES = Arrays.asList(
            "India", "United States", "United Kingdom", "Germany", "Canada",
            "Australia", "Singapore", "Japan", "France", "Brazil"
    );

    private static final List<String> DEPARTMENTS = Arrays.asList(
            "Engineering", "Finance", "Human Resources", "Sales", "Marketing",
            "Operations", "IT", "Legal", "Product", "Research"
    );

    private static final List<String> JOB_TITLES = Arrays.asList(
            "Software Engineer", "Senior Software Engineer", "Tech Lead",
            "Engineering Manager", "Business Analyst", "Financial Analyst",
            "HR Manager", "Product Manager", "Project Manager", "Sales Manager",
            "Customer Success Manager", "Research Scientist", "Data Analyst", "Security Engineer"
    );

    private static final List<Currency> CURRENCIES = Arrays.asList(
            Currency.USD, Currency.EUR, Currency.GBP, Currency.INR, Currency.CAD,
            Currency.AUD, Currency.SGD, Currency.JPY
    );

    private static final int DEFAULT_EMPLOYEE_COUNT = 10000;
    private static final int BATCH_SIZE = 500;

    private final EmployeeRepository employeeRepository;
    private final SalaryRepository salaryRepository;
    private final SeedProperties seedProperties;
    private final Random deterministicRandom;

    public DataSeeder(EmployeeRepository employeeRepository,
                      SalaryRepository salaryRepository,
                      SeedProperties seedProperties) {
        this.employeeRepository = employeeRepository;
        this.salaryRepository = salaryRepository;
        this.seedProperties = seedProperties;
        this.deterministicRandom = new Random(12345L);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedProperties.isEnabled()) {
            log.info("Seed disabled via app.seed.enabled=false; skipping employee bootstrap.");
            return;
        }

        int employeeCount = seedProperties.getEmployeeCount();
        long existingEmployees = employeeRepository.count();
        if (existingEmployees > 0) {
            log.info("Seed skipped because employee data already exists ({} employees present).", existingEmployees);
            return;
        }

        log.info("Starting deterministic seed for {} employees.", employeeCount);

        List<Employee> seededEmployees = generateEmployees(employeeCount);
        List<Employee> savedEmployees = new ArrayList<>(seededEmployees.size());

        for (int i = 0; i < seededEmployees.size(); i += BATCH_SIZE) {
            List<Employee> batch = seededEmployees.subList(i, Math.min(i + BATCH_SIZE, seededEmployees.size()));
            savedEmployees.addAll(employeeRepository.saveAll(batch));
        }

        List<Salary> salarySeedRecords = new ArrayList<>(savedEmployees.size() * 3);
        for (int i = 0; i < savedEmployees.size(); i++) {
            salarySeedRecords.addAll(generateSalaryHistory(savedEmployees.get(i), i));
            if (salarySeedRecords.size() >= BATCH_SIZE) {
                salaryRepository.saveAll(salarySeedRecords);
                salarySeedRecords.clear();
            }
        }

        if (!salarySeedRecords.isEmpty()) {
            salaryRepository.saveAll(salarySeedRecords);
        }

        log.info("Seed completed: {} employees and {} salary records created.",
                savedEmployees.size(), savedEmployees.size() * 3);
    }

    private List<Employee> generateEmployees(int employeeCount) {
        List<Employee> employees = new ArrayList<>(employeeCount);
        for (int index = 0; index < employeeCount; index++) {
            employees.add(buildEmployee(index));
        }
        return employees;
    }

    private Employee buildEmployee(int index) {
        String firstName = FIRST_NAMES.get(index % FIRST_NAMES.size());
        String lastName = LAST_NAMES.get((index * 7 + 3) % LAST_NAMES.size());
        String employeeCode = "EMP" + String.format("%06d", index + 1);
        String email = "employee" + String.format("%06d", index + 1) + "@acme.example";

        String country = COUNTRIES.get((index * 3 + 1) % COUNTRIES.size());
        String department = DEPARTMENTS.get((index * 3 + 2) % DEPARTMENTS.size());
        String jobTitle = JOB_TITLES.get((index * 3 + 1) % JOB_TITLES.size());

        return new Employee(
                employeeCode,
                firstName,
                lastName,
                email,
                country,
                department,
                jobTitle
        );
    }

    private List<Salary> generateSalaryHistory(Employee employee, int employeeIndex) {
        String country = employee.getCountry();
        String jobTitle = employee.getJobTitle();
        Currency primaryCurrency = resolveCurrency(country, employeeIndex);

        BigDecimal currentAmount = calculateCurrentSalary(jobTitle, country, employeeIndex, primaryCurrency);
        BigDecimal previousAmount = currentAmount.multiply(new BigDecimal("0.88")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal olderAmount = currentAmount.multiply(new BigDecimal("0.72")).setScale(2, java.math.RoundingMode.HALF_UP);

        LocalDate currentEffectiveFrom = LocalDate.of(2026, 1, 1);
        LocalDate previousEffectiveFrom = LocalDate.of(2025, 1, 1);
        LocalDate olderEffectiveFrom = LocalDate.of(2024, 1, 1);

        Salary olderSalary = new Salary(employee, olderAmount, primaryCurrency, olderEffectiveFrom, LocalDate.of(2024, 12, 31));
        Salary previousSalary = new Salary(employee, previousAmount, primaryCurrency, previousEffectiveFrom, LocalDate.of(2025, 12, 31));
        Salary currentSalary = new Salary(employee, currentAmount, primaryCurrency, currentEffectiveFrom, null);

        return List.of(olderSalary, previousSalary, currentSalary);
    }

    private Currency resolveCurrency(String country, int employeeIndex) {
        if (country == null) {
            return CURRENCIES.get(employeeIndex % CURRENCIES.size());
        }

        return switch (country) {
            case "India" -> Currency.INR;
            case "United States" -> Currency.USD;
            case "United Kingdom" -> Currency.GBP;
            case "Germany", "France" -> Currency.EUR;
            case "Canada" -> Currency.CAD;
            case "Australia" -> Currency.AUD;
            case "Singapore" -> Currency.SGD;
            case "Japan" -> Currency.JPY;
            default -> CURRENCIES.get((employeeIndex + 2) % CURRENCIES.size());
        };
    }

    private BigDecimal calculateCurrentSalary(String jobTitle, String country, int employeeIndex, Currency currency) {
        long base = switch (jobTitle) {
            case "Software Engineer" -> 75000L;
            case "Senior Software Engineer" -> 110000L;
            case "Tech Lead" -> 140000L;
            case "Engineering Manager" -> 180000L;
            case "Business Analyst" -> 82000L;
            case "Financial Analyst" -> 90000L;
            case "HR Manager" -> 95000L;
            case "Product Manager" -> 130000L;
            case "Project Manager" -> 115000L;
            case "Sales Manager" -> 95000L;
            case "Customer Success Manager" -> 78000L;
            case "Research Scientist" -> 100000L;
            case "Data Analyst" -> 86000L;
            case "Security Engineer" -> 135000L;
            default -> 80000L;
        };

        long countryAdjust = switch (country) {
            case "India" -> 25000L;
            case "United States" -> 30000L;
            case "United Kingdom" -> 20000L;
            case "Germany" -> 22000L;
            case "Canada" -> 17000L;
            case "Australia" -> 18000L;
            case "Singapore" -> 16000L;
            case "Japan" -> 19000L;
            case "France" -> 21000L;
            case "Brazil" -> 12000L;
            default -> 10000L;
        };

        long variance = Math.abs(deterministicRandom.nextInt(12000));
        long amount = base + countryAdjust + variance + (employeeIndex % 7) * 500L;

        if (currency == Currency.INR) {
            amount = Math.round(amount * 1.4d);
        }
        if (currency == Currency.JPY) {
            amount = Math.round(amount * 120d);
        }

        return BigDecimal.valueOf(amount).setScale(2);
    }
}
