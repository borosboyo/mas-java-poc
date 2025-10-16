# Postman Testing Guide - Multi-Tenancy Edition

## Overview

This guide demonstrates how to test the multi-tenant API using Postman or curl commands.

## Important: X-Tenant-ID Header

Every API request (except tenant management) requires the `X-Tenant-ID` header to specify which tenant's database to use.

```
X-Tenant-ID: tenant1
```

Without this header, the system defaults to `tenant1`.

---

## 1. Tenant Management

### List All Tenants

```bash
GET http://localhost:8080/api/tenants
```

**Response:**

```json
["tenant1", "tenant2", "tenant3"]
```

### Provision New Tenant

```bash
POST http://localhost:8080/api/tenants/company_acme/provision
```

**Response:**

```
Tenant provisioned successfully: company_acme
```

This creates a new database `tenant_company_acme` and runs all migrations.

---

## 2. Multi-Tenant Data Operations

### Create Author for Tenant 1 (Company A)

```bash
POST http://localhost:8080/api/authors
Headers:
  Content-Type: application/json
  X-Tenant-ID: tenant1

Body:
{
  "name": "Alice Johnson",
  "birthDate": "1975-03-15",
  "nationality": "American"
}
```

### Create Author for Tenant 2 (Company B)

```bash
POST http://localhost:8080/api/authors
Headers:
  Content-Type: application/json
  X-Tenant-ID: tenant2

Body:
{
  "name": "Bob Smith",
  "birthDate": "1980-07-20",
  "nationality": "British"
}
```

### List Authors for Tenant 1

```bash
GET http://localhost:8080/api/authors
Headers:
  X-Tenant-ID: tenant1
```

**Response:** Only returns Alice Johnson (tenant1's data)

### List Authors for Tenant 2

```bash
GET http://localhost:8080/api/authors
Headers:
  X-Tenant-ID: tenant2
```

**Response:** Only returns Bob Smith (tenant2's data)

---

## 3. Testing Data Isolation

### Step 1: Create Books for Different Tenants

**Book for Tenant 1:**

```bash
POST http://localhost:8080/api/books
Headers:
  Content-Type: application/json
  X-Tenant-ID: tenant1

Body:
{
  "title": "Tenant 1 Book",
  "isbn": "111-1111111111",
  "publicationDate": "2024-01-01",
  "authorId": 1
}
```

**Book for Tenant 2:**

```bash
POST http://localhost:8080/api/books
Headers:
  Content-Type: application/json
  X-Tenant-ID: tenant2

Body:
{
  "title": "Tenant 2 Book",
  "isbn": "222-2222222222",
  "publicationDate": "2024-02-02",
  "authorId": 1
}
```

### Step 2: Verify Isolation

**List books for tenant1:**

```bash
GET http://localhost:8080/api/books
Headers:
  X-Tenant-ID: tenant1
```

Returns only "Tenant 1 Book"

**List books for tenant2:**

```bash
GET http://localhost:8080/api/books
Headers:
  X-Tenant-ID: tenant2
```

Returns only "Tenant 2 Book"

---

## 4. Complete Multi-Tenant Workflow

### Scenario: Three Companies Using the System

#### Company A (tenant1) - Publishing House

```bash
# Create authors
POST http://localhost:8080/api/authors
Headers: X-Tenant-ID: tenant1
Body: {"name": "Stephen King", "birthDate": "1947-09-21", "nationality": "American"}

POST http://localhost:8080/api/authors
Headers: X-Tenant-ID: tenant1
Body: {"name": "J.K. Rowling", "birthDate": "1965-07-31", "nationality": "British"}

# Create books
POST http://localhost:8080/api/books
Headers: X-Tenant-ID: tenant1
Body: {"title": "The Shining", "isbn": "978-0-385-12167-5", "publicationDate": "1977-01-28", "authorId": 1}
```

#### Company B (tenant2) - Academic Publisher

```bash
# Create authors
POST http://localhost:8080/api/authors
Headers: X-Tenant-ID: tenant2
Body: {"name": "Carl Sagan", "birthDate": "1934-11-09", "nationality": "American"}

# Create books
POST http://localhost:8080/api/books
Headers: X-Tenant-ID: tenant2
Body: {"title": "Cosmos", "isbn": "978-0-375-50832-5", "publicationDate": "1980-01-01", "authorId": 1}
```

#### Company C (tenant3) - Tech Publisher

```bash
# Create authors
POST http://localhost:8080/api/authors
Headers: X-Tenant-ID: tenant3
Body: {"name": "Robert Martin", "birthDate": "1952-12-05", "nationality": "American"}

# Create books
POST http://localhost:8080/api/books
Headers: X-Tenant-ID: tenant3
Body: {"title": "Clean Code", "isbn": "978-0-13-235088-4", "publicationDate": "2008-08-01", "authorId": 1}
```

---

## 5. Postman Collection Setup

### Creating a Postman Environment

1. Create environment "Multi-Tenant Local"
2. Add variables:
   - `baseUrl`: `http://localhost:8080`
   - `tenant1`: `tenant1`
   - `tenant2`: `tenant2`
   - `tenant3`: `tenant3`

### Request Templates

#### Get Authors (Dynamic Tenant)

```
GET {{baseUrl}}/api/authors
Headers:
  X-Tenant-ID: {{tenant1}}
```

#### Create Author (Dynamic Tenant)

```
POST {{baseUrl}}/api/authors
Headers:
  Content-Type: application/json
  X-Tenant-ID: {{tenant2}}
Body:
{
  "name": "{{$randomFullName}}",
  "birthDate": "1980-01-01",
  "nationality": "{{$randomCountry}}"
}
```

---

## 6. Curl Commands Cheat Sheet

### Quick Test Commands

```bash
# List tenants
curl http://localhost:8080/api/tenants

# Provision new tenant
curl -X POST http://localhost:8080/api/tenants/newcompany/provision

# Create author for tenant1
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name":"Test Author","birthDate":"1980-01-01","nationality":"American"}'

# Get authors for tenant1
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/authors

# Get authors for tenant2
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/authors

# Create book for tenant1
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"title":"Test Book","isbn":"123-456","publicationDate":"2024-01-01","authorId":1}'
```

---

## 7. Testing with Docker

### Start Services

```bash
# Build the JAR
./mvnw clean package -DskipTests

# Start containers
docker compose up --build
```

### Test Multi-Tenancy

```bash
# Wait for services to start, then test
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/authors
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/authors
```

---

## 8. Database Verification

### Connect to Tenant Databases

```bash
# Connect to tenant1 database
docker exec -it mas-java-poc-postgres-1 psql -U myuser -d tenant_tenant1

# List tables
\dt

# Check authors
SELECT * FROM authors;

# Exit
\q
```

```bash
# Connect to tenant2 database
docker exec -it mas-java-poc-postgres-1 psql -U myuser -d tenant_tenant2

# Verify different data
SELECT * FROM authors;
```

---

## 9. Expected Responses

### Success Response (Create Author)

```json
{
  "id": 1,
  "name": "Alice Johnson",
  "birthDate": "1975-03-15",
  "nationality": "American"
}
```

### Success Response (List Authors - Tenant 1)

```json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "birthDate": "1975-03-15",
    "nationality": "American"
  }
]
```

### Success Response (List Authors - Tenant 2)

```json
[
  {
    "id": 1,
    "name": "Bob Smith",
    "birthDate": "1980-07-20",
    "nationality": "British"
  }
]
```

Notice: Both tenants can have ID=1, as they're in separate databases.

---

## 10. Error Scenarios

### Missing Tenant Header (defaults to tenant1)

```bash
curl http://localhost:8080/api/authors
# Uses tenant1 by default
```

### Invalid Tenant (auto-provisions)

```bash
curl -H "X-Tenant-ID: invalidtenant" http://localhost:8080/api/authors
# Creates tenant_invalidtenant database automatically
```

---

## 11. Swagger UI Testing

1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" (if authentication is enabled)
3. For each API call, add `X-Tenant-ID` parameter in the header section
4. Test different tenants by changing the header value

---

## 12. Performance Testing

### Create Multiple Authors for Load Testing

```bash
# Create 100 authors for tenant1
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/authors \
    -H "Content-Type: application/json" \
    -H "X-Tenant-ID: tenant1" \
    -d "{\"name\":\"Author $i\",\"birthDate\":\"1980-01-01\",\"nationality\":\"American\"}"
done

# Verify count
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/authors | jq 'length'
```

---

## Tips

1. **Use Postman Collections**: Organize requests by tenant
2. **Set Default Headers**: Add X-Tenant-ID as a collection-level header
3. **Use Variables**: Store tenant IDs in environment variables
4. **Test Isolation**: Always verify data doesn't leak between tenants
5. **Monitor Logs**: Check application logs for tenant routing messages

---

## Troubleshooting

### Issue: Data showing up in wrong tenant

- **Check**: Verify X-Tenant-ID header is set correctly
- **Debug**: Check application logs for "Setting tenant context to: ..."

### Issue: Database not found

- **Cause**: Tenant not provisioned
- **Solution**: Call POST /api/tenants/{tenantId}/provision

### Issue: Connection errors

- **Check**: PostgreSQL is running and accessible
- **Verify**: User has CREATE DATABASE permission
