package com.optahaul.mas_java_poc.multitenancy;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Runs Flyway migrations on the catalog database at application startup This
 * must run BEFORE TenantFlywayMigrator since tenants are loaded from the
 * catalog
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
@Order(1) // Run before TenantFlywayMigrator
public class CatalogFlywayMigrator {

	@Value("${spring.datasource.catalog.jdbc-url}")
	private String catalogUrl;

	@Value("${spring.datasource.catalog.username}")
	private String catalogUsername;

	@Value("${spring.datasource.catalog.password}")
	private String catalogPassword;

	@EventListener(ApplicationReadyEvent.class)
	public void migrateCatalog() {
		log.info("Starting Flyway migration for catalog database...");

		try {
			// Create a direct datasource to avoid routing issues
			DataSource catalogDataSource = DataSourceBuilder.create()
					.url(catalogUrl)
					.username(catalogUsername)
					.password(catalogPassword)
					.driverClassName("org.postgresql.Driver")
					.build();

			Flyway flyway = Flyway.configure()
					.dataSource(catalogDataSource)
					.locations("classpath:db/catalog/migration")
					.baselineOnMigrate(true)
					.baselineVersion("0")
					.table("flyway_catalog_schema_history") // Different table name to avoid conflicts
					.load();

			int migrationsApplied = flyway.migrate().migrationsExecuted;
			log.info("Applied {} migration(s) to catalog database", migrationsApplied);
		} catch (Exception e) {
			log.error("Failed to migrate catalog database: {}", e.getMessage(), e);
			throw new RuntimeException("Catalog database migration failed", e);
		}
	}
}
