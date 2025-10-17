package com.optahaul.mas_java_poc.multitenancy;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.optahaul.mas_java_poc.service.TenantService;

/**
 * Main configuration for multi-tenancy setup Only active when
 * multitenancy.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public class MultiTenancyConfig {

	/**
	 * Catalog database - stores tenant metadata
	 */
	@Bean(name = "catalogDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.catalog")
	public DataSource catalogDataSource() {
		return DataSourceBuilder.create().build();
	}

	/**
	 * EntityManager for catalog database
	 */
	@Bean(name = "catalogEntityManager")
	public LocalContainerEntityManagerFactoryBean catalogEntityManager(
			@Qualifier("catalogDataSource") DataSource dataSource) {

		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		// Only scan for Tenant entity in catalog database
		em.setPackagesToScan("com.optahaul.mas_java_poc.domain.catalog");
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		HashMap<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", "update");
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		em.setJpaPropertyMap(properties);

		return em;
	}

	@Bean(name = "catalogTransactionManager")
	public PlatformTransactionManager catalogTransactionManager(
			@Qualifier("catalogEntityManager") LocalContainerEntityManagerFactoryBean emf) {
		return new JpaTransactionManager(emf.getObject());
	}

	/**
	 * Primary DataSource - routes to tenant databases
	 */
	@Primary
	@Bean(name = "tenantDataSource")
	public DataSource tenantDataSource(@Lazy TenantService tenantService) {
		return new TenantRoutingDataSource(tenantService);
	}

	/**
	 * EntityManager for tenant databases
	 */
	@Primary
	@Bean(name = "tenantEntityManager")
	public LocalContainerEntityManagerFactoryBean tenantEntityManager(
			@Qualifier("tenantDataSource") DataSource dataSource) {

		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("com.optahaul.mas_java_poc.domain"); // Business entities
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		// Defer bootstrap to prevent connection attempts during startup
		em.setBootstrapExecutor(null);

		HashMap<String, Object> properties = new HashMap<>();
		// Disable automatic schema management for tenant databases
		// Schema should be managed per-tenant through Flyway or during tenant creation
		properties.put("hibernate.hbm2ddl.auto", "none");
		properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		properties.put("hibernate.multiTenancy", "DATABASE");
		// Disable schema validation at startup
		properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
		em.setJpaPropertyMap(properties);

		return em;
	}

	@Primary
	@Bean(name = "tenantTransactionManager")
	public PlatformTransactionManager tenantTransactionManager(
			@Qualifier("tenantEntityManager") LocalContainerEntityManagerFactoryBean emf) {
		return new JpaTransactionManager(emf.getObject());
	}

	/**
	 * JPA Repository configuration for catalog database (TenantRepository)
	 */
	@Configuration
	@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
	@EnableJpaRepositories(
			basePackages = "com.optahaul.mas_java_poc.multitenancy",
			entityManagerFactoryRef = "catalogEntityManager",
			transactionManagerRef = "catalogTransactionManager")
	static class CatalogRepositoryConfig {
	}

	/**
	 * JPA Repository configuration for tenant databases (application repositories)
	 */
	@Configuration
	@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
	@EnableJpaRepositories(
			basePackages = "com.optahaul.mas_java_poc.repository",
			entityManagerFactoryRef = "tenantEntityManager",
			transactionManagerRef = "tenantTransactionManager")
	static class TenantRepositoryConfig {
	}
}
