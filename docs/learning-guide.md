# Learning guide

Start at MovieController → MovieService → MovieRepository → Movie. The controller
owns HTTP concerns; the service handles validation and transaction boundaries;
the repository handles persistence. MovieResponse prevents persistence details
from becoming API contracts. The page DTO avoids binding clients to JPA internals.

`@Transactional(readOnly = true)` marks catalog reads. Rental writes will need a
single transaction covering locked copy allocation and order creation. A transaction
alone does not prevent two readers from allocating the same copy: locks and state
checks must share its boundary. This is a common interview concurrency question.

Flyway versions schema changes and Hibernate validates mappings. Neither developer
machines nor production should depend on Hibernate silently changing the database.
Security rules currently deny unimplemented operations. UI route protection will
complement server-side authorization, never replace it.
