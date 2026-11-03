---
config:
  theme: mc
---
flowchart TB
    A["Developer creates feature branch"] --> B["Push changes to GitHub"]
    B --> C["Open Pull Request"]
    C --> D["GitHub Actions triggered"]
    D --> E["Checkout source code"]
    E --> F["Configure JDK 21"]
    F --> G["Restore Maven cache"]
    G --> H["Run Maven verify"]
    H --> I["JUnit 5 executes tests"]
    I --> J["Testcontainers starts PostgreSQL"]
    J --> K["Flyway applies migrations"]
    K --> L["Spring Boot initialises JPA"]
    L --> M{"Tests successful?"}
    M -- Yes --> N["CI check passes"]
    N --> P["Review and merge into main"]
    M -- No --> O["CI check fails"]