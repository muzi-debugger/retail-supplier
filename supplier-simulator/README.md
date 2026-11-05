
# retail-supplier

Spring Boot 4 / Java 21 supplier simulator. PostgreSQL 17 runs in Docker; the schema is owned by Flyway (`src/main/resources/db/migration`) and Hibernate runs with `ddl-auto: validate`.

The Supplier Simulator is one of three independently deployable applications in the **Sneaker Street distributed commerce platform**:

- **Supplier Simulator:** Product catalogue, sellable variants, inventory and retailer order fulfilment.
- **Retail Application:** Customer accounts, product browsing, checkout, orders and accounting.
- **StreetLogistics:** Delivery management, tracking and proof of delivery.

The Supplier Simulator currently implements the product catalogue. Inventory management, retailer ordering, security and service-to-service integration are planned for subsequent iterations.

## Prerequisites

- Java 21
- Docker (for the local PostgreSQL container)

## Running locally

```bash
cd supplier-simulator
docker compose up -d postgres
./mvnw spring-boot:run
```

Connection settings come from `supplier-simulator/.env` (git-ignored). It is read by **both** Docker Compose and Spring Boot, so there is no need to export anything in your shell:

```dotenv
POSTGRES_SUPPLIER_DB=sneaker_street_supplier
POSTGRES_SUPPLIER_USER=supplier_app
POSTGRES_SUPPLIER_PASSWORD=...
```

## Tests

```bash
cd supplier-simulator
./mvnw verify
```

Unit tests use JUnit 5 and Mockito. Controller tests use Spring MVC MockMvc.

Integration tests start a throwaway `postgres:17` Testcontainer, so they need Docker but **not** your local database or `.env`.

## API documentation (Swagger / OpenAPI)

The running application publishes interactive API documentation.

Start it with `./mvnw spring-boot:run`, then open:

| URL | Description |
|---|---|
| [Swagger UI](http://localhost:8080/swagger-ui.html) | Interactive documentation. "Try it out" sends real requests to the running application. |
| [OpenAPI JSON](http://localhost:8080/v3/api-docs) | OpenAPI 3 specification for API clients, Postman/Insomnia imports and contract inspection. |

`/swagger-ui.html` redirects to `/swagger-ui/index.html`; both addresses work.

Retrieve the raw specification:

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -m json.tool | head -40
```

Notes:

- Provided by `springdoc-openapi-starter-webmvc-ui` (see `pom.xml`).
- Endpoints are derived from Spring MVC controllers and their OpenAPI annotations.
- The documentation is available only while the application is running.
- Check application health using `curl http://localhost:8080/actuator/health` if the Actuator health endpoint is enabled.
- Port `8080` is the default. If `server.port` changes, update the URLs accordingly.
- Swagger is an API documentation and testing interface, **not an authentication or authorisation mechanism**.

---

## Product Catalogue API

The Supplier Simulator distinguishes between a **product** and a **product variant**.

This separation is important because the same product can be sold in different colours and sizes, each with its own SKU and wholesale price.

### Understanding products and variants

A **product** represents the general item in the supplier catalogue.

Example:

```text
Product: Nike Air Force 1
Product Code: NIKE-AF1-001
Brand: Nike
```

A **product variant** represents a specific sellable configuration of that product.

For example:

| Product | SKU | Colour | Size |
|---|---|---|---|
| Nike Air Force 1 | AF1-WHT-UK7 | White | UK7 |
| Nike Air Force 1 | AF1-WHT-UK8 | White | UK8 |
| Nike Air Force 1 | AF1-BLK-UK9 | Black | UK9 |

All three variants belong to the same product, but each represents a different sellable item.

**Important distinctions:**

- Product: The general catalogue item.
- Product variant: A specific colour and size combination.
- SKU: A globally unique identifier for a sellable variant.
- Inventory: The quantity available for a particular variant.

Creating a product does **not** automatically create its variants or inventory.

### Product creation workflow

```text
Create Product
      |
      v
Product receives UUID
      |
      v
Create Product Variant
      |
      v
Variant receives UUID and SKU
      |
      v
Variant belongs to Product
      |
      v
Inventory can be managed for the variant
(Future iteration)
```

A parent product must exist before a variant can be registered.

### Available endpoints

Base URL:

```text
http://localhost:8080/api/v1/products
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/products` | Create a product |
| GET | `/api/v1/products/{productId}` | Retrieve a product by UUID |
| POST | `/api/v1/products/{productId}/variants` | Create a variant for a product |
| GET | `/api/v1/products/{productId}/variants` | List all variants belonging to a product |
| GET | `/api/v1/products/{productId}/variants/{variantId}` | Retrieve a specific product variant |

### 1. Create a product

**Endpoint**

```http
POST /api/v1/products
Content-Type: application/json
```

**Request**

```json
{
  "productCode": "NIKE-AF1-001",
  "brand": "Nike",
  "name": "Air Force 1",
  "description": "Nike Air Force 1 sneaker collection"
}
```

**cURL**

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "NIKE-AF1-001",
    "brand": "Nike",
    "name": "Air Force 1",
    "description": "Nike Air Force 1 sneaker collection"
  }'
```

**Expected status:** `201 Created`

**Example response**

```json
{
  "id": "210758bf-9b55-4777-984b-12e3429127dd",
  "productCode": "NIKE-AF1-001",
  "brand": "Nike",
  "name": "Air Force 1",
  "description": "Nike Air Force 1 sneaker collection",
  "active": true,
  "createdAt": "2026-11-05T13:28:05.357913Z",
  "updatedAt": "2026-11-05T13:28:05.357913Z"
}
```

The UUID and timestamps are generated by the application.

The response also includes a `Location` header:

```http
Location: /api/v1/products/210758bf-9b55-4777-984b-12e3429127dd
```

**Business rules:**

- `productCode` is required and must be globally unique.
- `brand` is required.
- `name` is required.
- `description` is optional.
- A new product receives a UUID.
- Product creation does not create variants.
- Duplicate product codes are rejected with HTTP `409 Conflict`.

### 2. Retrieve a product

**Endpoint**

```http
GET /api/v1/products/{productId}
```

Replace `{productId}` with the UUID returned during product creation.

**cURL**

```bash
curl http://localhost:8080/api/v1/products/210758bf-9b55-4777-984b-12e3429127dd
```

**Expected status:** `200 OK`

The response contains the product's UUID, product code, brand, name, description, active status and timestamps.

If the product does not exist, the API returns `404 Not Found`.

**Important:** The product retrieval endpoint returns the product details. To retrieve its variants, use the dedicated variants endpoint.

### 3. Create a product variant

Before creating a variant, the parent product must already exist.

**Endpoint**

```http
POST /api/v1/products/{productId}/variants
Content-Type: application/json
```

**Request**

```json
{
  "sku": "AF1-WHT-UK7",
  "colour": "White",
  "size": "UK7",
  "wholesalePrice": 2100.00,
  "currency": "ZAR"
}
```

**cURL**

```bash
curl -X POST \
  http://localhost:8080/api/v1/products/210758bf-9b55-4777-984b-12e3429127dd/variants \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "AF1-WHT-UK7",
    "colour": "White",
    "size": "UK7",
    "wholesalePrice": 2100.00,
    "currency": "ZAR"
  }'
```

**Expected status:** `201 Created`

**Example response**

```json
{
  "id": "c5a1137e-d2c0-4754-9438-d7a91f396a23",
  "productId": "210758bf-9b55-4777-984b-12e3429127dd",
  "sku": "AF1-WHT-UK7",
  "colour": "White",
  "size": "UK7",
  "wholesalePrice": 2100.00,
  "currency": "ZAR",
  "active": true,
  "createdAt": "2026-11-05T13:30:00Z",
  "updatedAt": "2026-11-05T13:30:00Z"
}
```

The response includes a `Location` header pointing to the new variant resource.

**Business rules:**

- The parent product must exist.
- Every variant belongs to exactly one product.
- `sku` is required and must be globally unique.
- `colour` is required.
- `size` is required.
- The combination of product, colour and size must be unique.
- `wholesalePrice` must be non-negative and is represented using `BigDecimal`.
- `currency` must be a three-letter uppercase code, such as `ZAR`.
- New variants are active by default.
- Duplicate SKUs or colour/size combinations return `409 Conflict`.
- A missing parent product returns `404 Not Found`.

**Example uniqueness rules**

Valid:

```text
AF1-WHT-UK7 | White | UK7
AF1-WHT-UK8 | White | UK8
AF1-BLK-UK9 | Black | UK9
```

Invalid:

```text
AF1-WHT-UK7 | White | UK7
AF1-WHT-UK7 | Black | UK8
```

The same SKU cannot identify two different variants.

Also invalid for the same product:

```text
AF1-WHT-UK7     | White | UK7
AF1-WHT-UK7-NEW | White | UK7
```

A different SKU cannot bypass the product's colour/size uniqueness rule.

### 4. Retrieve a product variant

**Endpoint**

```http
GET /api/v1/products/{productId}/variants/{variantId}
```

**cURL**

```bash
curl \
  http://localhost:8080/api/v1/products/210758bf-9b55-4777-984b-12e3429127dd/variants/c5a1137e-d2c0-4754-9438-d7a91f396a23
```

**Expected status:** `200 OK`

The API returns the variant's UUID, parent product UUID, SKU, colour, size, wholesale price, currency, active status and timestamps.

A variant can only be retrieved under the product to which it belongs.

If the variant does not exist under the requested product, the API returns `404 Not Found`.

### 5. List product variants

**Endpoint**

```http
GET /api/v1/products/{productId}/variants
```

**cURL**

```bash
curl \
  http://localhost:8080/api/v1/products/210758bf-9b55-4777-984b-12e3429127dd/variants
```

**Expected status:** `200 OK`

**Example response**

```json
[
  {
    "id": "c5a1137e-d2c0-4754-9438-d7a91f396a23",
    "productId": "210758bf-9b55-4777-984b-12e3429127dd",
    "sku": "AF1-WHT-UK7",
    "colour": "White",
    "size": "UK7",
    "wholesalePrice": 2100.00,
    "currency": "ZAR",
    "active": true,
    "createdAt": "2026-11-05T13:30:00Z",
    "updatedAt": "2026-11-05T13:30:00Z"
  }
]
```

If the product exists but has no variants, the API returns:

```json
[]
```

with HTTP `200 OK`.

**An empty array is not an error.**

It means the product exists but no sellable configurations have been registered.

If the parent product does not exist, the API returns `404 Not Found`.

### API response codes

| Status | Meaning | Example |
|---|---|---|
| 200 OK | Resource retrieved successfully | Get product or variants |
| 201 Created | Resource created successfully | Create product or variant |
| 400 Bad Request | Invalid request payload | Missing required fields |
| 404 Not Found | Requested resource does not exist | Unknown product UUID |
| 409 Conflict | Business uniqueness constraint violated | Duplicate product code or SKU |
| 500 Internal Server Error | Unexpected application failure | Unhandled server error |

Domain exceptions are handled centrally using `GlobalExceptionHandler`.

Errors use Spring's `ProblemDetail` representation.

**Example: duplicate product**

```json
{
  "type": "about:blank",
  "title": "Duplicate Product",
  "status": 409,
  "detail": "Product code already exists: NIKE-AF1-001"
}
```

The exact `detail` message depends on the exception raised by the service.

### Catalogue design decisions

**Why separate products and variants?**

A product describes an item at catalogue level. A variant identifies the configuration that can actually be stocked and sold.

This prevents unnecessary duplication of product information across different colours and sizes.

**Why use UUIDs?**

UUIDs provide identifiers that do not depend on a single database sequence. They are suitable for identifying resources across independently deployed services.

**Why enforce uniqueness in both the service and database?**

Service validation produces meaningful business errors.

Database constraints protect data integrity, including situations where concurrent requests attempt to create conflicting records.

Service checks alone cannot eliminate race conditions.

**Why use BigDecimal for prices?**

`BigDecimal` avoids binary floating-point precision problems and supports explicit decimal representation for monetary values.

**Why use DTOs instead of returning JPA entities?**

DTOs establish an explicit API contract and prevent persistence details, lazy-loaded relationships and internal domain structures from leaking into external responses.

**Why use separate inventory records?**

Product variants identify what can be sold; inventory identifies how many units are available.

Inventory management will be introduced in a later iteration, including stock-on-hand, reservations and available-to-sell quantities.

---

## Troubleshooting

### `FATAL: password authentication failed for user "${POSTGRES_SUPPLIER_USER}"`

The username is the **raw placeholder** — no environment source was loaded, so Spring could not resolve `${POSTGRES_SUPPLIER_*}` and passed the literal text to the driver.

It looks like a password problem, but it is a configuration problem.

Spring Boot can leave unresolvable placeholders as-is, which is why this may appear during Flyway startup.

Causes and fixes:

- **Working directory:** `spring.config.import` uses relative paths, and the working directory differs per launcher. `application.yml` imports both `./.env` and `./supplier-simulator/.env`; keep both entries if you change launch configurations.
- **Missing file:** `supplier-simulator/.env` is git-ignored, so a fresh clone has none. Recreate it using the example above.
- **Wrong directory:** Run Maven from `supplier-simulator/`.

Quick check:

```bash
cd supplier-simulator && ls -l .env
```

### `FATAL: password authentication failed for user "supplier_app"`

The credentials were resolved but do not match the credentials stored in PostgreSQL.

A common cause is a **stale Docker volume**.

PostgreSQL initialises database credentials only when its data directory is empty. Recreating a container does not recreate its named volume.

Test authentication:

```bash
docker compose exec -T postgres sh -c \
  'PGPASSWORD="$POSTGRES_PASSWORD" psql -h postgres -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select current_user"'
```

Fix, discarding local data:

```bash
docker compose down -v
docker compose up -d postgres
```

**Warning:** `docker compose down -v` deletes the local database volume and its data. Flyway recreates the schema, not the deleted records.

Fix, keeping the data:

```bash
docker compose exec -it postgres psql -U supplier_app -d postgres
```

Inside `psql`:

```text
\password supplier_app
\q
```

### Changing the password later

Two actions are required:

1. Update `POSTGRES_SUPPLIER_PASSWORD` in `.env`.
2. Update the PostgreSQL role password or recreate the database volume.

Editing `.env` alone does not change credentials stored inside an existing PostgreSQL database.

Also check for shell-exported environment variables, which may override `.env` values in Docker Compose.

### `set -a && source .env` produces a different password

`$` is special in the shell. An unquoted `$$` becomes the shell process ID when sourced.

This can produce different credentials from those parsed by the application.

The project does not require manually sourcing `.env`. Spring imports it directly, and Docker Compose reads its own dotenv configuration.

Avoid ambiguous shell-sensitive password values or quote them appropriately.

### `The goal you specified requires a project to execute but there is no POM in this directory`

The repository root (`supplier/`) is not the Maven project directory.

The Maven project is:

```text
supplier-simulator/
```

Run:

```bash
cd supplier-simulator
./mvnw verify
```

Alternatively:

```bash
./mvnw -f supplier-simulator/pom.xml verify
```

### `Schema validation: wrong column type encountered in column [currency]`

Hibernate validates entity mappings against the database schema.

A PostgreSQL `CHAR(3)` column mapped to a plain Java `String` can produce a type mismatch.

The `ProductVariant` entity explicitly maps the field:

```java
@JdbcTypeCode(SqlTypes.CHAR)
@Column(nullable = false, length = 3)
private String currency = "ZAR";
```

Never modify an already-applied Flyway migration.

Create a new versioned migration instead, such as:

```text
V3__description.sql
```

### `Mockito is currently self-attaching to enable the inline-mock-maker`

Mockito requires instrumentation for certain mocking capabilities.

The project configures its agent through Maven Surefire's `<argLine>`, while the VS Code Test Runner has its own JVM configuration.

The following JVM warning is generally harmless:

```text
Sharing is only supported for boot loader classes because bootstrap classpath has been appended
```

It is related to JVM class-data sharing and instrumentation.

`-Xshare:off` can silence this warning.

### Swagger UI returns `connection refused`

The application is not reachable at the configured address.

Check:

```bash
curl http://localhost:8080/actuator/health
```

If the Actuator endpoint is unavailable, inspect the application startup logs and confirm that the server is listening on port `8080`.

Start the application:

```bash
cd supplier-simulator
./mvnw spring-boot:run
```

### GET product variants returns `[]`

This is expected when a product exists but has no registered variants.

Create a variant first:

```http
POST /api/v1/products/{productId}/variants
```

Then retrieve the variants:

```http
GET /api/v1/products/{productId}/variants
```

An empty list with HTTP `200 OK` is different from a missing product, which returns `404 Not Found`.

### Swagger displays `201 Undocumented`

The API returned HTTP `201 Created`, but its OpenAPI specification does not explicitly document that response.

Add an `@ApiResponse` annotation to the relevant controller operation.

This is a documentation issue, not necessarily an endpoint failure.

### GitHub Actions: `The job was not started because your account is locked due to a billing issue`

An account-level billing restriction can prevent workflow runs, including workflows in public repositories.

Resolve the billing restriction through GitHub account settings before attempting to re-enable the workflow.

Local Maven verification remains available:

```bash
./mvnw clean verify
```

---

## Development roadmap

| Iteration | Scope | Status |
|---|---|---|
| 1 | Spring Boot project bootstrap | Complete |
| 2 | PostgreSQL, Docker and Flyway foundation | Complete |
| 3 | Integration testing with Testcontainers | Complete |
| 4 | Product Catalogue API, unit tests and Swagger | In progress — final verification |
| 5 | Inventory management and stock reservations | Planned |
| 6 | Retailer ordering | Planned |
| 7 | Supplier order lifecycle | Planned |
| 8 | Supplier–Retail integration | Planned |
| 9 | Security, concurrency, idempotency and hardening | Planned |
| 10 | Documentation, Docker distribution and release | Planned |

The final platform will demonstrate independent application deployment, secure API contracts, inventory consistency, order processing, logistics integration, delivery confirmation and auditable financial events.

The objective is to demonstrate production-oriented backend and integration engineering through a realistic commerce workflow.
