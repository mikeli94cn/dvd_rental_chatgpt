# API status

Implemented public routes:

| Method | Route | Behavior |
|---|---|---|
| GET | `/api/v1/movies` | Search titles; query, page (0+), size (1–100), sort |
| GET | `/api/v1/movies/{id}` | Movie DTO or 404 |
| GET | `/actuator/health` | Health without internal details |

Sort fields: title (default), releaseYear, dailyRate. Ascending with ID tie-break.
Collection envelope: content, page, size, totalElements, totalPages.
Invalid bounds/sort/search length produce 400 Problem Details.

Next routes: `/auth/register`, `/auth/login`, `/auth/logout`, `/auth/csrf`,
`/auth/me`, `/genres`, `/admin/movies`, `/admin/copies`, `/rentals`,
`/rentals/{id}`, `/staff/returns`, `/staff/reports`. All are under `/api/v1`.
These are planned contracts, not available endpoints.
