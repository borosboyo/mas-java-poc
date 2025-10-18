package com.optahaul.mas_java_poc.multitenancy;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.optahaul.mas_java_poc.domain.catalog.Tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically runs Flyway migrations on all tenant databases at application
 * startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
@Order(2) // Run after CatalogFlywayMigrator
public class TenantFlywayMigrator {

	private final TenantRepository tenantRepository;

	@EventListener(ApplicationReadyEvent.class)
	public void migrateTenants() {
		log.info("Starting Flyway migrations for all tenant databases...");

		Iterable<Tenant> tenants = tenantRepository.findAll();
		int successCount = 0;
		int failCount = 0;

		for (Tenant tenant : tenants) {
			try {
				log.info("Running migrations for tenant: {} ({})", tenant.getTenantId(), tenant.getCompanyName());
				migrateTenantDatabase(tenant);
				successCount++;
			} catch (Exception e) {
				log.error("Failed to migrate tenant database for {}: {}", tenant.getTenantId(), e.getMessage(), e);
				failCount++;
			}
		}

		log.info("Flyway migration completed. Success: {}, Failed: {}", successCount, failCount);
	}

	private void migrateTenantDatabase(Tenant tenant) {
		// Create a direct datasource for this tenant
		DataSource tenantDataSource = createTenantDataSource(tenant);

		// Configure and run Flyway
		Flyway flyway = Flyway.configure()
				.dataSource(tenantDataSource)
				.locations("classpath:db/migration")
				.baselineOnMigrate(true)
				.baselineVersion("0")
				.load();

		// Run migration
		int migrationsApplied = flyway.migrate().migrationsExecuted;
		log.info("Applied {} migration(s) for tenant: {}", migrationsApplied, tenant.getTenantId());
	}

	private DataSource createTenantDataSource(Tenant tenant) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(tenant.getDbUrl());
		dataSource.setUsername(tenant.getDbUsername());
		dataSource.setPassword(tenant.getDbPassword());
		dataSource.setDriverClassName("org.postgresql.Driver");
		return dataSource;
	}
}
