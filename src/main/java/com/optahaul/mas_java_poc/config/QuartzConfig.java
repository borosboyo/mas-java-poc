package com.optahaul.mas_java_poc.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.optahaul.mas_java_poc.job.ScheduledLoggingJob;

@Configuration
public class QuartzConfig {

	@Value("${job.scheduled-logging.cron}")
	private String scheduledLoggingCron;

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
