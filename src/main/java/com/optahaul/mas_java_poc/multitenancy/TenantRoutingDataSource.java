package com.optahaul.mas_java_poc.multitenancy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.optahaul.mas_java_poc.domain.catalog.Tenant;
import com.optahaul.mas_java_poc.service.TenantService;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Routes database queries to the appropriate tenant database Creates and caches
 * DataSource connections per tenant
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

	private final Map<Object, DataSource> tenantDataSources = new ConcurrentHashMap<>();
	private final TenantService tenantService;

	public TenantRoutingDataSource(TenantService tenantService) {
		this.tenantService = tenantService;
		// Initialize with empty map to satisfy Spring's requirement
		setTargetDataSources(new HashMap<>());
		// Set lenient fallback to allow dynamic datasource resolution
		setLenientFallback(false);
		// Initialize the datasource
		afterPropertiesSet();
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getCurrentTenant();
	}

	@Override
	protected DataSource determineTargetDataSource() {
		String tenantId = TenantContext.getCurrentTenant();

		if (tenantId == null) {
			throw new IllegalStateException("No tenant context set");
		}

		// Check cache first
		DataSource dataSource = tenantDataSources.get(tenantId);
		if (dataSource == null) {
			dataSource = createTenantDataSource(tenantId);
			tenantDataSources.put(tenantId, dataSource);
		}

		return dataSource;
	}

	private DataSource createTenantDataSource(String tenantId) {
		Tenant tenant = tenantService.findByTenantId(tenantId);

		if (tenant == null || !"ACTIVE".equals(tenant.getStatus())) {
			throw new IllegalStateException("Tenant not found or inactive: " + tenantId);
		}

		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(tenant.getDbUrl());
		ds.setUsername(tenant.getDbUsername());
		ds.setPassword(tenant.getDbPassword());
		ds.setMaximumPoolSize(10);
		ds.setMinimumIdle(2);
		ds.setConnectionTimeout(30000);

		return ds;
	}

	// Call this when tenant is deleted/suspended
	public void removeTenantDataSource(String tenantId) {
		DataSource ds = tenantDataSources.remove(tenantId);
		if (ds instanceof HikariDataSource) {
			((HikariDataSource) ds).close();
		}
	}
}
