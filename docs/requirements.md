# ACME Employee Salary Management — Requirements

## 1. Goal
ACME currently manages salary information for approximately 10,000 employees across multiple countries using spreadsheets. The goal is to provide HR Managers with a web-based system for employee salary management and compensation insights, replacing spreadsheet-based tracking with a structured, searchable application.

## 2. User Persona
HR Manager.

## 3. In Scope

### Employee Management
- View employees with server-side pagination.
- Search employees by employee code, first name, last name, full name, and email.
- Filter by country, department, and job title.
- Sort employees by name.
- Create and update employee information.
- View employee details.

### Salary Management
- Add salary records for employees.
- Store salary amount, currency, and effective dates.
- Preserve complete salary history.
- Maintain one active salary for an employee.
- Automatically close the previous active salary when a new salary becomes effective.
- Prevent overlapping salary periods.
- Validate salary amounts and currencies.

### Compensation Analytics
- Overall compensation summary.
- Average, minimum, maximum and median salary.
- Breakdown by country.
- Breakdown by department.
- Breakdown by job title.
- Reporting currency selection.
- Visual dashboard/charts for compensation insights.

### Data and Scale
- Support approximately 10,000 employees.
- Provide deterministic seed data for 10,000 employees.
- Maintain salary history for seeded employees.
- Use server-side pagination and database-side aggregation.

## 4. Out of Scope
- Employee self-service.
- Payroll processing.
- Payslips.
- Tax calculations.
- Benefits management.
- Performance management.
- Recruitment.
- Complex authentication/RBAC beyond the HR Manager persona.
- Live foreign exchange rate integration.
- Bulk Excel import/export.
- Email/notification workflows.
- Full compliance/audit management.

## 5. Product and Engineering Rationale
The product scope was deliberately constrained to the core problem: enabling HR to manage salary data and answer compensation questions. Payroll, tax, benefits, authentication infrastructure, live FX, and other capabilities were excluded because they add substantial complexity without being necessary to demonstrate the core salary-management workflow required by the assessment.

Salary history is modeled as separate records so salary changes do not overwrite historical information. This preserves the full compensation timeline while allowing one current salary to be active at a time. Likewise, analytics are calculated database-side rather than loading all employee data into the application, which keeps the solution viable at the target scale of roughly 10,000 employees and avoids unnecessary in-memory processing.

Reporting currency uses deterministic/static exchange-rate assumptions rather than a live FX integration. The rates are an explicit reporting assumption and are not intended to represent real-time market rates.

## 6. Success Criteria
The solution should allow an HR Manager to:
- Find an employee quickly.
- View current salary and salary history.
- Add a new salary without losing historical data.
- Prevent invalid/overlapping salary periods.
- Filter and paginate approximately 10,000 employees.
- Understand compensation by country, department and job title.
- Change the reporting currency.
- Use the application through the deployed web UI.
