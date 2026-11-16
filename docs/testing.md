# Testing

## Backend

```bash
mvn -B -ntp -f backend/pom.xml verify
```

JUnit Jupiter/Mockito cover catalog validation, exact decimal pricing, overdue date bounds,
invalid periods, and inventory lifecycle restrictions. ApplicationIntegrationTest starts
a real MySQL 8.4 Testcontainer and the real HTTP server; it runs Flyway and validates
all JPA mappings. Tests use independent cookie-keeping HTTP clients and a fixed business
clock. They verify registration, password hashing, session fixation protection, CSRF
rotation, logout, role restrictions, catalog/inventory validation, rental ownership,
price snapshots, atomic rollback, inspected returns, lost DVDs, and final overdue fees.

The concurrency test releases two independent customers' HTTP requests through a barrier
and asserts exactly one 201 and one 409, plus exactly one active database rental item.
No in-memory database substitutes for MySQL locking. Tests require Docker daemon access;
they are not silently skipped when Docker is unavailable.

For unit tests only during offline work:

```bash
mvn -f backend/pom.xml -Dtest=MovieServiceTest,PricingServiceTest,DvdCopyTest test
```

## Frontend and browser

```bash
npm --prefix frontend ci
npm --prefix frontend run build
cd frontend
npx playwright install chromium
E2E_ADMIN_EMAIL='your-disposable-dev-admin-email' \
E2E_ADMIN_PASSWORD='your-disposable-dev-admin-password' npm run test:e2e
```

The browser test expects a disposable stack at http://127.0.0.1:8084. Override with
E2E_BASE_URL; E2E_BROWSER_PATH can select an installed Chromium executable. The test
creates a movie and copy as administrator, registers a separate customer, rents through
the basket on a mobile viewport, confirms inspection as staff, and checks closed history.
It asserts no uncaught browser errors and no mobile document overflow, and saves screenshots.
It creates test records; never run it against production/customer data.

CI runs Maven verification, a frontend production build, and the browser workflow
against a dedicated Compose project with disposable CI-only credentials and a separate volume.

## Verification record

The initial foundation on 2026-11-15 passed three catalog unit tests and executable-jar
packaging. The expanded implementation was verified on 2026-11-16 as recorded below.

### Expanded MVP results — 2026-11-16

| Check | Actual result |
|---|---|
| Maven verify / executable jar | Passed |
| Backend unit + MySQL integration suite | 14 passed; 0 failures, errors, or skips |
| Concurrent HTTP allocation | Exactly one 201 and one 409; one active item in MySQL |
| Flyway and JPA mapping validation | All four migrations applied to fresh MySQL 8.4; application started |
| TypeScript and Vite production build | Passed |
| Full Docker Compose build/startup | Backend, MySQL, and Nginx all healthy |
| Chromium Playwright journey through Nginx | 1 passed; admin setup → mobile customer registration/rental → inspected return → closed history |
| Mobile layout / uncaught browser exceptions | No document overflow; no uncaught exceptions |
| Git whitespace check | Passed |

The verified stack used a separate `dvd-rental-verify` Compose project and loopback
port 18080 with generated disposable credentials. Browser screenshots are generated
under frontend/test-results (ignored by Git). Verification containers were stopped
at handoff; the test database volume was retained.

All 14 functional MVP acceptance criteria have local verification evidence. The
specification's framework versions were adjusted for compatibility: supported Spring
Boot 4.1.2 and its managed JUnit Jupiter release, with Java 21 unchanged.

Known limits: the Vite build reports a large JavaScript bundle (about 1.15 MB,
370 kB gzip); route/vendor splitting is a future performance improvement. GitHub
Actions jobs were added but have not been executed on GitHub in this session. A
production HTTPS server, domain, certificates, backups, and production accounts are
operator configuration rather than an executed remote deployment. No production
deployment was performed.
