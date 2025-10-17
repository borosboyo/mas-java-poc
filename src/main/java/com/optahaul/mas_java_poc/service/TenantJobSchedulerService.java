package com.optahaul.mas_java_poc.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.optahaul.mas_java_poc.job.TenantAwareJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for scheduling tenant-specific Quartz jobs Each tenant can have their
 * own scheduled jobs with isolated execution
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public class TenantJobSchedulerService {

	private final Scheduler scheduler;

	/**
	 * Schedule a job for a specific tenant with a cron expression
	 */
	public void scheduleJobForTenant(
			String tenantId,
			Class<? extends TenantAwareJob> jobClass,
			String jobName,
			String cronExpression) throws SchedulerException {

		JobKey jobKey = new JobKey(jobName, "tenant-" + tenantId);
		TriggerKey triggerKey = new TriggerKey(jobName + "-trigger", "tenant-" + tenantId);

		// Check if job already exists
		if (scheduler.checkExists(jobKey)) {
			log.info("Job {} already exists for tenant {}. Updating schedule.", jobName, tenantId);
			scheduler.deleteJob(jobKey);
		}

		// Create job data map with tenant ID
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(TenantAwareJob.TENANT_ID_KEY, tenantId);

		// Build job detail
		JobDetail jobDetail = JobBuilder.newJob(jobClass)
				.withIdentity(jobKey)
				.withDescription("Tenant-specific job: " + jobName + " for tenant: " + tenantId)
				.usingJobData(jobDataMap)
				.storeDurably(false)
				.build();

		// Build trigger with cron expression
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerKey)
				.forJob(jobDetail)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
				.build();

		// Schedule the job
		scheduler.scheduleJob(jobDetail, trigger);
		log.info("Scheduled job {} for tenant {} with cron expression: {}",
				jobName, tenantId, cronExpression);
	}

	/**
	 * Unschedule a job for a specific tenant
	 */
	public void unscheduleJobForTenant(String tenantId, String jobName) throws SchedulerException {
		JobKey jobKey = new JobKey(jobName, "tenant-" + tenantId);

		if (scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
			log.info("Unscheduled job {} for tenant {}", jobName, tenantId);
		}
	}

	/**
	 * Unschedule all jobs for a tenant (e.g., when tenant is suspended)
	 */
	public void unscheduleAllJobsForTenant(String tenantId) throws SchedulerException {
		String groupName = "tenant-" + tenantId;
		var jobKeys = scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.groupEquals(groupName));

		for (JobKey jobKey : jobKeys) {
			scheduler.deleteJob(jobKey);
			log.info("Unscheduled job {} for tenant {}", jobKey.getName(), tenantId);
		}
	}

	/**
	 * Pause all jobs for a tenant
	 */
	public void pauseJobsForTenant(String tenantId) throws SchedulerException {
		String groupName = "tenant-" + tenantId;
		scheduler.pauseJobs(org.quartz.impl.matchers.GroupMatcher.groupEquals(groupName));
		log.info("Paused all jobs for tenant {}", tenantId);
	}

	/**
	 * Resume all jobs for a tenant
	 */
	public void resumeJobsForTenant(String tenantId) throws SchedulerException {
		String groupName = "tenant-" + tenantId;
		scheduler.resumeJobs(org.quartz.impl.matchers.GroupMatcher.groupEquals(groupName));
		log.info("Resumed all jobs for tenant {}", tenantId);
	}
}
