# DVD Rental System

A modular Java/React application for physical DVD rentals with local collection,
customer accounts, staff inspection returns, and inventory management.
Original requirements: [project specification](doc/PROJECT_SPEC.md).

## Technology and architecture

Java 21, Spring Boot 4.1.2, MVC/Security/JPA, MySQL 8.4, Flyway, Maven,
JUnit Jupiter/Mockito/Testcontainers, Actuator, and springdoc. React 19, TypeScript,
Vite, React Router, and Ant Design. Docker Compose runs MySQL, the backend,
and Nginx serving the frontend and proxying the API on one origin.

Boot 4 replaces the specification's 3.x baseline because Spring's 3.5.16 release
announcement recommends 4.x for ongoing OSS support. Java remains at 21.
See [the architecture](docs/architecture.md) for the compatibility decision.

## Start locally with Docker

Prerequisites: Docker Engine with Compose v2 and access to the Docker daemon.

```bash
cp .env.example .env
```

Edit `.env`: provide distinct `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD` values,
and set `DEV_ADMIN_EMAIL` and a `DEV_ADMIN_PASSWORD` of 12+ characters and at
most 72 UTF-8 bytes. Use locally generated secrets; the example has no defaults.

```bash
docker compose -f compose.yml -f compose.dev.yml up --build -d --wait
```

Visit **http://localhost:8084**. Sign in with the development administrator account.
Create genres and movies under **Staff workspace**, then add physical copies in
**Inventory** using the movie ID and a unique barcode. Register a separate customer
account to browse, add DVDs to the basket, and confirm a rental. Staff confirm returns
after inspection. No movies or customer data are silently seeded.

The local override enables HTTP cookies and the `dev` profile. Existing accounts are
not overwritten and their passwords are not reset on subsequent startup.

```bash
docker compose -f compose.yml -f compose.dev.yml logs -f backend
docker compose -f compose.yml -f compose.dev.yml down
```

The named database volume survives `down`. Do not delete it unless you intend to
remove all data. Production setup and HTTPS are described in [deployment](docs/deployment.md).

## Verify

Prerequisites: Java 21, Maven 3.6.3+, Node 22.12+ (24 recommended), and Docker.

```bash
mvn -B -ntp -f backend/pom.xml verify
npm --prefix frontend ci
npm --prefix frontend run build
```

The Maven suite starts a disposable MySQL 8.4 container. Docker is required; database
and concurrency tests fail rather than silently skipping when it is unavailable.
Browser verification against a disposable local stack is documented in [testing](docs/testing.md).

## Documentation

- [Architecture and feature responsibilities](docs/architecture.md)
- [Database and ER model](docs/database.md)
- [API and session/CSRF examples](docs/api.md)
- [Business rules and transitions](docs/business-rules.md)
- [Deployment and recovery](docs/deployment.md)
- [Testing and verification status](docs/testing.md)
- [Learning guide](docs/learning-guide.md)
- [Implementation progress](docs/implementation-plan.md)
