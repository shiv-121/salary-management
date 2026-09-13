# ACME Employee Salary Management — Design Decisions

## 1. Layered Spring Boot Architecture

Decision:
Use a conventional layered architecture: Controller → Service → Repository → PostgreSQL.

Why:
- clear separation of responsibilities
- easier to understand and maintain
- business rules remain in services
- persistence concerns remain in repositories
- appropriate complexity for an application of this size

Trade-off:
A modular monolith is less operationally complex than microservices, but it keeps all backend capabilities in one deployable application. That is a good match for the current assessment scope where the priority is correctness and maintainability rather than distributed system complexity.

## 2. PostgreSQL as the Relational Database

PostgreSQL was selected because the domain is strongly relational. Employee and salary data are tied together by business relationships, and salary history needs consistent date-based semantics. PostgreSQL supports efficient grouping and aggregation, which is essential for compensation analytics and server-side reporting.

The application is designed for approximately 10,000 employees, which fits comfortably in a relational model with indexed lookup and database-side aggregation. The trade-off is that a relational database requires schema management and explicit data integrity rules, but the domain benefits significantly from structured relationships and SQL-driven analytics.

## 3. Separate Employee and Salary Entities

Salary is modeled separately from Employee because salary is temporal and historical. An employee can have many salary records over time, and those records must be preserved without overwriting prior compensation data. The current salary can be identified using `effectiveTo IS NULL`.

This is somewhat more complex than storing only the latest salary, but it preserves both past history and current state correctly. The employee entity remains lightweight and does not maintain a bidirectional salary collection, which keeps the employee aggregate simpler and avoids unnecessarily loading historical salary rows when they are not required.

## 4. Salary History as Append-Oriented Records

The salary model is append-oriented: each salary change creates a new salary record rather than updating the historical record in place. The previous active salary is closed one day before the new salary's `effectiveFrom` date, and overlapping periods are rejected.

Example:
- 2025-01-01 → 2025-12-31 : USD 70,000
- 2026-01-01 → null        : USD 75,000

This protects historical correctness because the system can preserve salary history without destructive updates. Salary operations are transactional so the closing of the previous salary and creation of the new active salary happen as one unit of work. This reduces the chance of leaving the compensation timeline in an inconsistent state.

## 5. BigDecimal for Salary Amounts

The application uses `BigDecimal` for salary amounts instead of floating-point numbers. This is important because salary is monetary data, and explicit decimal precision is safer than relying on binary floating-point approximations. It keeps calculations predictable and makes the financial values easier to reason about and validate.

## 6. Server-Side Pagination and Filtering

Employee search, filtering, and pagination are implemented on the backend because the dataset is expected to be roughly 10,000 employees. This avoids sending the entire dataset to the client, reduces browser memory usage, and keeps the UI responsive even as the dataset grows.

The backend builds database-backed queries for filters and search, so filtering is performed by PostgreSQL rather than in the Angular client. It returns only the requested page of results and uses a bounded page size. This design keeps the API efficient and allows the database to do the work of narrowing results instead of the frontend.

## 7. Database-Side Compensation Analytics

The analytics layer calculates average, minimum, maximum, median, and grouped breakdown results in PostgreSQL instead of loading all salary rows into Java memory. This is the right trade-off for the assessment because the database is optimized for grouping and aggregation.

This keeps the service response small and reduces memory and network overhead. The main trade-off is that repository queries become more complex, but that complexity is justified by the performance and scalability benefits for a dataset of this size.

## 8. Reporting Currency and Static FX Rates

The application defaults to USD as the reporting currency and allows supported currencies to be selected by the user. Salary analytics are converted before aggregation so the grouped results reflect the requested reporting currency.

Static and deterministic exchange-rate assumptions are intentionally used instead of a live FX service. This keeps the application reproducible, simple to test, and free from external dependency during assessment use. The rates are explicit reporting assumptions and are not intended to represent real-time market rates.

Trade-off:
This is not a production-market FX solution. A future production system would likely use a trusted external FX provider with versioned or effective-dated rates and a clearer policy around real-time valuation.

## 9. DTO-Based API Contracts

The application uses DTOs for API requests and responses rather than exposing persistence entities directly. This separates the external contract from the internal database model, keeps the API easier to evolve, and allows request validation without leaking internal entity state.

This also keeps the domain model cleaner: the API contract can be shaped to the business workflow rather than the persistence structure. The result is a clearer boundary between the HTTP layer and the database model.

## 10. Validation and Business Rules

Validation is intentionally split between request validation and business validation:
- Request validation: DTO and Bean Validation
- Business validation: service layer
- Persistence: repository/database

This is important because business rules such as positive salary amounts, valid currency values, required effective dates, no overlapping periods, one current salary, and duplicate current-salary amount/currency checks must be enforced server-side. These rules are not something that should be trusted to the client alone; they must be enforced in the backend to maintain integrity.

## 11. Deterministic Seed Data

The seed data is deterministic and repeatable. It creates a realistic employee population for local development and demonstration, with 10,000 employees and 3 salary records per employee. The seeded data includes multiple countries, departments, job titles, and currencies, and it is designed to reflect the shape of the real domain without depending on random values.

The seed is disabled by default, which keeps production startup behavior predictable. The batched approach also improves efficiency when populating a large dataset. This is a practical choice because deterministic data is valuable for demos, testing, and performance validation.

## 12. No Authentication in Current Scope

The current scope does not include authentication or role-based access control beyond the HR Manager persona. The assessment is focused on employee salary management and compensation analytics, not on building a broader enterprise identity platform.

This is not described as a completed security feature; it is a deliberate scope boundary. A production system would likely add authentication and authorization as separate concerns with stronger access control and audit requirements.

## 13. Docker + Render Deployment

The backend is packaged for Docker deployment, which provides a consistent runtime environment for the Spring Boot application. This helps ensure the Java runtime and dependencies behave consistently across environments.

The deployment model separates the frontend from the backend:
- Angular static site
- Spring Boot API service
- PostgreSQL database

This keeps the presentation layer independent from the server-side business logic while still allowing the application to function as a single coherent system.

## 14. Key Trade-off Summary

| Area | Decision | Why |
|---|---|---|
| Backend | Layered monolith | Simplicity and maintainability |
| Database | PostgreSQL | Relational domain and aggregation |
| Salary model | Separate historical records | Preserve compensation history |
| Monetary type | BigDecimal | Precise financial values |
| Pagination | Server-side | Supports ~10k employees |
| Analytics | Database-side | Efficient aggregation |
| FX | Static deterministic rates | Reproducibility |
| Seed | Deterministic 10k dataset | Repeatable validation |
| Auth | Out of scope | Focus on assessment problem |
| Deployment | Docker + Render | Reproducible deployment |

## 15. Future Improvements

The following are future improvement areas and are not claimed to currently exist:
- authentication and authorization
- versioned database migrations such as Flyway
- a production FX provider with effective-dated rates
- comprehensive audit logging
- load and performance testing
- observability and monitoring
- CI/CD improvements

These would enhance operational maturity, but they are intentionally outside the current implementation scope.
