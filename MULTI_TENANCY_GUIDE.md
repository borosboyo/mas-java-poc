# Multi-Tenancy Implementation Guide

## Overview

This project implements **database-per-tenant** multi-tenancy, where each tenant (company) gets their own isolated PostgreSQL database. This ensures complete data isolation and independent scaling.

## Architecture

### Database Structure

**Catalog Database (Port 5433)**
- **Only contains**: `tenants` table (metadata about each company)
- **Purpose**: Store tenant connection information and configuration

**Tenant Databases (Ports 5434, 5435, etc.)**
- **Contains**: `users`, `authors`, `books` tables (all business data)
- **Purpose**: Isolated data storage per company

### Entity Organization

- **Catalog Entities** (`com.optahaul.mas_java_poc.domain.catalog`):
  - `Tenant` - Managed by catalog database
- **Tenant Entities** (`com.optahaul.mas_java_poc.domain`):
  - `User` - Managed by tenant databases
  - `Author` - Managed by tenant databases
  - `Book` - Managed by tenant databases

### Components

1. **Catalog Database**: Central database storing tenant metadata and connection information
2. **TenantContext**: Thread-local storage for the current tenant ID
3. **TenantInterceptor**: Intercepts HTTP requests to extract tenant from subdomain
4. **TenantRoutingDataSource**: Routes database connections to the correct tenant database
5. **TenantService**: Manages tenant metadata with caching
6. **MultiTenancyConfig**: Configures dual entity managers (catalog + tenant)

### How It Works

```
HTTP Request to company1.optahaul.com
    ↓
TenantInterceptor extracts "company1" from subdomain
    ↓
TenantContext stores tenant ID (thread-local)
    ↓
TenantRoutingDataSource queries catalog DB for connection info
    ↓
Creates/retrieves HikariCP connection pool for tenant
    ↓
Application code executes against tenant database (users, authors, books)
```

### Authentication Flow

```
POST /api/auth/login with Host: company1.optahaul.com
    ↓
TenantInterceptor extracts tenant "company1"
    ↓
UserDetailsService queries users table from company1's tenant DB
    ↓
Authentication happens using company1's users
    ↓
JWT token returned with tenant context
```

## Quick Start

### 1. Start All Databases

```bash
./setup-multitenancy.sh
```

This script:
- Starts catalog DB, tenant1 DB, tenant2 DB
- Initializes catalog with tenant metadata
- Verifies configuration

### 2. Start the Application

```bash
./mvnw spring-boot:run
```

### 3. Test Multi-Tenancy

#### Using subdomain (requires host file or DNS)

Add to `/etc/hosts`:

```
127.0.0.1 company1.optahaul.com
127.0.0.1 company2.optahaul.com
```

Then access:

```bash
# Tenant 1 data
curl http://company1.optahaul.com:8080/api/authors

# Tenant 2 data
curl http://company2.optahaul.com:8080/api/authors
```

#### Manual Testing (Without DNS)

You can still test by modifying the `TenantInterceptor` to extract tenant from a header temporarily, or use a proxy.

## Configuration

### application.properties

```properties
# Catalog Database
spring.datasource.catalog.url=jdbc:postgresql://localhost:5433/catalog_db
spring.datasource.catalog.username=myuser
spring.datasource.catalog.password=secret

# Tenant Configuration
multitenancy.enabled=true
multitenancy.tenant-resolution-strategy=subdomain
multitenancy.default-domain=optahaul.com

# HikariCP Pool Settings
tenant.datasource.hikari.maximum-pool-size=10
tenant.datasource.hikari.minimum-idle=2
```

## Adding New Tenants

### Programmatic Approach

```java
@Autowired
private TenantService tenantService;

public void createNewTenant() {
    Tenant tenant = tenantService.createTenant(
        "company3",           // tenant ID
        "Company Three Corp", // company name
        "company3"            // subdomain
    );
}
```

### Manual Database Approach

```sql
INSERT INTO tenants (tenant_id, company_name, subdomain, db_url, db_username, db_password, status)
VALUES (
    'company3',
    'Company Three Corp',
    'company3',
    'jdbc:postgresql://localhost:5436/tenant_company3',
    'myuser',
    'secret',
    'ACTIVE'
);
```

Then create the physical database:

```bash
createdb -h localhost -p 5436 -U myuser tenant_company3
```

## Docker Compose Setup

The `compose.yaml` includes:
- **catalog-postgres**: Port 5433 (Catalog DB)
- **tenant1-postgres**: Port 5434 (Company 1)
- **tenant2-postgres**: Port 5435 (Company 2)
- **rabbitmq**: Ports 5672, 15672

## Security Considerations

### Production Recommendations

1. **Encrypt Database Passwords**: Use AWS Secrets Manager or Vault
2. **Connection Pooling**: Configure per tenant based on usage
3. **Tenant Isolation**: Validate tenant access in all API endpoints
4. **Rate Limiting**: Implement per-tenant rate limits
5. **Audit Logging**: Log all tenant context switches

### Example: Using Secrets Manager

```java
private DataSource createTenantDataSource(String tenantId) {
    Tenant tenant = tenantService.findByTenantId(tenantId);

    // Fetch password from secrets manager
    String password = secretsManager.getSecret(
        "tenant/" + tenantId + "/db-password"
    );

    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl(tenant.getDbUrl());
    ds.setUsername(tenant.getDbUsername());
    ds.setPassword(password); // Use fetched secret
    return ds;
}
```

## Monitoring

### Check Tenant Metadata

```bash
psql -h localhost -p 5433 -U myuser -d catalog_db \
  -c "SELECT tenant_id, company_name, subdomain, status FROM tenants;"
```

### Verify Tenant Databases

```bash
# Check tenant1 data
psql -h localhost -p 5434 -U myuser -d tenant_company1 -c "\dt"

# Check tenant2 data
psql -h localhost -p 5435 -U myuser -d tenant_company2 -c "\dt"
```

## Troubleshooting

### Issue: "Cannot identify tenant"

**Cause**: Subdomain doesn't match any tenant in catalog
**Solution**:
1. Check catalog DB: `SELECT * FROM tenants;`
2. Verify subdomain in request matches tenant.subdomain
3. Check `/etc/hosts` or DNS configuration

### Issue: "Tenant not found or inactive"

**Cause**: Tenant status is not "ACTIVE" or doesn't exist
**Solution**: Update tenant status in catalog DB

### Issue: Connection pool exhausted

**Cause**: Too many concurrent connections
**Solution**: Increase `tenant.datasource.hikari.maximum-pool-size`

## Testing

The test suite uses H2 in-memory databases with multi-tenancy disabled.

### application-test.properties

```properties
# Catalog DB for tests
spring.datasource.catalog.url=jdbc:h2:mem:catalog_testdb;MODE=PostgreSQL
multitenancy.enabled=false
```

## Performance Tips

1. **Cache Tenant Metadata**: Already enabled with `@Cacheable`
2. **Connection Pool Tuning**: Adjust based on tenant usage patterns
3. **Lazy DataSource Creation**: DataSources created on-demand
4. **DataSource Cleanup**: Call `removeTenantDataSource()` for suspended tenants

## Migration Strategy

### From Single-Tenant to Multi-Tenant

1. **Backup existing data**
2. **Create catalog database**
3. **Insert tenant metadata**
4. **Create tenant-specific databases**
5. **Migrate data to tenant databases**
6. **Update application.properties**
7. **Deploy with multi-tenancy enabled**

## API Examples

### Creating Resources (Tenant-Specific)

```bash
# Create author for company1
curl -X POST http://company1.optahaul.com:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "birthDate": "1980-01-01"}'

# Create author for company2
curl -X POST http://company2.optahaul.com:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Smith", "birthDate": "1985-05-15"}'
```

### Verifying Data Isolation

```bash
# List company1 authors (only shows company1 data)
curl http://company1.optahaul.com:8080/api/authors

# List company2 authors (only shows company2 data)
curl http://company2.optahaul.com:8080/api/authors
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────┐
│           Application Layer                      │
│  (Spring Boot + JPA + Multi-Tenancy Config)     │
└─────────────┬───────────────────────────────────┘
              │
              ├──────────────────┬─────────────────┐
              │                  │                 │
    ┌─────────▼─────────┐  ┌────▼──────┐  ┌──────▼─────┐
    │  Catalog DB       │  │ Tenant 1  │  │  Tenant 2  │
    │  (Port 5433)      │  │ (Port     │  │  (Port     │
    │                   │  │  5434)    │  │   5435)    │
    │ - tenants table   │  │ - authors │  │  - authors │
    │ - metadata        │  │ - books   │  │  - books   │
    └───────────────────┘  │ - users   │  │  - users   │
                           └───────────┘  └────────────┘
```
