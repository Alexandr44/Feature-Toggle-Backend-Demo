# Feature Toggle Backend Demo

A lightweight **Feature Flag (Feature Toggle) management service** built with **Kotlin + Spring Boot**.  
It allows you to dynamically enable or disable features without redeploying applications — similar to tools like **LaunchDarkly** or **Unleash**, but implemented as a compact demo backend.

---

## Main Features

- **Authentication & Authorization** — JWT-based security with role-based access (`ADMIN`, `USER`)
- **Feature Management** — Create, update, delete, and list feature flags via REST API
- **Audit Logging** — Tracks changes to entities with before/after JSON snapshots using AOP
- **Database Versioning** — Managed with Liquibase migrations
- **Comprehensive Testing** — Integration tests using MockMvc, JUnit 5, and H2
- **DTO Layer** — Clean separation between API models and persistence entities
- **Extensible Design** — Simple architecture that can be extended into a production service

---

## 🧱 Tech Stack

| Purpose | Technology |
|----------|-------------|
| Language | Kotlin |
| Framework | Spring Boot |
| Web | Spring MVC (REST Controllers) |
| Persistence | Spring Data JPA |
| Database | PostgreSQL (prod), H2 (tests) |
| DB Migrations | Liquibase |
| Security | Spring Security + JWT |
| Serialization | Jackson (`ObjectMapper`) |
| Testing | JUnit 5, MockMvc, Spring Boot Test |
| Build Tool | Gradle Kotlin DSL |

---

## Security Overview

Uses Spring Security with JWT Authentication

Custom JwtAuthFilter parses tokens from the Authorization header

Role-based access (@PreAuthorize("hasRole('ADMIN')")) for administrative actions

Passwords are securely encoded using BCryptPasswordEncoder

## Audit Logging

Every entity implementing AuditableEntity is automatically tracked for changes.
Audit records are persisted in audit_logs with:

Entity type and ID

Action (CREATE, UPDATE, DELETE)

Old and new JSON snapshots

Timestamp and username
