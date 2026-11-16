# Verification

Run `mvn -f backend/pom.xml test` for catalog service unit tests. They check page
bounds, sort allowlists, empty search results, and missing movie status. They do
not connect to MySQL or prove the application starts.

Before completing the foundation, add MySQL Testcontainers migration/repository
and HTTP/security integration coverage. Before completing rentals, prove concurrent
allocation using separate transactions against real MySQL 8.4. Never substitute H2
for the database locking test. CI must have Docker access and run these checks.

Frontend build and end-to-end tests await frontend implementation.

## Local environment findings

Java 21, Maven 3.8.7, Node 26, and the Docker CLI are installed. Docker daemon
access fails with permission denied on `/var/run/docker.sock`, including outside
the sandbox. MySQL integration and application startup have not been verified.
Maven Central requires approved access outside the network sandbox.

## Executed foundation checks

On 2026-11-15, the final configuration passed:

```bash
mvn -B -ntp -f backend/pom.xml -Dmaven.repo.local=/tmp/dvd-rental-m2 package
```

Result: BUILD SUCCESS; three JUnit 5 tests passed, zero failures/errors/skips.
The executable jar was produced at `backend/target/dvd-rental-0.1.0-SNAPSHOT.jar`.
This verifies compilation, unit tests, and packaging, not runtime/database behavior.
