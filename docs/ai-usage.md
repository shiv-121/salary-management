# ACME Employee Salary Management — AI Usage

## 1. Purpose

AI tools were used as development assistants throughout the project to accelerate implementation, review, documentation, and problem solving.

The primary tools used were:
- GitHub Copilot
- ChatGPT

AI assistance was treated as a productivity tool rather than an autonomous decision-maker. Generated code and suggestions were reviewed, tested, and adapted before being incorporated into the project.

## 2. How AI Was Used

### Implementation Assistance

GitHub Copilot was used to assist with:
- Spring Boot service and repository implementations
- DTOs and validation
- JPA specifications for employee search and filtering
- salary lifecycle logic
- compensation analytics queries
- unit tests
- Angular components and services
- configuration and deployment-related code

Generated suggestions were reviewed against the requirements and modified where necessary.

### Test Development

AI assistance was used to identify important business-rule scenarios and generate initial test structures.

Examples included:
- salary overlap validation
- closing the previous active salary
- duplicate salary detection
- employee duplicate code/email validation
- pagination and page-size validation
- search and filtering behavior
- compensation analytics and currency conversion

Tests were executed locally and failing or incorrect suggestions were corrected before being incorporated.

### Documentation

ChatGPT was used to help structure and refine:
- requirements
- architecture documentation
- design decisions
- testing strategy
- performance considerations
- AI usage documentation

The final documentation reflects the actual implementation rather than blindly reproducing AI-generated content.

### Problem Solving and Review

ChatGPT was also used as a review and reasoning assistant for:
- evaluating architecture choices
- identifying edge cases
- reviewing validation rules
- improving API design
- reviewing test coverage areas
- troubleshooting deployment issues
- reviewing assessment completeness

## 3. Representative Prompts

Examples of prompts used during development include:

> "How should salary history be modeled so that historical salaries are preserved and only one salary is active?"

> "What validation rules should be applied when creating a new salary record with effective dates?"

> "How can I implement server-side filtering and pagination for approximately 10,000 employees using Spring Data JPA Specifications?"

> "What unit tests should cover the salary lifecycle and overlapping salary periods?"

> "Review this architecture for a Java Spring Boot and Angular salary management assessment and identify unnecessary complexity."

> "Review the project against the assessment requirements and identify missing engineering artifacts."

## 4. Human Review and Engineering Responsibility

AI-generated code was not accepted without review.

The implementation was validated by:
- running the backend test suite
- reviewing generated code and modifying it where required
- testing API behavior through HTTP requests
- verifying database behavior
- testing the deployed application
- reviewing validation and edge cases
- checking that implementation decisions matched the requirements

Final responsibility for architecture, business rules, implementation choices, and acceptance of generated code remained with the developer.

## 5. Engineering Judgment

AI suggestions were evaluated based on:
- correctness
- simplicity
- maintainability
- alignment with the requirements
- performance implications
- testability
- suitability for the assessment scope

Complexity suggested by AI was intentionally avoided when it did not provide meaningful value. For example, authentication infrastructure, microservices, live FX integrations, payroll functionality, and other features outside the assessment scope were not introduced.

## 6. Key Principle

AI was used to accelerate engineering work, not to replace engineering judgment.

The resulting implementation was reviewed, tested, and adapted to fit the requirements, domain rules, and deployment constraints of the ACME Salary Management system.
