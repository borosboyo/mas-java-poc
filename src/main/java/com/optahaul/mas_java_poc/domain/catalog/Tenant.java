package com.optahaul.mas_java_poc.domain.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Stored in the central catalog database Contains connection info for each
 * tenant's database
 */
@Entity
@Table(name = "tenants")
@Data
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", unique = true, nullable = false)
	private String tenantId; // e.g., "company1"

	@Column(name = "company_name", nullable = false)
	private String companyName;

	@Column(name = "subdomain", unique = true)
	private String subdomain; // e.g., "company1"

	@Column(name = "custom_domain")
	private String customDomain; // e.g., "app.company1.com"

	@Column(name = "db_url", nullable = false)
	private String dbUrl; // e.g., "jdbc:postgresql://localhost:5432/tenant_company1"

	@Column(name = "db_username", nullable = false)
	private String dbUsername;

	@Column(name = "db_password", nullable = false)
	private String dbPassword; // In production, use encrypted secrets

	@Column(name = "status", nullable = false)
	private String status; // ACTIVE, SUSPENDED, TRIAL

	@Column(name = "db_schema")
	private String dbSchema; // For schema-based isolation (alternative approach)
}
