# Multi-Tenancy Implementation Guide

## Overview

This project implements **database-per-tenant** multi-tenancy, where each tenant (company) gets their own isolated PostgreSQL database. This ensures complete data isolation and independent scaling.

## Architecture

### Components

1. **TenantContext**: Thread-local storage for the current tenant ID
2. **TenantFilter**: HTTP filter that extracts tenant ID from request headers
3. **TenantRoutingDataSource**: Routes database connections to the correct tenant database
4. **TenantDataSourceManager**: Creates and manages datasources for each tenant
5. **MultiTenancyConfig**: Configures multi-tenancy and runs Flyway migrations per tenant

### How It Works

```
HTTP Request with X-Tenant-ID header
    ↓
TenantFilter extracts tenant ID
    ↓
TenantContext stores tenant ID (thread-local)
    ↓
TenantRoutingDataSource routes to correct database
    ↓
TenantDataSourceManager provides tenant-specific DataSource
    ↓
Application code executes against tenant database
```

## Database Structure

- **Master Database**: `postgres` (used to create tenant databases)
- **Tenant Databases**: `tenant_<tenantId>` (e.g., `tenant_tenant1`, `tenant_tenant2`)

Each tenant database has:
- Complete schema (authors, books, users, etc.)
- Independent data
- Own Flyway migration history

## Usage

### 1. Specify Tenant in HTTP Requests

Add the `X-Tenant-ID` header to every API request:

```bash
# Access tenant1's data
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/authors

# Access tenant2's data
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/authors

# No header = defaults to tenant1
curl http://localhost:8080/api/authors
```

### 2. Provision New Tenants

Create a new tenant database:

```bash
# Provision tenant3
curl -X POST http://localhost:8080/api/tenants/tenant3/provision
```

This will:
- Create database `tenant_tenant3`
- Run all Flyway migrations
- Cache the datasource for future requests

### 3. List All Tenants

```bash
curl http://localhost:8080/api/tenants
```

Returns: `["tenant1", "tenant2", "tenant3"]`

## Testing Multi-Tenancy

### Scenario: Create Authors for Different Tenants

```bash
# Create author for Company A (tenant1)
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Company A Author",
    "birthDate": "1980-01-01",
    "nationality": "American"
  }'

# Create author for Company B (tenant2)
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant2" \
  -d '{
    "name": "Company B Author",
    "birthDate": "1990-02-02",
    "nationality": "British"
  }'

# List authors for tenant1 (only sees Company A's author)
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/authors

# List authors for tenant2 (only sees Company B's author)
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/authors
```

### Verify Data Isolation

```bash
# Connect to tenant1 database
psql -U myuser -d tenant_tenant1 -c "SELECT * FROM authors;"

# Connect to tenant2 database
psql -U myuser -d tenant_tenant2 -c "SELECT * FROM authors;"
```

Each database will only show its own tenant's data.

## Default Tenants

The application automatically provisions these tenants on startup:
- `tenant1` (default if no header is provided)
- `tenant2`
- `tenant3`

## Configuration

### application.properties

```properties
# Master database for creating tenant databases
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=myuser
spring.datasource.password=secret
```

### Environment Variables (Docker)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=secret
```

## Docker Compose Setup

Update `compose.yaml` to use postgres as the master database:

```yaml
services:
  postgres:
    image: 'postgres:latest'
    environment:
      - 'POSTGRES_DB=postgres'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=myuser'
    ports:
      - '5432:5432'

  backend:
    build: .
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/postgres
      SPRING_DATASOURCE_USERNAME: myuser
      SPRING_DATASOURCE_PASSWORD: secret
    ports:
      - '8080:8080'
```

## Flyway Migrations

All migrations in `src/main/resources/db/migration/` are applied to each tenant database:

- `V1__initial_schema.sql`
- `V2__create_quartz_tables.sql`
- `V3__add_isbn_to_books.sql`
- Future migrations...

When adding a new migration, it will be applied to:
- All existing tenant databases on next startup
- New tenant databases when provisioned

## Production Considerations

### 1. Tenant Registry

In production, maintain a tenant registry database:

```sql
CREATE TABLE tenants (
    id VARCHAR(50) PRIMARY KEY,
    company_name VARCHAR(255),
    created_at TIMESTAMP,
    status VARCHAR(20)
);
```

### 2. Connection Pooling

Configure per-tenant connection pools:

```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```

### 3. Tenant Discovery

Instead of hardcoded tenant list, load from registry:

```java
@PostConstruct
public void initializeTenants() {
    List<String> tenants = tenantRepository.findAllActiveTenantIds();
    tenants.forEach(dataSourceManager::getDataSource);
}
```

### 4. Security

- Validate tenant ID against authenticated user
- Implement tenant-based authorization
- Add rate limiting per tenant

```java
@Component
public class TenantSecurityFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String tenantId = request.getHeader("X-Tenant-ID");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!userService.hasAccessToTenant(auth.getName(), tenantId)) {
            throw new AccessDeniedException("User not authorized for tenant");
        }
        // ... continue
    }
}
```

### 5. Monitoring

Track metrics per tenant:
- Database connections
- Query performance
- Storage usage
- API request rates

## Troubleshooting

### Issue: "No tenant context set"

**Cause**: Missing `X-Tenant-ID` header or filter not applied

**Solution**: Ensure TenantFilter is registered and header is present

### Issue: Database creation fails

**Cause**: Insufficient PostgreSQL permissions

**Solution**: Grant CREATE DATABASE permission:

```sql
ALTER USER myuser CREATEDB;
```

### Issue: Flyway migration errors

**Cause**: Migration already applied or failed

**Solution**: Check flyway_schema_history table in tenant database:

```sql
SELECT * FROM tenant_tenant1.flyway_schema_history;
```

## API Endpoints

### Tenant Management

| Method |              Endpoint               |     Description      |
|--------|-------------------------------------|----------------------|
| GET    | `/api/tenants`                      | List all tenants     |
| POST   | `/api/tenants/{tenantId}/provision` | Provision new tenant |

### Data Operations (require X-Tenant-ID header)

| Method |    Endpoint    |       Description        |
|--------|----------------|--------------------------|
| GET    | `/api/authors` | List tenant's authors    |
| POST   | `/api/authors` | Create author for tenant |
| GET    | `/api/books`   | List tenant's books      |
| POST   | `/api/books`   | Create book for tenant   |

## Example Postman Collection

See `POSTMAN_TESTING_GUIDE.md` for complete examples with multi-tenancy headers.

## Benefits

1. **Complete Data Isolation**: Each tenant has its own database
2. **Independent Scaling**: Scale tenant databases independently
3. **Backup & Restore**: Per-tenant backup and recovery
4. **Customization**: Tenant-specific schema modifications possible
5. **Compliance**: Meet data residency requirements per tenant

## Limitations

1. **Resource Usage**: More databases = more connections and memory
2. **Management Overhead**: Multiple databases to maintain
3. **Cross-Tenant Queries**: Not possible (by design for isolation)
4. **Migration Time**: Migrations run serially per tenant

## Alternative Approaches

This implementation uses **database-per-tenant**. Other approaches include:

1. **Schema-per-tenant**: All tenants in one database, different schemas
2. **Row-level security**: All tenants in one database/schema, filtered by tenant_id column

Each has trade-offs between isolation, performance, and management complexity.
