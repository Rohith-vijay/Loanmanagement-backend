# CrediFlow Loan Management Backend

A production‑ready, SaaS‑grade **Loan Management System** built with **Spring Boot 3.4.4** and **Java 21**. This backend powers the full loan lifecycle—from onboarding and application to risk scoring, approvals, repayments, analytics, and reporting—with strong security, auditability, and export tools.

---

## ✨ Highlights

- **End‑to‑end loan lifecycle**: application → approval → repayment → reporting
- **Secure by default**: JWT + OAuth2 (Google/GitHub) with refresh tokens
- **Audit & compliance ready**: AOP‑based audit logging
- **Analytics & reporting**: PDF/Excel/CSV exports
- **Rate limiting**: Bucket4j (per‑IP/API throttling)
- **Modern stack**: Spring Boot 3.4.4, Java 21, MySQL 8

---

## 🧱 Tech Stack

- **Framework**: Spring Boot 3.4.4
- **Language**: Java 21
- **Security**: Spring Security 6, JWT, OAuth2
- **Database**: MySQL 8 + Spring Data JPA
- **Docs**: SpringDoc OpenAPI (Swagger UI)
- **Reporting**: iText (PDF), Apache POI (Excel), OpenCSV
- **Utilities**: Lombok, MapStruct, Spring Retry
- **Rate Limiting**: Bucket4j

---

## 🧭 Architecture Overview

Modular monolith with clear separation of concerns:

1. **Authentication & Security** – token validation, OAuth2 login
2. **Controller Layer** – REST endpoints (`@RestController`)
3. **Service Layer** – business logic (risk scoring, approvals, repayments)
4. **Repository Layer** – JPA data access
5. **Integration Layer** – email, PDF/Excel exports, analytics

---

## ✅ Key Features

- **Authentication & Authorization**
  - JWT access tokens + refresh tokens
  - OAuth2 login (Google & GitHub)

- **Loan Management**
  - Application submission & lifecycle tracking
  - Orchestration with state transitions

- **Risk Scoring & Analytics**
  - Risk evaluation and performance insights

- **Reporting**
  - Generate PDF/Excel/CSV reports

- **Audit Logging**
  - Track critical actions via AOP

- **Rate Limiting**
  - Built‑in API protection

---

## 📁 Package Map (Quick Reference)

| Feature | Package | Key Components |
|---|---|---|
| Auth & Tokens | `com.loanmanagement.auth` | `AuthController`, `AuthService`, `RefreshTokenService` |
| Security | `com.loanmanagement.security` | `JwtAuthenticationFilter`, `JwtTokenProvider` |
| Loan Core | `com.loanmanagement.loan` | `LoanController`, `LoanService` |
| Orchestration | `com.loanmanagement.loanplatform` | `LoanPlatformOrchestrator` |
| Audit | `com.loanmanagement.audit` | `AuditAspect`, `AuditService` |
| Reporting | `com.loanmanagement.report` | `ReportController`, `ReportService` |
| Excel/CSV | `com.loanmanagement.excel` | `ExcelController`, `ExcelService` |
| Analytics | `com.loanmanagement.analytics` | `AnalyticsController`, `AnalyticsService` |

---

## 🚀 Getting Started

### Prerequisites

- **Java**: 21
- **Maven**: 3.9+
- **MySQL**: 8.0

### Configuration

Update `src/main/resources/application.yml` with:

- Database credentials
- JWT secrets
- OAuth2 client IDs/secrets
- Mail server settings

### Build

```bash
mvn clean install
```

### Run

```bash
java -jar target/loanmanagementsystem-1.0.0.jar
```

### API Docs (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Testing

```bash
mvn test
```

---

## 🔐 Security Notes

- Stateless JWT authentication
- Access token expiry: **15–60 mins**
- Refresh tokens stored securely in DB
- Password hashing with **BCrypt**
- Rate‑limited endpoints via **Bucket4j**

---

## 🛠️ Useful Scripts

The repository includes `test_api.ps1` for quick API testing from PowerShell.

---

## 📜 License

No license specified yet. Add a `LICENSE` file if you plan to open‑source this project.

---

## 🙌 Acknowledgements

Built with Spring Boot and modern Java tooling to provide a robust, enterprise‑grade loan platform backend.
