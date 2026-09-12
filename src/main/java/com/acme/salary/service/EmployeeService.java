package com.acme.salary.service;

import com.acme.salary.dto.PageResponse;
import com.acme.salary.dto.employee.CreateEmployeeRequest;
import com.acme.salary.dto.employee.EmployeeResponse;
import com.acme.salary.dto.employee.UpdateEmployeeRequest;
import com.acme.salary.entity.Employee;
import com.acme.salary.exception.EmployeeNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.EmployeeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for Employee operations.
 * Handles business logic, validation, and data transformation.
 */
@Service
@Transactional
public class EmployeeService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "lastName";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    // Allowed sort fields to prevent SQL injection
    private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "id",
            "employeeCode",
            "firstName",
            "lastName",
            "email",
            "country",
            "department",
            "jobTitle",
            "createdAt",
            "updatedAt"
    ));

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get paginated list of employees with optional filtering and searching.
     *
     * @param page Page number (0-indexed), default 0
     * @param size Page size, default 20, max 100
     * @param sort Sort field and direction (e.g., "lastName,asc"), default "lastName,asc"
     * @param search Keyword search across employeeCode, firstName, lastName, email
     * @param country Filter by country (exact match)
     * @param department Filter by department (exact match)
     * @param jobTitle Filter by job title (exact match)
     * @return PageResponse containing employees and pagination metadata
     * @throws IllegalArgumentException if page size exceeds limit or invalid sort field
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployees(
            Integer page,
            Integer size,
            String sort,
            String search,
            String country,
            String department,
            String jobTitle) {

        // Apply defaults
        int pageNum = (page != null) ? page : DEFAULT_PAGE;
        int pageSize = (size != null) ? size : DEFAULT_SIZE;
        String sortStr = (sort != null && !sort.isBlank()) ? sort : DEFAULT_SORT_FIELD + "," + DEFAULT_SORT_DIRECTION;

        // Validate page size
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE + ", got: " + pageSize);
        }

        // Parse and validate sort
        Sort sortObject = parseSortParameter(sortStr);

        // Create Pageable
        Pageable pageable = PageRequest.of(pageNum, pageSize, sortObject);

        // Build specification for filtering and searching
        Specification<Employee> specification = EmployeeSpecification.combineFilters(
                search, country, department, jobTitle);

        // Query repository with specification and pagination
        Page<Employee> employeePage = employeeRepository.findAll(specification, pageable);

        // Map entities to DTOs
        List<EmployeeResponse> content = employeePage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Return PageResponse
        return new PageResponse<>(
                content,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isFirst(),
                employeePage.isLast()
        );
    }

    /**
     * Get all employees (backward compatibility, uses pagination).
     *
     * @return PageResponse with first page using defaults
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees() {
        return getEmployees(DEFAULT_PAGE, DEFAULT_SIZE, null, null, null, null, null);
    }

    /**
     * Get an employee by ID.
     *
     * @param id Employee ID
     * @return EmployeeResponse
     * @throws EmployeeNotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));
        return mapToResponse(employee);
    }

    /**
     * Create a new employee.
     * Validates that employeeCode and email are unique.
     *
     * @param request CreateEmployeeRequest
     * @return EmployeeResponse
     * @throws IllegalArgumentException if employeeCode or email already exists
     */
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        validateEmployeeCodeUniqueness(request.employeeCode());
        validateEmailUniqueness(request.email());

        Employee employee = new Employee(
                request.employeeCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.country(),
                request.department(),
                request.jobTitle()
        );

        Employee savedEmployee = employeeRepository.save(employee);
        return mapToResponse(savedEmployee);
    }

    /**
     * Update an existing employee.
     * Validates that updated email and employeeCode (if changed) do not conflict with existing records.
     *
     * @param id Employee ID
     * @param request UpdateEmployeeRequest
     * @return EmployeeResponse
     * @throws EmployeeNotFoundException if employee not found
     * @throws IllegalArgumentException if email already exists on another employee
     */
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

        // Validate email uniqueness if email is being updated
        if (request.email() != null && !request.email().equals(employee.getEmail())) {
            validateEmailUniqueness(request.email());
        }

        // Update fields if provided (non-null)
        if (request.firstName() != null) {
            employee.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName());
        }
        if (request.email() != null) {
            employee.setEmail(request.email());
        }
        if (request.country() != null) {
            employee.setCountry(request.country());
        }
        if (request.department() != null) {
            employee.setDepartment(request.department());
        }
        if (request.jobTitle() != null) {
            employee.setJobTitle(request.jobTitle());
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToResponse(updatedEmployee);
    }

    /**
     * Validate that employeeCode is unique.
     *
     * @param employeeCode Employee code to validate
     * @throws IllegalArgumentException if employeeCode already exists
     */
    private void validateEmployeeCodeUniqueness(String employeeCode) {
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new IllegalArgumentException("Employee code '" + employeeCode + "' already exists");
        }
    }

    /**
     * Validate that email is unique.
     *
     * @param email Email to validate
     * @throws IllegalArgumentException if email already exists
     */
    private void validateEmailUniqueness(String email) {
        if (employeeRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' already exists");
        }
    }

    /**
     * Parse sort parameter and create Sort object.
     * Format: "fieldName,asc" or "fieldName,desc"
     *
     * @param sortStr Sort string
     * @return Sort object
     * @throws IllegalArgumentException if sort field is not allowed
     */
    private Sort parseSortParameter(String sortStr) {
        if (sortStr == null || sortStr.isBlank()) {
            return Sort.by(Sort.Direction.ASC, DEFAULT_SORT_FIELD);
        }

        String[] parts = sortStr.split(",");
        if (parts.length < 1 || parts.length > 2) {
            throw new IllegalArgumentException("Invalid sort format. Use: fieldName,asc or fieldName,desc");
        }

        String fieldName = parts[0].trim();
        String direction = (parts.length == 2) ? parts[1].trim().toLowerCase() : DEFAULT_SORT_DIRECTION;

        // Validate field name
        if (!ALLOWED_SORT_FIELDS.contains(fieldName)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + fieldName + ". Allowed fields: " + ALLOWED_SORT_FIELDS);
        }

        // Validate direction
        if (!direction.equals("asc") && !direction.equals("desc")) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc', got: " + direction);
        }

        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, fieldName);
    }

    /**
     * Map Employee entity to EmployeeResponse DTO.
     *
     * @param employee Employee entity
     * @return EmployeeResponse
     */
    private EmployeeResponse mapToResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getCountry(),
                employee.getDepartment(),
                employee.getJobTitle(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
