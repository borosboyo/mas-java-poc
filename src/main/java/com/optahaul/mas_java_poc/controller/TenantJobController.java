package com.optahaul.mas_java_poc.controller;

import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.job.TenantDailyReportJob;
import com.optahaul.mas_java_poc.multitenancy.TenantContext;
import com.optahaul.mas_java_poc.service.TenantJobSchedulerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing tenant-specific scheduled jobs Each tenant can
 * schedule their own jobs that run against their database
 */
@RestController
@RequestMapping("/api/tenant/jobs")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
@Tag(name = "Tenant Jobs", description = "Manage tenant-specific scheduled jobs")
public class TenantJobController {

	private final TenantJobSchedulerService jobSchedulerService;

	@PostMapping("/daily-report")
	@Operation(summary = "Schedule daily report job for current tenant")
	public ResponseEntity<String> scheduleDailyReport(@RequestBody ScheduleJobRequest request) {
		String tenantId = TenantContext.getCurrentTenant();

		try {
			jobSchedulerService.scheduleJobForTenant(
					tenantId,
					TenantDailyReportJob.class,
					"daily-report",
					request.getCronExpression());
			return ResponseEntity.ok("Daily report job scheduled successfully for tenant: " + tenantId);
		} catch (SchedulerException e) {
			return ResponseEntity.internalServerError()
					.body("Failed to schedule job: " + e.getMessage());
		}
	}

	@DeleteMapping("/daily-report")
	@Operation(summary = "Unschedule daily report job for current tenant")
	public ResponseEntity<String> unscheduleDailyReport() {
		String tenantId = TenantContext.getCurrentTenant();

		try {
			jobSchedulerService.unscheduleJobForTenant(tenantId, "daily-report");
			return ResponseEntity.ok("Daily report job unscheduled for tenant: " + tenantId);
		} catch (SchedulerException e) {
			return ResponseEntity.internalServerError()
					.body("Failed to unschedule job: " + e.getMessage());
		}
	}

	@PostMapping("/pause")
	@Operation(summary = "Pause all jobs for current tenant")
	public ResponseEntity<String> pauseJobs() {
		String tenantId = TenantContext.getCurrentTenant();

		try {
			jobSchedulerService.pauseJobsForTenant(tenantId);
			return ResponseEntity.ok("All jobs paused for tenant: " + tenantId);
		} catch (SchedulerException e) {
			return ResponseEntity.internalServerError()
					.body("Failed to pause jobs: " + e.getMessage());
		}
	}

	@PostMapping("/resume")
	@Operation(summary = "Resume all jobs for current tenant")
	public ResponseEntity<String> resumeJobs() {
		String tenantId = TenantContext.getCurrentTenant();

		try {
			jobSchedulerService.resumeJobsForTenant(tenantId);
			return ResponseEntity.ok("All jobs resumed for tenant: " + tenantId);
		} catch (SchedulerException e) {
			return ResponseEntity.internalServerError()
					.body("Failed to resume jobs: " + e.getMessage());
		}
	}

	@DeleteMapping
	@Operation(summary = "Delete all scheduled jobs for current tenant")
	public ResponseEntity<String> deleteAllJobs() {
		String tenantId = TenantContext.getCurrentTenant();

		try {
			jobSchedulerService.unscheduleAllJobsForTenant(tenantId);
			return ResponseEntity.ok("All jobs deleted for tenant: " + tenantId);
		} catch (SchedulerException e) {
			return ResponseEntity.internalServerError()
					.body("Failed to delete jobs: " + e.getMessage());
		}
	}

	@Data
	public static class ScheduleJobRequest {
		private String cronExpression = "0 0 9 * * ?"; // Default: 9 AM daily
	}
}
