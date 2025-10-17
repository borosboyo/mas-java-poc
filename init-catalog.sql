-- Initialize catalog database with tenant metadata
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(255) UNIQUE,
    custom_domain VARCHAR(255),
    db_url VARCHAR(512) NOT NULL,
    db_username VARCHAR(255) NOT NULL,
    db_password VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    db_schema VARCHAR(255)
);

-- Insert tenant 1 (Company 1) - using Docker service name
INSERT INTO tenants (tenant_id, company_name, subdomain, db_url, db_username, db_password, status)
VALUES ('company1', 'Company One Inc', 'company1', 'jdbc:postgresql://tenant1-postgres:5432/tenant_company1', 'myuser', 'secret', 'ACTIVE')
ON CONFLICT (tenant_id) DO UPDATE SET
    company_name = EXCLUDED.company_name,
    subdomain = EXCLUDED.subdomain,
    db_url = EXCLUDED.db_url,
    status = EXCLUDED.status;

-- Insert tenant 2 (Company 2) - using Docker service name
INSERT INTO tenants (tenant_id, company_name, subdomain, db_url, db_username, db_password, status)
VALUES ('company2', 'Company Two LLC', 'company2', 'jdbc:postgresql://tenant2-postgres:5432/tenant_company2', 'myuser', 'secret', 'ACTIVE')
ON CONFLICT (tenant_id) DO UPDATE SET
    company_name = EXCLUDED.company_name,
    subdomain = EXCLUDED.subdomain,
    db_url = EXCLUDED.db_url,
    status = EXCLUDED.status;

