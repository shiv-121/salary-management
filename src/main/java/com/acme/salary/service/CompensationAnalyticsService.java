package com.acme.salary.service;

import com.acme.salary.config.CurrencyExchangeRates;
import com.acme.salary.dto.analytics.CompensationBreakdownResponse;
import com.acme.salary.dto.analytics.CompensationSummaryResponse;
import com.acme.salary.enums.Currency;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CompensationAnalyticsService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrencyExchangeRates currencyExchangeRates;

    public CompensationAnalyticsService(
            SalaryRepository salaryRepository,
            EmployeeRepository employeeRepository,
            CurrencyExchangeRates currencyExchangeRates) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.currencyExchangeRates = currencyExchangeRates;
    }

    public CompensationSummaryResponse getSummary() {
        long totalEmployees = employeeRepository.count();

        SalaryRepository.CompensationSummaryProjection projection = salaryRepository.findCurrentSalarySummary(
                currencyExchangeRates.getRate(Currency.EUR),
                currencyExchangeRates.getRate(Currency.GBP),
                currencyExchangeRates.getRate(Currency.INR),
                currencyExchangeRates.getRate(Currency.CAD),
                currencyExchangeRates.getRate(Currency.AUD),
                currencyExchangeRates.getRate(Currency.SGD),
                currencyExchangeRates.getRate(Currency.JPY));

        return new CompensationSummaryResponse(
                currencyExchangeRates.getReportingCurrencyCode(),
                totalEmployees,
                projection != null ? projection.getAverageSalary() : null,
                projection != null ? projection.getMedianSalary() : null,
                projection != null ? projection.getHighestSalary() : null,
                projection != null ? projection.getLowestSalary() : null
        );
    }

    public List<CompensationBreakdownResponse> getByCountry() {
        return mapBreakdown(salaryRepository.findCurrentSalaryBreakdownByCountry(
                currencyExchangeRates.getRate(Currency.EUR),
                currencyExchangeRates.getRate(Currency.GBP),
                currencyExchangeRates.getRate(Currency.INR),
                currencyExchangeRates.getRate(Currency.CAD),
                currencyExchangeRates.getRate(Currency.AUD),
                currencyExchangeRates.getRate(Currency.SGD),
                currencyExchangeRates.getRate(Currency.JPY)));
    }

    public List<CompensationBreakdownResponse> getByDepartment() {
        return mapBreakdown(salaryRepository.findCurrentSalaryBreakdownByDepartment(
                currencyExchangeRates.getRate(Currency.EUR),
                currencyExchangeRates.getRate(Currency.GBP),
                currencyExchangeRates.getRate(Currency.INR),
                currencyExchangeRates.getRate(Currency.CAD),
                currencyExchangeRates.getRate(Currency.AUD),
                currencyExchangeRates.getRate(Currency.SGD),
                currencyExchangeRates.getRate(Currency.JPY)));
    }

    public List<CompensationBreakdownResponse> getByJobTitle() {
        return mapBreakdown(salaryRepository.findCurrentSalaryBreakdownByJobTitle(
                currencyExchangeRates.getRate(Currency.EUR),
                currencyExchangeRates.getRate(Currency.GBP),
                currencyExchangeRates.getRate(Currency.INR),
                currencyExchangeRates.getRate(Currency.CAD),
                currencyExchangeRates.getRate(Currency.AUD),
                currencyExchangeRates.getRate(Currency.SGD),
                currencyExchangeRates.getRate(Currency.JPY)));
    }

    private List<CompensationBreakdownResponse> mapBreakdown(List<SalaryRepository.CompensationBreakdownProjection> projections) {
        return projections.stream()
                .map(projection -> new CompensationBreakdownResponse(
                        projection.getGroupName(),
                        projection.getEmployeeCount() != null ? projection.getEmployeeCount() : 0L,
                        projection.getAverageSalary(),
                        projection.getMedianSalary()))
                .collect(Collectors.toList());
    }
}
