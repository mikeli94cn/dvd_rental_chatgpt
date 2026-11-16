# Deployment

## Local Docker setup

Follow README: copy `.env.example`, supply secrets, then use both `compose.yml` and
`compose.dev.yml`. Only the development override activates the administrator bootstrap
and disables secure cookies for local HTTP. No accounts are created without explicit
DEV_ADMIN_EMAIL/DEV_ADMIN_PASSWORD. Existing administrator passwords are not changed.
An existing non-administrator with that email makes bootstrap fail safely.

## Linux production

Docker Compose builds both applications in stages. The runtime backend uses Java 21
under an unprivileged account. Nginx serves static files and proxies `/api/` to Java;
MySQL has no published host port and resides on a private internal Docker network.
A persistent `mysql_data` volume stores the database. Memory limits are declared for
a small server (roughly 1.5 GB across services; allow additional OS/build headroom).

Run `docker compose up --build -d --wait` without the development override.
Keep COOKIE_SECURE=true. The application binds to 127.0.0.1:8084 by default; configure
the server's HTTPS reverse proxy to forward to it and set up a valid TLS certificate.
Only HTTPS should be publicly reachable. Do not expose MySQL, backend, or the local
bootstrap profile to the public internet. APP_BIND and APP_PORT change the listener.
The repository does not provision TLS certificates or cloud infrastructure.

For a fresh installation, perform the explicit development bootstrap only on a private
local instance, then stop it and start the base production configuration. Staff accounts
are subsequently created by the administrator from the application. Keep .env outside
version control with mode 600, use distinct database/application administrator secrets,
and remove bootstrap environment variables when finished.

MySQL and backend health checks gate startup. `/health` returns 200 only when Spring
Actuator reports the backend and database healthy. Flyway applies migrations at Java
startup; Hibernate validates mappings. Migration failure prevents healthy startup.
Never use Hibernate schema creation or delete volumes to fix a migration error.

## Backup and update

Before an update, take and verify a database backup using MySQL's supported dump tools
and a protected credentials file. Back up the deployment environment separately.
Keep the previous application image and restore a backup if a destructive schema
change prevents rollback. Then build/start the new version and inspect service health:

```bash
docker compose up --build -d --wait
docker compose ps
docker compose logs --tail=100 backend
```

`docker compose down` retains the data volume. `down -v` destroys data; never use it
against a live deployment. Integration tests use their own disposable database instead.
OpenAPI/Swagger are disabled by default and require ADMIN when explicitly enabled.

## Development without application containers

Provide DB_URL, DB_USER, DB_PASSWORD for a private MySQL 8.4 instance. Set
SPRING_PROFILES_ACTIVE=dev, DEV_ADMIN_EMAIL, DEV_ADMIN_PASSWORD, and COOKIE_SECURE=false
only for local HTTP. Run `mvn -f backend/pom.xml spring-boot:run` (port 8084), then
`npm --prefix frontend run dev` (Vite proxies /api to Java). Do not expose Vite publicly.

## Port configuration

`APP_PORT` changes the browser-facing Nginx port on the host (8084 by default).
The Java server is explicitly configured to listen on 8084 inside its container. Nginx proxies and the backend
health check must use that internal port. Dockerfile `EXPOSE` documents a port;
it does not configure Spring Boot's listening port. To choose another public port,
change only `APP_PORT` in `.env`, then run Compose again.

Only Nginx publishes a host port: 127.0.0.1:8084 by default. Its container-local
port 80 is isolated by Docker and does not reserve host port 80. MySQL and Java
publish no host ports. Host ports 80 and 8080–8083 remain available to other apps.
