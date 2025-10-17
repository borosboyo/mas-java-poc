package com.optahaul.mas_java_poc.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.optahaul.mas_java_poc.multitenancy.TenantContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Base class for all tenant-aware Quartz jobs Automatically sets tenant context
 * before job execution
 */
@Slf4j
public abstract class TenantAwareJob implements Job {

	public static final String TENANT_ID_KEY = "tenantId";

	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException {
		String tenantId = context.getJobDetail().getJobDataMap().getString(TENANT_ID_KEY);

		if (tenantId == null) {
			log.error("No tenant ID found in job data. Skipping job execution.");
			return;
		}

		try {
			// Set tenant context
			TenantContext.setCurrentTenant(tenantId);
			log.debug("Executing job {} for tenant {}", context.getJobDetail().getKey(), tenantId);

			// Execute tenant-specific job logic
			executeInternal(context, tenantId);

		} catch (Exception e) {
			log.error("Error executing job for tenant {}: {}", tenantId, e.getMessage(), e);
			throw new JobExecutionException(e);
		} finally {
			// Always clear tenant context after job execution
			TenantContext.clear();
		}
	}

	/**
	 * Implement this method with tenant-specific job logic Tenant context is
	 * already set when this method is called
	 */
	protected abstract void executeInternal(JobExecutionContext context, String tenantId) throws Exception;
}
