# Executive Summary: CrediFlow Loan Management System

**CrediFlow** is a production-level, SaaS-ready loan management system built with Spring Boot 3.4.4. It handles the complete lifecycle of a loan, from application to repayment, with built-in modules for risk assessment, auditing, Excel export/import, and robust analytics.

---

## 1. Technical Stack

- **Core Framework**: Spring Boot 3.4.4 (Java 21)
- **Security**: Spring Security 6 (JWT & OAuth2 - Google/GitHub)
- **Database**: MySQL 8.0 (ORM: Spring Data JPA + Hibernate)
- **Reporting**: iText 8 (PDF), Apache POI (Excel), OpenCSV
- **Rate Limiting**: Bucket4j (10 requests/min per IP/API)
- **API Documentation**: SpringDoc OpenAPI 2.1 (Swagger UI)
- **Utilities**: Lombok, MapStruct (DTO Mapping), Spring Retry

---

## 2. Core Architecture & Workflow

The project follows a **Modular Monolith** architecture with a clear separation of concerns:

1.  **Authentication & Security Layer**: Validates requests, issues tokens.
2.  **Controller Layer**: Handles HTTP requests (`@RestController`).
3.  **Service Layer**: Business logic (e.g., loan calculations, risk scoring).
4.  **Repository Layer**: Database interactions (Spring Data JPA).
5.  **Utility & Integration Layer**: PDF/Excel generation, Email notifications.

### Typical Workflow (Loan Lifecycle)
1.  **Onboarding**: User registers or logs in via OAuth2.
2.  **Loan Application**: Borrower submits a loan request.
3.  **Risk Assessment**: System runs a risk score calculation based on user data.
4.  **Orchestration**: `LoanPlatformOrchestrator` manages the state transitions.
5.  **Repayment**: Payments are logged, and status is updated automatically.
6.  **Auditing**: Every critical action is logged via Spring AOP (`AuditAspect`).

---

## 3. Detailed Technicalities & Services

### 3.1 Security & Authentication
Implemented using Spring Security and stateless JWT tokens.

-   **Location**: `com.loanmanagement.security` & `com.loanmanagement.auth`
-   **Security Configuration**: Standard Spring Security filters with custom `JwtAuthenticationFilter`.
-   **OAuth2**: Integrated with Google and GitHub. `OAuth2SuccessHandler` handles JWT generation after successful external login.
-   **Code Reference**:
    -   `JwtTokenProvider.java`: Token generation and validation logic.
    -   `JwtAuthenticationFilter.java`: Intercepts requests to validate tokens.
    -   `CustomUserDetailsService.java`: Loads user specific data from the database.

### 3.2 Refresh Token Feature
Provides a secure way to maintain user sessions without long-lived access tokens.

-   **Location**: `com.loanmanagement.auth`
-   **Mechanism**: A random UUID-based refresh token is stored in the database and linked to the user.
-   **Code Reference**:
    -   `RefreshToken.java`: Entity representing the refresh token.
    -   `RefreshTokenService.java`: Logic for generating, validating, and expiring refresh tokens.
    -   `AuthService.java (refreshToken)`: Validates the refresh token and issues a new JWT.

### 3.3 Loan Management & Orchestration
The heart of the system, managing loan states and platform-level operations.

-   **Location**: `com.loanmanagement.loan`
-   **Loan Platform**: `com.loanmanagement.loanplatform`
-   **Key Component**: `LoanPlatformOrchestrator.java`
-   **Features**: Application submission, approval workflows, status tracking.

### 3.4 Analytics & Reporting
Detailed metrics for administrators to track portfolio performance.

-   **Location**: `com.loanmanagement.analytics` & `com.loanmanagement.report`
-   **Features**:
    -   Financial metrics (Total lending, active loans, repayment rates).
    -   PDF export of statements and reports using iText.
    -   Custom SQL queries to aggregate performance data.

### 3.5 Excel & CSV Processing (Bulk Operations)
Allows administrators to import or export large datasets easily.

-   **Location**: `com.loanmanagement.excel`
-   **Features**:
    -   Bulk export of user and loan data to Excel/CSV.
    -   Multi-sheet Excel generation for complex reports.
-   **Code Reference**: `ExcelService.java` (Logic for generating sheets and parsing uploads).

---

## 4. Feature-to-Code Mapping (Quick Reference)

| Service / Feature | Package Path | Key Files / Classes |
| :--- | :--- | :--- |
| **Authentication** | `com.loanmanagement.auth` | `AuthController`, `AuthService`, `RefreshTokenService` |
| **Security Config** | `com.loanmanagement.security` | `JwtAuthenticationFilter`, `JwtTokenProvider`, `OAuth2SuccessHandler` |
| **Loan Logic** | `com.loanmanagement.loan` | `LoanController`, `LoanService`, `LoanRepository` |
| **Orchestration** | `com.loanmanagement.loanplatform`| `LoanPlatformOrchestrator` |
| **Audit Logs** | `com.loanmanagement.audit` | `AuditAspect`, `AuditLog`, `AuditService` |
| **Email Service** | `com.loanmanagement.email` | `EmailService`, `EmailTemplateService` |
| **Excel Service** | `com.loanmanagement.excel` | `ExcelController`, `ExcelService` |
| **PDF Reporting** | `com.loanmanagement.report` | `ReportController`, `ReportService` |
| **Risk Scoring** | `com.loanmanagement.risk` | `RiskService`, `RiskController` |
| **Admin Controls** | `com.loanmanagement.admin` | `AdminController`, `AdminService` |
| **Analytics Dashboard**| `com.loanmanagement.analytics` | `AnalyticsController`, `AnalyticsService` |

---

## 5. Security Summary
-   **State Management**: Stateless (JWT-based).
-   **Token Expiration**: Access token (15-60 mins), Refresh token (7 days).
-   **Encryption**: BCrypt for password hashing.
-   **Oversight**: Comprehensive auditing on all modification endpoints.
-   **Rate Limiting**: Protected against brute force and DDoS via `Bucket4j`.

---

## 6. How to Run & Build
1.  **Prerequisites**: JDK 21, MySQL 8.
2.  **Configuration**: Update `src/main/resources/application.yml` with database credentials and Mail settings.
3.  **Build**: `mvn clean install`
4.  **Run**: `java -jar target/loanmanagementsystem-1.0.0.jar`
5.  **API Docs**: Access `http://localhost:8080/swagger-ui.html`
