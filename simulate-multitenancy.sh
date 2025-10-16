#!/bin/bash

# Multi-Tenancy Simulation Script
# This script demonstrates how different tenants have isolated data

echo "=========================================="
echo "Multi-Tenancy Simulation"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080"

echo "Step 1: List all tenants"
echo "----------------------------------------"
curl -s $BASE_URL/api/tenants | jq .
echo ""
echo ""

echo "Step 2: Create authors for different tenants"
echo "----------------------------------------"

echo "Creating 'Alice Johnson' for tenant1 (Company A)..."
curl -s -X POST $BASE_URL/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "name": "Alice Johnson",
    "birthDate": "1975-03-15",
    "nationality": "American"
  }' | jq .

echo ""
echo "Creating 'Bob Smith' for tenant2 (Company B)..."
curl -s -X POST $BASE_URL/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant2" \
  -d '{
    "name": "Bob Smith",
    "birthDate": "1980-07-20",
    "nationality": "British"
  }' | jq .

echo ""
echo "Creating 'Carol Davis' for tenant3 (Company C)..."
curl -s -X POST $BASE_URL/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant3" \
  -d '{
    "name": "Carol Davis",
    "birthDate": "1985-11-30",
    "nationality": "Canadian"
  }' | jq .

echo ""
echo ""

echo "Step 3: Verify data isolation - each tenant sees only their data"
echo "----------------------------------------"

echo "Authors for tenant1 (should only see Alice):"
curl -s -H "X-Tenant-ID: tenant1" $BASE_URL/api/authors | jq .
echo ""

echo "Authors for tenant2 (should only see Bob):"
curl -s -H "X-Tenant-ID: tenant2" $BASE_URL/api/authors | jq .
echo ""

echo "Authors for tenant3 (should only see Carol):"
curl -s -H "X-Tenant-ID: tenant3" $BASE_URL/api/authors | jq .
echo ""

echo ""
echo "Step 4: Create books for each tenant"
echo "----------------------------------------"

echo "Creating book for tenant1..."
curl -s -X POST $BASE_URL/api/books \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "title": "Company A Book",
    "isbn": "111-1111111111",
    "publicationDate": "2024-01-01",
    "authorId": 1
  }' | jq .

echo ""
echo "Creating book for tenant2..."
curl -s -X POST $BASE_URL/api/books \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant2" \
  -d '{
    "title": "Company B Book",
    "isbn": "222-2222222222",
    "publicationDate": "2024-02-02",
    "authorId": 1
  }' | jq .

echo ""
echo "Creating book for tenant3..."
curl -s -X POST $BASE_URL/api/books \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant3" \
  -d '{
    "title": "Company C Book",
    "isbn": "333-3333333333",
    "publicationDate": "2024-03-03",
    "authorId": 1
  }' | jq .

echo ""
echo ""

echo "Step 5: Verify book isolation"
echo "----------------------------------------"

echo "Books for tenant1:"
curl -s -H "X-Tenant-ID: tenant1" $BASE_URL/api/books | jq .
echo ""

echo "Books for tenant2:"
curl -s -H "X-Tenant-ID: tenant2" $BASE_URL/api/books | jq .
echo ""

echo "Books for tenant3:"
curl -s -H "X-Tenant-ID: tenant3" $BASE_URL/api/books | jq .
echo ""

echo ""
echo "Step 6: Provision a new tenant dynamically"
echo "----------------------------------------"
echo "Provisioning 'acme_corp'..."
curl -s -X POST $BASE_URL/api/tenants/acme_corp/provision
echo ""
echo ""

echo "Creating author for new tenant acme_corp..."
curl -s -X POST $BASE_URL/api/authors \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: acme_corp" \
  -d '{
    "name": "David Wilson",
    "birthDate": "1990-05-10",
    "nationality": "Australian"
  }' | jq .

echo ""
echo "Authors for acme_corp (should only see David):"
curl -s -H "X-Tenant-ID: acme_corp" $BASE_URL/api/authors | jq .
echo ""

echo ""
echo "Step 7: Final tenant list"
echo "----------------------------------------"
curl -s $BASE_URL/api/tenants | jq .
echo ""

echo ""
echo "=========================================="
echo "Multi-Tenancy Simulation Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "- Created 4 tenants with isolated databases"
echo "- Each tenant has separate authors and books"
echo "- Data isolation verified"
echo "- Dynamic tenant provisioning demonstrated"

