package com.acme.salary.repository;

import com.acme.salary.entity.Employee;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

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
            String normalized = normalizeSearchKeyword(keyword);
            if (normalized == null) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";

            Predicate codeMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("employeeCode")), searchPattern);
            Predicate firstNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), searchPattern);
            Predicate lastNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), searchPattern);
            Predicate emailMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), searchPattern);
            Predicate fullNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.concat(
                            criteriaBuilder.concat(root.get("firstName"), " "),
                            root.get("lastName"))),
                    searchPattern);
            Predicate reversedFullNameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.concat(
                            criteriaBuilder.concat(root.get("lastName"), " "),
                            root.get("firstName"))),
                    searchPattern);

            return criteriaBuilder.or(codeMatch, firstNameMatch, lastNameMatch, emailMatch, fullNameMatch, reversedFullNameMatch);
        };
    }

    /**
     * Filter employees by country using case-insensitive partial match.
     *
     * @param country Country name fragment
     * @return Specification for partial country match
     */
    public static Specification<Employee> filterByCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            String normalized = normalizeFilterValue(country);
            if (normalized == null) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), searchPattern);
        };
    }

    /**
     * Filter employees by department using case-insensitive partial match.
     *
     * @param department Department name fragment
     * @return Specification for partial department match
     */
    public static Specification<Employee> filterByDepartment(String department) {
        return (root, query, criteriaBuilder) -> {
            String normalized = normalizeFilterValue(department);
            if (normalized == null) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), searchPattern);
        };
    }

    /**
     * Filter employees by job title using case-insensitive partial match.
     *
     * @param jobTitle Job title fragment
     * @return Specification for partial job title match
     */
    public static Specification<Employee> filterByJobTitle(String jobTitle) {
        return (root, query, criteriaBuilder) -> {
            String normalized = normalizeFilterValue(jobTitle);
            if (normalized == null) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("jobTitle")), searchPattern);
        };
    }

    private static String normalizeFilterValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeSearchKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
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
