package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic filtering and searching of employees.
 * Provides composable predicates for building complex WHERE clauses.
 */
public class EmployeeSpecification {

    /**
     * Search employees by keyword across multiple fields.
     * Case-insensitive partial match on: employeeCode, firstName, lastName, email.
     *
     * @param keyword Search keyword
     * @return Specification combining OR predicates for multiple fields
     */
    public static Specification<Employee> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + keyword.toLowerCase() + "%";

            Predicate codeMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("employeeCode")), searchPattern);
            Predicate firstNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), searchPattern);
            Predicate lastNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), searchPattern);
            Predicate emailMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), searchPattern);

            return criteriaBuilder.or(codeMatch, firstNameMatch, lastNameMatch, emailMatch);
        };
    }

    /**
     * Filter employees by country (exact match).
     *
     * @param country Country name
     * @return Specification for exact country match
     */
    public static Specification<Employee> filterByCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (country == null || country.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("country"), country);
        };
    }

    /**
     * Filter employees by department (exact match).
     *
     * @param department Department name
     * @return Specification for exact department match
     */
    public static Specification<Employee> filterByDepartment(String department) {
        return (root, query, criteriaBuilder) -> {
            if (department == null || department.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("department"), department);
        };
    }

    /**
     * Filter employees by job title (exact match).
     *
     * @param jobTitle Job title
     * @return Specification for exact job title match
     */
    public static Specification<Employee> filterByJobTitle(String jobTitle) {
        return (root, query, criteriaBuilder) -> {
            if (jobTitle == null || jobTitle.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("jobTitle"), jobTitle);
        };
    }

    /**
     * Combine multiple filter specifications.
     * All filters are applied with AND logic.
     *
     * @param search Keyword search
     * @param country Country filter
     * @param department Department filter
     * @param jobTitle Job title filter
     * @return Combined Specification
     */
    public static Specification<Employee> combineFilters(
            String search,
            String country,
            String department,
            String jobTitle) {

        return Specification
                .where(searchByKeyword(search))
                .and(filterByCountry(country))
                .and(filterByDepartment(department))
                .and(filterByJobTitle(jobTitle));
    }
}
