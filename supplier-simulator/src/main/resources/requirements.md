Our immediate requirements are:
- PostgreSQL must run locally in Docker.
- Spring Boot must connect using configurable credentials.
- Flyway must manage schema changes.
- Hibernate must not automatically create or modify database tables.
- Database credentials must not be committed to Git.
- The application must start successfully and expose a health endpoint.
- The setup must be reproducible by another developer cloning the repository.