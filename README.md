# MAS Java PoC - Multi-Tenant Book Management System

A Spring Boot application demonstrating enterprise patterns including multi-tenancy, role-based access control, async job processing, and public APIs.

## Requirements Implementation

### 1. Simple CRUD Operations

**Domain Model:** Book ↔ Author (One-to-Many relationship)

- **Entities:** `Book`, `Author`, `User` with JPA annotations
- **Repositories:** Spring Data JPA interfaces (`BookRepository`, `AuthorRepository`)
- **Services:** Business logic layer with `BookService`, `AuthorService`
- **Controllers:** RESTful endpoints in `BookController`, `AuthorController`
- **DTOs:** MapStruct for entity-DTO mapping with zero boilerplate

**Endpoints:**
- `GET/POST /api/books`, `GET/PUT/DELETE /api/books/{id}`
- `GET/POST /api/authors`, `GET/PUT/DELETE /api/authors/{id}`

### 2. Public API & Webhooks

**Public API** (`PublicApiController`):
- **No authentication required** for read-only endpoints
- Endpoints: `/public/api/books`, `/public/api/authors`, `/public/api/books/genre/{genre}`
- Configured in `SecurityConfig` to bypass JWT authentication

**Webhooks** (`BackgroundJobService`):
- `callWebhook(url, payload)` - Async webhook invocation
- Executed on dedicated thread pool for reliability
- Used for event notifications and integrations

### 3. Role-Based Access Control (RBAC)

**Implementation:**
- **JWT-based authentication** with `JwtTokenProvider` and `JwtAuthenticationFilter`
- **Roles:** `USER`, `ADMIN`, `PUBLIC` (enum in `User` entity)
- **Method-level security:** `@PreAuthorize` annotations on controllers
- Create/Update: `hasAnyRole('ADMIN', 'USER')`
- Delete: `hasRole('ADMIN')` only
- **Configuration:** `@EnableMethodSecurity` in `SecurityConfig`

**Auth Flow:**
1. Login via `/api/auth/login` → Returns JWT token
2. Token included in `Authorization: Bearer <token>` header
3. Filter validates token and sets Spring Security context
4. Method security enforced on each endpoint

### 4. Compute-Intensive Long-Running Jobs

**Design:** Fast startup with async execution (handles 5sec - 10min jobs)

**Implementation** (`LongRunningJobService`):
- **Dedicated thread pool:** `longRunningTaskExecutor` (10-20 threads, 500 queue capacity)
- **Async execution:** `@Async` annotation with `CompletableFuture<T>` return type
- **Quick start:** Jobs submitted immediately, execution happens in background
- **Methods:**
- `executeComputeIntensiveJob(jobId)` - Simulates heavy computation
- `executeDataProcessingJob(dataId)` - Handles data processing tasks

**Configuration** (`AsyncConfig`):

```java
@Bean(name = "longRunningTaskExecutor")
- corePoolSize: 10
- maxPoolSize: 20
- queueCapacity: 500
- awaitTermination: 60s (graceful shutdown)
```

**Usage via API:**
- `POST /api/jobs/compute/{jobId}` - Trigger compute job
- `POST /api/jobs/process` - Trigger data processing

### 5. Critical Background Jobs

**Implementation** (`BackgroundJobService`):
- **Separate thread pool:** `taskExecutor` (5-10 threads, 100 queue capacity)
- **Fire-and-forget:** Async methods with void return
- **Use cases:**
- `sendEmail(to, subject, body)` - Email notifications
- `callWebhook(url, payload)` - External webhooks
- `processNotification(userId, message)` - User notifications

**Scheduled Jobs** (Quartz + Cron):
- `ScheduledLoggingJob` - Periodic system maintenance
- `TenantDailyReportJob` - Per-tenant scheduled reports
- Configured via `QuartzConfig` with JDBC persistence

**Message Queue** (RabbitMQ):
- Spring AMQP integration for reliable async messaging
- Configuration in `RabbitMQConfig`

### 6. Multi-Tenancy with Separate Databases

**Architecture:** Database-per-tenant with dynamic routing

**Components:**
- **Catalog DB** (port 5433): Stores tenant metadata (`tenants` table)
- **Tenant DBs** (ports 5434, 5435...): Isolated business data per company
- **TenantContext:** Thread-local storage for current tenant ID
- **TenantInterceptor:** Extracts tenant from subdomain (e.g., `company1.optahaul.com`)
- **TenantRoutingDataSource:** Routes connections to correct tenant database
- **MultiTenancyConfig:** Dual EntityManager setup (catalog + tenant)

**Flow:**

```
Request → TenantInterceptor → TenantContext → TenantRoutingDataSource → Tenant DB
```

**Benefits:**
- Complete data isolation
- Independent scaling per tenant
- Tenant-specific migrations via Flyway

See [MULTI_TENANCY_GUIDE.md](MULTI_TENANCY_GUIDE.md) for detailed architecture.

### 7. Code Style & Formatting

**Tools:**
- **Checkstyle:** `checkstyle.xml` - Enforces code conventions (warns on star imports)
- **Spotless:** Maven plugin for automatic code formatting
- **Formatter Profile:** `formatter/optahaul_java_formatter_profiles.xml`

**Enforcement:**
- Build-time validation via Maven plugins
- Consistent code style across team

**Run:**

```bash
mvn checkstyle:check
mvn spotless:check
mvn spotless:apply  # Auto-fix formatting
```

### 8. Database Access

**Technology Stack:**
- **JPA/Hibernate:** ORM with PostgreSQL dialect
- **Spring Data JPA:** Repository pattern with query derivation
- **Flyway:** Database migration management
- Catalog migrations: `db/catalog/migration/`
- Tenant migrations: `db/migration/`
- **HikariCP:** High-performance connection pooling (per tenant)

**Repository Pattern:**

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByGenreIgnoreCase(String genre);
    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.id = :id")
    Book findByIdWithAuthor(@Param("id") Long id);
}
```

**Transaction Management:** `@Transactional` on service layer

### 9. Domain Model: Books & Authors

**Entities:**

**Author:**
- Fields: `id`, `name`, `birthDate`, `bio`, `createdAt`, `updatedAt`
- Relationship: One-to-Many with `Book` (cascade ALL, orphan removal)

**Book:**
- Fields: `id`, `title`, `author`, `genre`, `pageCount`, `language`, `publicationDate`, `isbn`
- Relationship: Many-to-One with `Author` (lazy loading)

**Features:**
- Automatic timestamps via `@PrePersist` and `@PreUpdate`
- Lombok for boilerplate reduction
- Validation annotations (`@NotNull`, size constraints)

### 10. Dockerfile

**Base Image:** `eclipse-temurin:25-jre-alpine` (lightweight JRE)

**Features:**
- **Multi-stage ready:** Uses pre-built JAR from `target/`
- **Health check support:** `wait-for-it.sh` script waits for catalog DB
- **Environment variables:** Configurable DB connection
- **Port:** Exposes 8080

**Docker Compose:**
- 3 PostgreSQL containers (catalog + 2 tenants)
- Health checks for all databases
- Persistent volumes for data

## Technology Stack

- **Spring Boot 3.5.6** - Framework
- **Java 25** - Language
- **PostgreSQL** - Database
- **JWT (jjwt 0.12.6)** - Authentication
- **MapStruct 1.6.3** - DTO Mapping
- **Quartz** - Job Scheduling
- **Flyway** - DB Migrations
- **SpringDoc/Swagger** - API Documentation
- **RabbitMQ** - Message Queue
- **Lombok** - Boilerplate Reduction
- **HikariCP** - Connection Pooling

## Getting Started

### Prerequisites

- Java 25
- Maven 3.6+
- Docker & Docker Compose

### Run Locally

1. **Build app:**

```bash
mvn clean install
```

With client

```bash
mvn clean install -Popenapi-generate
```

2. **Start containers:**

```bash
docker compose up --build -d
```

4. **Access API docs:**

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/api-docs

### Configuration

Key properties in `application.properties`:
- **Catalog DB:** `spring.datasource.catalog.*`
- **Multi-tenancy:** `multitenancy.enabled=true`
- **JWT Secret:** `jwt.secret`
- **Async Pools:** `spring.task.execution.*`
- **Quartz:** `spring.quartz.*`

## Project Structure

```
src/main/java/com/optahaul/mas_java_poc/
├── config/              # Spring configuration (Security, Async, Quartz)
├── controller/          # REST endpoints
├── domain/              # JPA entities (Book, Author, User)
├── dto/                 # Data Transfer Objects
├── job/                 # Async & scheduled jobs
├── mapper/              # MapStruct interfaces
├── multitenancy/        # Multi-tenant infrastructure
├── repository/          # Spring Data repositories
├── security/            # JWT authentication
├── service/             # Business logic
└── websocket/           # WebSocket support
```

## Testing

```bash
mvn test
```

Test reports in `target/surefire-reports/`

## Additional Documentation

- [MULTI_TENANCY_GUIDE.md](MULTI_TENANCY_GUIDE.md) - Detailed multi-tenancy architecture
- [TENANT_JOB_SCHEDULING_GUIDE.md](TENANT_JOB_SCHEDULING_GUIDE.md) - Tenant-aware Quartz jobs

## License

Internal PoC project
