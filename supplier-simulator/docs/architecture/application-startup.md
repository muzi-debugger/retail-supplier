
sequenceDiagram
    autonumber
    actor Developer
    participant Docker as Docker Compose
    participant DB as PostgreSQL
    participant App as Spring Boot
    participant Flyway
    participant JPA as Hibernate

    Developer->>Docker: docker compose up -d
    Docker->>DB: Start PostgreSQL
    DB-->>Docker: Healthy

    Developer->>App: ./mvnw spring-boot:run
    App->>DB: Open JDBC connection
    DB-->>App: Connection established

    App->>Flyway: Initialize migrations
    Flyway->>DB: Inspect flyway_schema_history
    Flyway->>DB: Apply pending migrations
    DB-->>Flyway: Migration successful

    App->>JPA: Initialize EntityManagerFactory
    JPA->>DB: Validate mapped schema
    DB-->>JPA: Validation successful

    App-->>Developer: Application started
