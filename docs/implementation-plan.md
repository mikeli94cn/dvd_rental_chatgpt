# Implementation progress

The original repository contained only doc/PROJECT_SPEC.md. The initial foundation
introduced a public catalog, schema migration, security defaults, and architecture
planning, with three passing tests and jar packaging. Docker access was initially
blocked and became available outside the execution sandbox on 2026-11-16.

## Current implementation

- Modular Java 21 backend with all eight feature packages.
- Database accounts/roles/customer profiles, secure session login/logout and CSRF.
- Explicit dev-profile administrator bootstrap and admin-only staff creation.
- Movie/genre CRUD, physical inventory, availability, and guarded state changes.
- Transactional multi-copy rentals, price snapshots, customer ownership, staff returns,
  loss/damage/maintenance handling, overdue charges and operational reports.
- Real MySQL Testcontainers integration coverage including concurrent HTTP allocation.
- React/TypeScript/Vite/Ant Design customer and administrative pages using real APIs.
- Dockerfiles, private MySQL network/volume, Nginx, health checks, and GitHub Actions.
- Architecture, API, database, business-rule, deployment, testing and learning docs.

## Assumptions

One physical collection/return counter; collection occurs when a rental is created.
No shipping, payment capture, replacement fees, or reservation expiry. GBP pricing,
Europe/London calendar dates, 1–30 day periods, maximum 10 copies per order. One role
per account; staff use a separate customer account if they want to rent DVDs. Customer
self-registration can never grant privileged roles. Bootstrap never resets accounts.

Spring Boot 4.1.2 preserves Java 21 while replacing the now-ended OSS Boot 3.5 baseline.
Catalog and inventory administration, returns, reports, and customer contact updates
are available to STAFF and ADMIN. Only ADMIN can create privileged staff accounts.

## Verification and handoff

Local verification completed: 14 backend tests, executable jar, real MySQL migrations
and concurrency, frontend type-check/build, healthy three-service Compose startup,
and the complete Chromium customer/staff browser journey. See docs/testing.md for
results, environment, and actual limitations. All functional MVP acceptance criteria
have local verification evidence. GitHub Actions is configured but has not run on
GitHub, and no production deployment or Git commit was performed.

Potential follow-up work: reduce the frontend bundle through route/vendor splitting;
configure the target server's TLS, backups, and production account lifecycle. These
do not block the verified local collection/return MVP.
