package com.optahaul.mas_java_poc.job;

import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Example tenant-aware job that could send daily reports This job runs
 * independently for each tenant with their own database context
 */
@Slf4j
@Component
public class TenantDailyReportJob extends TenantAwareJob {

	@Override
	protected void executeInternal(JobExecutionContext context, String tenantId) throws Exception {
		log.info("=== Executing Daily Report Job for Tenant: {} ===", tenantId);

		// Here you would:
		// 1. Query tenant's database for report data (tenant context is already set)
		// 2. Generate report
		// 3. Send email/notification to tenant users

		// Example: You could use repositories here and they will automatically
		// query the correct tenant database because TenantContext is set

		log.info("Daily report job completed for tenant: {}", tenantId);
	}
}
