package com.acme.salary.repository;

import com.acme.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId ORDER BY s.effectiveFrom ASC")
    List<Salary> findSalaryHistoryByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.effectiveTo IS NULL")
    Optional<Salary> findCurrentSalaryByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT s FROM Salary s WHERE s.employee.id = :employeeId AND s.effectiveFrom <= :date AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)")
    Optional<Salary> findActiveSalaryByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
