# DVD Rental System

A physical DVD rental application for customer collection and staff-confirmed returns.
The requirements are preserved in [the project specification](doc/PROJECT_SPEC.md).

## Current implementation

Backend foundation: Java 21, Spring Boot 4.1.2, Spring MVC, JPA, Flyway, MySQL 8.4,
Spring Security, Actuator, and springdoc 3.2.0. Public catalog reads support bounded
pagination, title search, and an allowlist of sort fields. Every other route is denied
until database-backed identity is implemented. No default user is created.

This is an incremental implementation; registration, rental workflows, React UI, and
production deployment are not implemented yet. See [the plan](docs/implementation-plan.md).

## Run the backend

Prerequisites: Java 21, Maven 3.6.3+, and an accessible MySQL 8.4 database.
Create a database named `dvd_rental` and a dedicated user with schema migration privileges.
Supply your own password; do not commit secrets.

```bash
export DB_URL=jdbc:mysql://localhost:3306/dvd_rental
export DB_USER=dvd_rental
export DB_PASSWORD='your-local-database-password'
# Only for local HTTP development; production must use HTTPS and secure cookies.
export COOKIE_SECURE=false
mvn -f backend/pom.xml spring-boot:run
```

Flyway applies migrations on startup; Hibernate validates the schema.
No sample movies are inserted into production data.

```bash
curl 'http://localhost:8080/api/v1/movies?query=Alien&page=0&size=20&sort=title'
curl http://localhost:8080/actuator/health
mvn -f backend/pom.xml test
mvn -f backend/pom.xml package
```

Swagger endpoints are disabled by default. OpenAPI dependency is installed; access
rules for development documentation will be added with identity configuration.
