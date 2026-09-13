# ACME Employee Salary Management — Testing Strategy

## 1. Testing Goals

Testing focuses on the business-critical behaviors that matter most to the assessment: correctness of the salary lifecycle, employee management rules, server-side search and filtering logic, and compensation analytics. The goal is meaningful confidence in the application's core domain logic rather than maximizing test count.

The current test suite is designed to verify that:
- salary rules are enforced consistently
- employee data management behaves as expected
- employee filtering and pagination remain server-side and deterministic
- compensation analytics return valid aggregated results
- the application remains repeatable and easy to run in local development and test environments

## 2. Test Approach

The current project uses a pragmatic mix of fast unit tests and focused service-level verification. Repository dependencies are mocked in several service tests so the behavior can be validated without requiring a live database or external services. This keeps tests deterministic and fast while still testing important business logic.

The main test patterns in the project are:
- fast service-level unit tests for salary and employee logic
- specification tests for employee filtering/search behavior
- analytics service tests for summary and breakdown results
- a lightweight application context test to confirm the Spring application loads

Unit tests are preferred for business rules because they isolate behavior, run quickly, and avoid unnecessary database and network dependencies. This fits the assessment goal of validating core domain correctness without turning test execution into a slow integration exercise.

## 3. SalaryService Tests

The SalaryService tests focus on the domain's most important integrity rules. The current SalaryServiceTest covers:
- successful initial salary creation
- positive salary validation
- rejection of zero and negative salary amounts
- invalid currency rejection
- missing `effectiveFrom` date rejection
- closing the previous active salary when a new salary becomes effective
- correct `effectiveTo` calculation before the new salary starts
- rejection of overlapping salary periods
- rejection of a new salary when the amount and currency are unchanged from the current salary
- allowing the same amount with a different currency
- allowing the same amount and currency when the comparison is against a historical salary instead of the current salary
- salary history ordering
- employee-not-found behavior
- service-level transaction boundary verification

These tests validate the core salary lifecycle: maintain a current active salary, preserve history, prevent invalid periods, and reject unhelpful duplicate salary states. This is the most business-critical part of the domain and therefore receives the most explicit coverage.

## 4. EmployeeService Tests

The EmployeeService tests cover the service-layer rules that are not already validated as repository-level specification behavior. They include:
- duplicate employee code validation during creation
- duplicate email validation during creation
- duplicate email validation during update
- employee-not-found behavior
- page-size validation
- name sorting behavior translated to `firstName` then `lastName`

This test set intentionally complements the repository specification tests rather than duplicating them. EmployeeServiceTest validates service behavior around validation, lookup, pagination constraints, and sort handling, while EmployeeSpecificationTest validates the actual database query logic for filtering and searching.

## 5. Employee Search and Filtering Tests

The EmployeeSpecificationTest covers the server-side filtering and search logic used by employee listing. The actual scenarios include:
- employee code search
- first-name and last-name search
- full-name search
- email search
- country partial-match filtering
- department partial-match filtering
- job-title partial-match filtering
- case-insensitive matching
- partial matching
- combined filter behavior using AND semantics
- blank/empty filter handling returning the applicable records
- pagination and sorting behavior after filtering

The important point is that this suite validates the specification behavior that drives the employee list; it is where the server-side filter contracts are proven. These tests are especially relevant because the application is designed for a large employee population and avoids client-side filtering.

## 6. Compensation Analytics Tests

The CompensationAnalyticsServiceTest verifies the application’s compensation aggregation behavior. The current test coverage includes:
- summary analytics
- country breakdown
- department breakdown
- job-title breakdown
- default reporting currency behavior
- explicit reporting currency behavior
- invalid currency handling
- comparison of USD and converted-currency results
- empty-data handling in the analytics path

These tests validate that analytics are produced from the current-salary dataset only and that reporting currency changes affect the salary values without changing the employee count. The service tests focus on the most important business outputs of the analytics layer: aggregated compensation values by summary and by grouping dimension.

## 7. Test Characteristics

The current tests are intentionally selected to be:
- fast: they avoid heavyweight infrastructure and external dependencies
- deterministic: fixed dates, salary amounts, and setup data are used
- isolated: service-level behavior is tested without unnecessary system complexity
- easy to understand: the assertions match the business rule being validated
- stable: they do not rely on current time or random data

This makes the suite reliable during normal development and suitable for validating the core compensation and employee-management logic that the assessment depends on.

## 8. Test Execution

The project executes the backend test suite with:

./gradlew test --no-daemon

Current project result:
- 34 tests passing
- BUILD SUCCESSFUL

No coverage percentage is being claimed in this document because the project does not currently publish a measured coverage report as part of the assessment workflow.

## 9. Testing Trade-offs and Future Improvements

The current suite emphasizes unit-level business correctness because that provides the fastest feedback for the most important rules. This is a sensible balance for the assessment: it validates the core domain behavior without slowing development or depending on broad integration environments.

Potential future additions could include:
- repository/database integration tests
- controller or API integration tests
- end-to-end UI tests
- automated performance or load tests
- test coverage reporting

These are future improvements, not claims about the current implementation. The existing suite is intentionally focused on the behaviors the assessment cares about most.
