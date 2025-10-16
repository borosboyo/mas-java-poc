package com.optahaul.mas_java_poc.job;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LongRunningJobService {

	@Async("longRunningTaskExecutor")
	public CompletableFuture<String> executeComputeIntensiveJob(Long jobId) {
		log.info("Starting compute-intensive job: {}", jobId);
		long startTime = System.currentTimeMillis();

		try {
			// Simulate compute-intensive work
			int duration = new Random().nextInt(10) + 1; // 1-10 seconds
			for (int i = 0; i < duration; i++) {
				Thread.sleep(1000);
				log.debug("Job {} progress: {}%", jobId, (i + 1) * 100 / duration);
			}

			long endTime = System.currentTimeMillis();
			String result = String.format("Job %d completed in %d ms", jobId, (endTime - startTime));
			log.info(result);
			return CompletableFuture.completedFuture(result);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Job {} was interrupted", jobId, e);
			return CompletableFuture.failedFuture(e);
		}
	}

	@Async("longRunningTaskExecutor")
	public CompletableFuture<String> executeDataProcessingJob(String dataId) {
		log.info("Starting data processing job for: {}", dataId);

		try {
			// Simulate data processing
			Thread.sleep(2000);
			String result = "Processed data: " + dataId;
			log.info("Data processing completed for: {}", dataId);
			return CompletableFuture.completedFuture(result);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Data processing job failed for: {}", dataId, e);
			return CompletableFuture.failedFuture(e);
		}
	}
}
