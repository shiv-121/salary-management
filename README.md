# ACME Employee Salary Management

A web-based employee salary management system for ACME's HR Manager persona.

The application replaces spreadsheet-based salary tracking with a structured system for employee management, salary history, and compensation analytics.

## Live Application

- Web UI: https://salary-ui.onrender.com
- Swagger / OpenAPI: https://salary-management-6wdd.onrender.com/swagger-ui/index.html

> The application is deployed for assessment/demo purposes.

## Repositories

- Backend: https://github.com/shiv-121/salary-management
- Frontend: https://github.com/shiv-121/salary-ui

## Features

### Employee Management

- Server-side pagination
- Search by:
  - Employee code
  - First name
  - Last name
  - Full name
  - Email
- Filtering by country, department, and job title
- Case-insensitive partial matching
- Sorting by employee name
- Create and update employees
- Employee detail view

### Salary Management

- Add salary records
- Salary history preservation
- Current salary tracking
- Effective-from and effective-to dates
- Automatic closure of the previous active salary
- Overlapping salary period validation
- Positive salary validation
- Currency validation
- Prevention of duplicate current salary amount/currency combinations

Salary history is modeled as separate records so that previous compensation is preserved rather than overwritten.

### Compensation Analytics

- Total employees
- Average salary
- Minimum salary
- Maximum salary
- Median salary
- Breakdown by country
- Breakdown by department
- Breakdown by job title
- Reporting currency selection
- Dashboard visualizations

Analytics are calculated database-side and use deterministic reporting exchange rates.

## Architecture

```text
                    ┌──────────────────┐
                    │    HR Manager    │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │  Angular UI      │
                    │  Static Site     │
                    └────────┬─────────┘
                             │ REST / JSON
                             ▼
                    ┌──────────────────┐
                    │  Spring Boot API │
                    │                  │
                    │ Controller       │
                    │      ↓           │
                    │ Service          │
                    │      ↓           │
                    │ Repository       │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   PostgreSQL     │
                    └──────────────────┘
```

The backend follows a conventional layered architecture:

Controller
↓
Service
↓
Repository
↓
PostgreSQL

Business rules are kept in the service layer, while persistence concerns remain in repositories.

## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- Bean Validation
- PostgreSQL
- Gradle
- OpenAPI / Swagger
- Docker

### Frontend

- Angular 22
- TypeScript
- Node.js 24.15.0

### Deployment

- Render
- Dockerized Spring Boot backend
- Angular static site
- PostgreSQL

## Data Model

The core domain consists of two entities:

Employee
│
│ 1 : N
▼
Salary

Employee
│
│ 1 : N
▼
Salary

`effectiveTo = NULL` identifies the current salary.

When a new salary becomes effective, the previous active salary is closed on the day before the new salary's effective date.

Example:

- 2025-01-01 → 2025-12-31 : USD 70,000
- 2026-01-01 → NULL        : USD 75,000

Salary records are not updated or deleted through the salary management API, preserving historical compensation records.

## API

### Employees

- `GET /api/employees`
- `GET /api/employees/{id}`
- `POST /api/employees`
- `PUT /api/employees/{id}`

Example query:

```text
GET /api/employees?page=0&size=20&search=smith&country=USA
```

### Salaries

- `GET /api/employees/{employeeId}/salaries`
- `POST /api/employees/{employeeId}/salaries`
- `GET /api/employees/{employeeId}/salaries/current`

### Analytics

- `GET /api/analytics/summary`
- `GET /api/analytics/by-country`
- `GET /api/analytics/by-department`
- `GET /api/analytics/by-job-title`

Analytics support an optional reporting currency:

```text
GET /api/analytics/summary?currency=EUR
```

See the Swagger documentation for the complete API contract.

## Scale and Performance

The application is designed around the assessment target of approximately 10,000 employees.

Key considerations:

- Server-side pagination prevents loading the complete employee dataset into the browser.
- Search and filters are implemented through database-backed queries.
- Database indexes support common employee search and filtering fields.
- Compensation analytics use database-side aggregation.
- Salary-to-employee relationships use lazy loading where appropriate.
- Hibernate/JDBC batching is used during seed operations.
- Angular routes are lazy-loaded.
- Seed data is deterministic for repeatable testing and demos.

The implementation focuses on predictable behavior and bounded data transfer rather than claiming unsupported benchmark numbers.

## Seed Data

The application includes a deterministic seed mechanism.

Default configuration:

```properties
app.seed.enabled=false
app.seed.employee-count=10000
```

When enabled for initial setup, the seed creates:

- 10,000 employees
- 30,000 salary records
- 3 salary records per employee
- multiple countries
- multiple departments
- multiple job titles
- multiple currencies
- one current salary per employee
- non-overlapping salary periods

Employee codes are deterministic:

```text
EMP000001
EMP000002
...
EMP010000
```

## Local Development

```bash
git clone https://github.com/shiv-121/salary-management.git
cd salary-management
```

Configure the database using environment variables or the application's configuration, then run:

```bash
./gradlew bootRun
```

The API starts on:

- http://localhost:8080

Swagger is available at:

- http://localhost:8080/swagger-ui/index.html
