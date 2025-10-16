package com.optahaul.mas_java_poc.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledLoggingJob implements Job {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String currentTime = LocalDateTime.now().format(FORMATTER);
		log.info("=== Scheduled Job Executed at {} ===", currentTime);
		log.info("Job Name: {}", context.getJobDetail().getKey().getName());
		log.info("Job Group: {}", context.getJobDetail().getKey().getGroup());
		log.info("Trigger Name: {}", context.getTrigger().getKey().getName());
		log.info("Next Fire Time: {}", context.getNextFireTime());
		log.info("===========================================");
	}
}
