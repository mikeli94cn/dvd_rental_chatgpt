# Deployment status

Production packaging and Compose are a later milestone. The backend currently
requires an externally provisioned MySQL 8.4 schema and environment variables
shown in README. Maven packaging produces an executable Spring Boot jar.

Production will expose Nginx only, keep MySQL on a private Docker network, persist
MySQL data, terminate TLS, set secure session cookies, and restrict health/details
and development documentation. Flyway runs at application startup. Back up the
database before migrations; never reset production volumes to apply a schema change.
