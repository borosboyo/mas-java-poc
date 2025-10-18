package com.optahaul.mas_java_poc.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Single tenant configuration for non-multi-tenant environments (e.g., OpenAPI
 * generation, testing). Only active when multitenancy.enabled=false
 */
@Configuration
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "false", matchIfMissing = true)
public class SingleTenantConfig {

	/**
	 * DataSource properties configuration
	 */
	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * Simple single DataSource when multi-tenancy is disabled
	 */
	@Primary
	@Bean
	public DataSource dataSource(DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().build();
	}
}
