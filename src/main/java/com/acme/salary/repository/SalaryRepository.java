package com.acme.salary.repository;

import com.acme.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId ORDER BY s.effectiveFrom DESC")
    List<Salary> findSalaryHistoryByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.effectiveTo IS NULL")
    Optional<Salary> findCurrentSalaryByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.effectiveFrom <= :date AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)")
    Optional<Salary> findActiveSalaryByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query(value = """
            SELECT CAST(AVG(converted_salary) AS DECIMAL(19,2)) AS averageSalary,
                   CAST(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY converted_salary) AS DECIMAL(19,2)) AS medianSalary,
                   CAST(MAX(converted_salary) AS DECIMAL(19,2)) AS highestSalary,
                   CAST(MIN(converted_salary) AS DECIMAL(19,2)) AS lowestSalary
            FROM (
                SELECT CASE s.currency
                    WHEN 'USD' THEN s.amount
                    WHEN 'EUR' THEN s.amount * :eurRate
                    WHEN 'GBP' THEN s.amount * :gbpRate
                    WHEN 'INR' THEN s.amount * :inrRate
                    WHEN 'CAD' THEN s.amount * :cadRate
                    WHEN 'AUD' THEN s.amount * :audRate
                    WHEN 'SGD' THEN s.amount * :sgdRate
                    WHEN 'JPY' THEN s.amount * :jpyRate
                    ELSE s.amount
                END AS converted_salary
                FROM salaries s
                WHERE s.effective_to IS NULL
            ) current_salary_values
            """, nativeQuery = true)
    CompensationSummaryProjection findCurrentSalarySummary(
            @Param("eurRate") BigDecimal eurRate,
            @Param("gbpRate") BigDecimal gbpRate,
            @Param("inrRate") BigDecimal inrRate,
            @Param("cadRate") BigDecimal cadRate,
            @Param("audRate") BigDecimal audRate,
            @Param("sgdRate") BigDecimal sgdRate,
            @Param("jpyRate") BigDecimal jpyRate);

    @Query(value = """
            SELECT e.country AS groupName,
                   COUNT(DISTINCT salary_values.employee_id) AS employeeCount,
                   CAST(AVG(salary_values.converted_salary) AS DECIMAL(19,2)) AS averageSalary,
                   CAST(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary_values.converted_salary) AS DECIMAL(19,2)) AS medianSalary
            FROM (
                SELECT s.employee_id, s.currency, s.amount,
                       CASE s.currency
                           WHEN 'USD' THEN s.amount
                           WHEN 'EUR' THEN s.amount * :eurRate
                           WHEN 'GBP' THEN s.amount * :gbpRate
                           WHEN 'INR' THEN s.amount * :inrRate
                           WHEN 'CAD' THEN s.amount * :cadRate
                           WHEN 'AUD' THEN s.amount * :audRate
                           WHEN 'SGD' THEN s.amount * :sgdRate
                           WHEN 'JPY' THEN s.amount * :jpyRate
                           ELSE s.amount
                       END AS converted_salary
                FROM salaries s
                WHERE s.effective_to IS NULL
            ) salary_values
            INNER JOIN employees e ON e.id = salary_values.employee_id
            GROUP BY e.country
            ORDER BY AVG(salary_values.converted_salary) DESC, e.country ASC
            """, nativeQuery = true)
    List<CompensationBreakdownProjection> findCurrentSalaryBreakdownByCountry(
            @Param("eurRate") BigDecimal eurRate,
            @Param("gbpRate") BigDecimal gbpRate,
            @Param("inrRate") BigDecimal inrRate,
            @Param("cadRate") BigDecimal cadRate,
            @Param("audRate") BigDecimal audRate,
            @Param("sgdRate") BigDecimal sgdRate,
            @Param("jpyRate") BigDecimal jpyRate);

    @Query(value = """
            SELECT e.department AS groupName,
                   COUNT(DISTINCT salary_values.employee_id) AS employeeCount,
                   CAST(AVG(salary_values.converted_salary) AS DECIMAL(19,2)) AS averageSalary,
                   CAST(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary_values.converted_salary) AS DECIMAL(19,2)) AS medianSalary
            FROM (
                SELECT s.employee_id, s.currency, s.amount,
                       CASE s.currency
                           WHEN 'USD' THEN s.amount
                           WHEN 'EUR' THEN s.amount * :eurRate
                           WHEN 'GBP' THEN s.amount * :gbpRate
                           WHEN 'INR' THEN s.amount * :inrRate
                           WHEN 'CAD' THEN s.amount * :cadRate
                           WHEN 'AUD' THEN s.amount * :audRate
                           WHEN 'SGD' THEN s.amount * :sgdRate
                           WHEN 'JPY' THEN s.amount * :jpyRate
                           ELSE s.amount
                       END AS converted_salary
                FROM salaries s
                WHERE s.effective_to IS NULL
            ) salary_values
            INNER JOIN employees e ON e.id = salary_values.employee_id
            GROUP BY e.department
            ORDER BY AVG(salary_values.converted_salary) DESC, e.department ASC
            """, nativeQuery = true)
    List<CompensationBreakdownProjection> findCurrentSalaryBreakdownByDepartment(
            @Param("eurRate") BigDecimal eurRate,
            @Param("gbpRate") BigDecimal gbpRate,
            @Param("inrRate") BigDecimal inrRate,
            @Param("cadRate") BigDecimal cadRate,
            @Param("audRate") BigDecimal audRate,
            @Param("sgdRate") BigDecimal sgdRate,
            @Param("jpyRate") BigDecimal jpyRate);

    @Query(value = """
            SELECT e.job_title AS groupName,
                   COUNT(DISTINCT salary_values.employee_id) AS employeeCount,
                   CAST(AVG(salary_values.converted_salary) AS DECIMAL(19,2)) AS averageSalary,
                   CAST(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary_values.converted_salary) AS DECIMAL(19,2)) AS medianSalary
            FROM (
                SELECT s.employee_id, s.currency, s.amount,
                       CASE s.currency
                           WHEN 'USD' THEN s.amount
                           WHEN 'EUR' THEN s.amount * :eurRate
                           WHEN 'GBP' THEN s.amount * :gbpRate
                           WHEN 'INR' THEN s.amount * :inrRate
                           WHEN 'CAD' THEN s.amount * :cadRate
                           WHEN 'AUD' THEN s.amount * :audRate
                           WHEN 'SGD' THEN s.amount * :sgdRate
                           WHEN 'JPY' THEN s.amount * :jpyRate
                           ELSE s.amount
                       END AS converted_salary
                FROM salaries s
                WHERE s.effective_to IS NULL
            ) salary_values
            INNER JOIN employees e ON e.id = salary_values.employee_id
            GROUP BY e.job_title
            ORDER BY AVG(salary_values.converted_salary) DESC, e.job_title ASC
            """, nativeQuery = true)
    List<CompensationBreakdownProjection> findCurrentSalaryBreakdownByJobTitle(
            @Param("eurRate") BigDecimal eurRate,
            @Param("gbpRate") BigDecimal gbpRate,
            @Param("inrRate") BigDecimal inrRate,
            @Param("cadRate") BigDecimal cadRate,
            @Param("audRate") BigDecimal audRate,
            @Param("sgdRate") BigDecimal sgdRate,
            @Param("jpyRate") BigDecimal jpyRate);

    interface CompensationSummaryProjection {
        BigDecimal getAverageSalary();
        BigDecimal getMedianSalary();
        BigDecimal getHighestSalary();
        BigDecimal getLowestSalary();
    }

    interface CompensationBreakdownProjection {
        String getGroupName();
        Long getEmployeeCount();
        BigDecimal getAverageSalary();
        BigDecimal getMedianSalary();
    }
}
