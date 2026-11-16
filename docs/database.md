# Database model

This diagram describes the intended model. V1 implements only movies, genres, and
movie_genres; subsequent migrations add identity, inventory, and rentals.

```mermaid
erDiagram
    roles ||--o{ users : grants
    users ||--o| customers : profile
    movies ||--o{ movie_genres : classified
    genres ||--o{ movie_genres : includes
    movies ||--o{ dvd_copies : owns
    customers ||--o{ rental_orders : places
    rental_orders ||--|{ rental_items : contains
    dvd_copies ||--o{ rental_items : history
    users ||--o{ audit_logs : performs
    users {
        bigint id PK
        bigint role_id FK
        varchar email UK
        varchar password_hash
        boolean enabled
    }
    dvd_copies {
        bigint id PK
        bigint movie_id FK
        varchar barcode UK
        varchar status
    }
    rental_items {
        bigint id PK
        bigint order_id FK
        bigint copy_id FK
        date due_date
        datetime returned_at
        decimal daily_rate_snapshot
        decimal overdue_rate_snapshot
        decimal rental_charge
        decimal overdue_charge
        varchar status
    }
```

Movie title is distinct from its physical copies. Money is DECIMAL/BigDecimal.
Foreign keys retain history. User email and barcode have unique indexes. Rental
items will index due date/status and customer orders will index customer/creation.
Allocation locks the copy row and checks AVAILABLE within the same transaction;
no application-only availability checks. All writers use the same locking protocol.
