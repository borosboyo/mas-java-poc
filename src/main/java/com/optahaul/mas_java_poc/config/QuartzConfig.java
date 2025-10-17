package com.optahaul.mas_java_poc.config;

import javax.sql.DataSource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.autoconfigure.quartz.QuartzTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.optahaul.mas_java_poc.job.ScheduledLoggingJob;

@Configuration
public class QuartzConfig {

	@Value("${job.scheduled-logging.cron}")
	private String scheduledLoggingCron;

	/**
	 * When multitenancy is enabled, use the catalog database for Quartz metadata
	 * This allows Quartz to function independently of tenant databases
	 */
	@Bean
	@QuartzDataSource
	@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
	public DataSource quartzDataSource(@Qualifier("catalogDataSource") DataSource catalogDataSource) {
		return catalogDataSource;
	}

	/**
	 * Use a separate transaction manager for Quartz to avoid tenant context issues
	 */
	@Bean
	@QuartzTransactionManager
	@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
	public PlatformTransactionManager quartzTransactionManager(
			@Qualifier("catalogDataSource") DataSource catalogDataSource) {
		return new DataSourceTransactionManager(catalogDataSource);
	}

	@Bean
	public JobDetail scheduledLoggingJobDetail() {
		return JobBuilder.newJob(ScheduledLoggingJob.class)
				.withIdentity("scheduledLoggingJob", "logging-jobs")
				.withDescription("Job that logs to console every minute")
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger scheduledLoggingJobTrigger(JobDetail scheduledLoggingJobDetail) {
		return TriggerBuilder.newTrigger()
				.forJob(scheduledLoggingJobDetail)
				.withIdentity("scheduledLoggingTrigger", "logging-triggers")
				.withDescription("Trigger that fires based on configured cron expression")
				.withSchedule(CronScheduleBuilder.cronSchedule(scheduledLoggingCron))
				.build();
	}
}
