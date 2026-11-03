
flowchart TD
    A["Spring Boot Startup"]
    B["Flyway Initializes"]
    C{"Schema History Exists?"}
    D["Initialize Schema History"]
    E["Read Applied Migrations"]
    F["Scan db/migration Directory"]
    G{"Pending Migrations?"}
    H["Validate Migration Checksums"]
    I["Execute Pending SQL"]
    J{"Migration Successful?"}
    K["Record Migration in History"]
    L["Continue Application Startup"]
    M["Fail Application Startup"]

    A --> B
    B --> C
    C -->|"No"| D
    C -->|"Yes"| E
    D --> E
    E --> F
    F --> G
    G -->|"No"| L
    G -->|"Yes"| H
    H --> I
    I --> J
    J -->|"Yes"| K
    J -->|"No"| M
    K --> L
