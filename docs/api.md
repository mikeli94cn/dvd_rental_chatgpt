# API

All application routes use `/api/v1`. Collections use `content`, `page`, `size`,
`totalElements`, and `totalPages`. Pages are zero-based, size is 1–100. Stable ID
ordering is used for operational collections. Catalog sorting accepts title,
releaseYear, or dailyRate with an ID tie-break; filtering uses the title `query`.

| Methods | Route | Access |
|---|---|---|
| GET | /auth/csrf | Public, creates a session token |
| POST | /auth/register | Public, customer registration only |
| POST | /auth/login | Public, form email/password |
| POST | /auth/logout | Session, CSRF required |
| GET | /auth/me | Authenticated account DTO |
| GET | /movies, /movies/{id}, /genres | Public catalog |
| GET | /movies/{id}/availability | Public available copy IDs |
| GET, PUT | /customers/me | Customer's own profile |
| GET, POST | /rentals | Customer's own history / new rental |
| GET | /rentals/{id} | Customer ownership required |
| GET | /staff/customers, /staff/copies, /staff/rentals | Staff/admin paginated collections |
| PUT | /staff/customers/{id} | Staff/admin profile edit |
| POST | /staff/rentals/{orderId}/items/{itemId}/return | Staff/admin inspection confirmation |
| GET | /staff/reports/dashboard, /staff/reports/overdue | Staff/admin operational reports |
| POST | /admin/staff | Admin creates STAFF account |
| POST | /staff/movies, /staff/genres, /staff/copies | Staff/admin creates catalog/inventory |
| PUT, DELETE | /staff/movies/{id}, /staff/genres/{id} | Staff/admin updates/deletes unreferenced records |
| PUT | /staff/copies/{id}/status | Staff/admin guarded inventory transition |
| GET | /admin/openapi | Admin, only when API_DOCS_ENABLED=true |

GET `/actuator/health` exposes health without internal details on the backend;
Nginx exposes the same response at `/health`. Other actuator endpoints are disabled.
When enabled, Swagger UI is `/swagger-ui/index.html` and requires an ADMIN session.

## Session and CSRF workflow

1. GET `/api/v1/auth/csrf`, retaining the HTTP session cookie. Response contains
   `headerName` and `token`. Send that header/token on every POST/PUT/DELETE.
2. Register with JSON `{email,password,name}`. Registration returns 201 but does
   not log in. Passwords require 12+ characters and at most 72 UTF-8 bytes.
3. Login with `application/x-www-form-urlencoded` fields `email` and `password`.
   Success is 204, failure 401. Login rotates the session ID and invalidates the old
   CSRF token. Fetch a fresh token after login and after logout.
4. GET `/auth/me` to load id/email/role. Session cookies are HTTP-only. No bearer
   tokens are used or persisted in browser storage.
5. Logout by POST with a valid CSRF header. Success is 204 and invalidates the session.

Create rental JSON: `{ "copyIds": [1, 2], "days": 3 }`.
Return JSON: `{ "inspectionResult": "AVAILABLE" }` (also MAINTENANCE, DAMAGED, LOST).
Create movie JSON: `{ "title": "Alien", "synopsis": "...", "releaseYear": 1979,
"dailyRate": 2.50, "genreIds": [] }`.
Create copy JSON: `{ "movieId": 1, "barcode": "DVD-0001" }`.

400 indicates invalid input, 401 missing/invalid authentication, 403 missing role or
CSRF token, 404 a missing resource or another customer's order, and 409 a conflicting
allocation/state/unique constraint. Error bodies use RFC 9457 Problem Details without
returning raw SQL or password fields.
