# Implementation plan

## Repository inspection

The repository initially contained only `doc/PROJECT_SPEC.md`, with no code, commits,
or repository-specific agent instructions. Preserve that original specification.

## Decisions and assumptions

- Modular monolith, Java 21, MySQL 8.4, same-origin React application behind Nginx.
- Spring Boot 4.1.2 rather than 3.x: Spring's 3.5.16 release announcement directs
  users requiring OSS support to 4.0/4.1. Java 21 remains supported by Boot 4.1.
  Boot manages dependency versions; springdoc 3.2.0 is its Boot 4 compatible line.
- Sources: https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/
  https://docs.spring.io/spring-boot/system-requirements.html and https://springdoc.org/.
- One physical location, GBP initially, no shipping or payment gateway.
- Calendar dates in Europe/London; a due date is inclusive, overdue days start the
  following date. Daily rental periods are 1–30 days. Confirm policy before production.
- One role per account initially; staff are not automatically customer accounts.
- No automatic Git commits, deployments, or production account creation.

## Milestones

1. **Foundation (in progress):** project, architecture/model documentation, public
   catalog, Flyway, secure defaults, error handling, and verified build.
2. **Identity and administration:** database accounts and roles, customer registration,
   session login/logout, CSRF token endpoint, ownership checks, development bootstrap,
   movie/genre/inventory administration; MySQL integration and security tests.
3. **Rentals:** transactional allocation with pessimistic locks in ID order, snapshot
   prices, customer history, staff returns/inspection, overdue pricing, reports.
   Prove double allocation is impossible using concurrent real MySQL transactions.
4. **Frontend:** React/TypeScript/Vite/Ant Design, customer and staff workflows,
   same-origin session integration and visible loading/error states.
5. **Release:** integration suite, frontend build, CI, Docker/Nginx/Compose, operational
   docs, end-to-end acceptance checks.

Each milestone must record actual verification and remaining gaps. Unit tests alone
cannot establish MySQL compatibility, authentication safety, or concurrency correctness.

## Foundation handoff

Catalog implementation, schema migration, security defaults, and documentation are
in place. Final Maven packaging and three JUnit 5 tests passed. Foundation remains
open for real MySQL migration, HTTP, and security integration checks; host Docker
socket access is denied. Next implementation work is database-backed identity and
administrator catalog/inventory writes, followed by the rental workflow.
