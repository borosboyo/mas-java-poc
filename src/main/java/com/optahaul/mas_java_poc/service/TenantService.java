package com.optahaul.mas_java_poc.service;

import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optahaul.mas_java_poc.domain.catalog.Tenant;
import com.optahaul.mas_java_poc.multitenancy.TenantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing tenant metadata Uses catalog database (not
 * tenant-specific) Only active when multitenancy.enabled=true
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public class TenantService {

	private final TenantRepository tenantRepository;
	private final TenantJobSchedulerService jobSchedulerService;

	@Cacheable(value = "tenants", key = "#tenantId")
	@Transactional(transactionManager = "catalogTransactionManager", readOnly = true)
	public Tenant findByTenantId(String tenantId) {
		return tenantRepository.findByTenantId(tenantId)
				.orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
	}

	@Cacheable(value = "tenantsBySubdomain", key = "#subdomain")
	@Transactional(transactionManager = "catalogTransactionManager", readOnly = true)
	public Tenant findBySubdomain(String subdomain) {
		return tenantRepository.findBySubdomain(subdomain)
				.orElseThrow(() -> new TenantNotFoundException("Tenant not found for subdomain: " + subdomain));
	}

	@Transactional(transactionManager = "catalogTransactionManager")
	public Tenant createTenant(String tenantId, String companyName, String subdomain) {
		// 1. Create entry in catalog
		Tenant tenant = new Tenant();
		tenant.setTenantId(tenantId);
		tenant.setCompanyName(companyName);
		tenant.setSubdomain(subdomain);
		tenant.setDbUrl("jdbc:postgresql://localhost:5432/tenant_" + tenantId);
		tenant.setDbUsername("tenant_user");
		tenant.setDbPassword("secure_password"); // Use secrets manager in production
		tenant.setStatus("ACTIVE");

		tenant = tenantRepository.save(tenant);

		// 2. Create physical database (you'd use Flyway/Liquibase for this)
		// createTenantDatabase(tenantId);

		// 3. Run migrations on new tenant database
		// runTenantMigrations(tenantId);

		log.info("Tenant created successfully: {}", tenantId);

		return tenant;
	}

	@Transactional(transactionManager = "catalogTransactionManager")
	public void suspendTenant(String tenantId) {
		Tenant tenant = findByTenantId(tenantId);
		tenant.setStatus("SUSPENDED");
		tenantRepository.save(tenant);

		// Pause all scheduled jobs for this tenant
		try {
			jobSchedulerService.pauseJobsForTenant(tenantId);
			log.info("Paused all jobs for suspended tenant: {}", tenantId);
		} catch (SchedulerException e) {
			log.error("Failed to pause jobs for tenant {}: {}", tenantId, e.getMessage());
		}

		// Clear cache and close connections
		// cacheManager.getCache("tenants").evict(tenantId);
	}

	@Transactional(transactionManager = "catalogTransactionManager")
	public void activateTenant(String tenantId) {
		Tenant tenant = findByTenantId(tenantId);
		tenant.setStatus("ACTIVE");
		tenantRepository.save(tenant);

		// Resume all scheduled jobs for this tenant
		try {
			jobSchedulerService.resumeJobsForTenant(tenantId);
			log.info("Resumed all jobs for activated tenant: {}", tenantId);
		} catch (SchedulerException e) {
			log.error("Failed to resume jobs for tenant {}: {}", tenantId, e.getMessage());
		}
	}

	@Transactional(transactionManager = "catalogTransactionManager")
	public void deleteTenant(String tenantId) {
		// Unschedule all jobs for this tenant
		try {
			jobSchedulerService.unscheduleAllJobsForTenant(tenantId);
			log.info("Unscheduled all jobs for deleted tenant: {}", tenantId);
		} catch (SchedulerException e) {
			log.error("Failed to unschedule jobs for tenant {}: {}", tenantId, e.getMessage());
		}

		// Delete tenant from catalog
		Tenant tenant = findByTenantId(tenantId);
		tenantRepository.delete(tenant);

		// Clear cache
		// cacheManager.getCache("tenants").evict(tenantId);

		log.info("Tenant deleted: {}", tenantId);
	}
}
