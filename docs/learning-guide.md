# Learning guide

## Follow a request

Start at MovieController → MovieService → MovieRepository → Movie. The controller
handles HTTP inputs, validation and DTO outputs. The service owns application rules
and transaction boundaries. The repository expresses persistence queries; entities
hold state. MovieResponse keeps JPA implementation details out of the API contract.
The page envelope avoids exposing Spring Data's internal serialization format.

Then trace RentalController → RentalService → InventoryService → CopyRepository.
RentalService prices the order, loads the authenticated customer, locks copies in
ascending ID order, and persists every item within one transaction. Any unavailable
copy aborts the whole order, including previous allocations in that transaction.

## Transactions and concurrency

A transaction alone cannot prevent two customers from reading AVAILABLE simultaneously.
A pessimistic write lock serializes allocation on the copy row; availability is checked
after taking the lock. The MySQL unique generated active-copy column provides an
additional invariant at the database layer. Lock ordering reduces deadlock risk.
Integration tests use separate HTTP clients and real database transactions to prove
one physical copy cannot be rented twice. A mock repository cannot prove that property.

Returns lock the order and copy before resolving state. Staff inspection can make a
copy available or move it to maintenance, damaged, or lost. The item preserves the
confirming account. Repeated returns fail rather than silently rewriting history.

## Money and time

BigDecimal and DECIMAL avoid binary floating-point rounding in authoritative charges.
Rental and overdue rates are snapshotted on the item: changing a movie's current price
cannot rewrite historical charges. Active overdue fees are estimates and become final
at staff resolution. A configurable Clock makes business dates deterministic in tests;
Instants record events and LocalDate represents due dates in the business timezone.

## Persistence

Flyway versions schema changes and Hibernate validates mappings. Developers and
production must not depend on Hibernate silently changing the database. Foreign keys,
unique barcodes/emails, money constraints, and indexes complement application rules.
Open Session in View is disabled: DTO mapping finishes inside the service transaction,
so controllers never accidentally trigger lazy database access during serialization.

## Security

Spring Security authenticates email/password against hashed database credentials and
stores authentication in a server-side HTTP session. The standard login filter handles
session fixation protection. HTTP-only cookies prevent direct JavaScript access to the
session secret. CSRF requires a per-session token for unsafe requests; same-origin
routing avoids broad CORS allowances. Fetch a fresh token after login or logout.

Authorization has two levels: roles gate operations, then ownership queries constrain
customer rental reads to the logged-in account. A hidden UI button or route guard
cannot replace either server check. Public registration assigns CUSTOMER unconditionally;
only ADMIN can create STAFF. Development bootstrap is explicit and profile-scoped.

## Professional development questions

- Why is a movie different from a physical copy? Titles describe catalog data, while
  copies carry inventory identity and independent rental lifecycles.
- Why use DTOs? They make input validation and output contracts deliberate and prevent
  password hashes, lazy proxies, and internal relationships from leaking through APIs.
- Why test MySQL instead of H2? SQL constraints, generated columns, isolation, and
  row-lock behavior differ. Allocation correctness is a database-dependent property.
- Why a modular monolith? It preserves feature boundaries and simple transactions
  without distributed failure modes and unnecessary deployment infrastructure.
- Why use Spring Boot's dependency management? Individually recent dependencies can
  be incompatible. Integration verification exposed Spring 7's requirement for JUnit
  APIs beyond version 5, so the project uses the compatible managed Jupiter release.
