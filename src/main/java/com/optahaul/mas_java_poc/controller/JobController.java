package com.optahaul.mas_java_poc.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.job.BackgroundJobService;
import com.optahaul.mas_java_poc.job.LongRunningJobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Background and long-running job management")
public class JobController {

	private final LongRunningJobService longRunningJobService;
	private final BackgroundJobService backgroundJobService;
	private final AtomicLong jobIdGenerator = new AtomicLong(1);

	@PostMapping("/compute-intensive")
	@Operation(summary = "Start compute-intensive job", description = "Starts a compute-intensive long-running job")
	public ResponseEntity<Map<String, Object>> startComputeIntensiveJob() {
		Long jobId = jobIdGenerator.getAndIncrement();
		CompletableFuture<String> future = longRunningJobService.executeComputeIntensiveJob(jobId);

		Map<String, Object> response = new HashMap<>();
		response.put("jobId", jobId);
		response.put("status", "started");
		response.put("message", "Job submitted successfully");

		return ResponseEntity.accepted().body(response);
	}

	@PostMapping("/data-processing")
	@Operation(summary = "Start data processing job", description = "Starts a data processing job")
	public ResponseEntity<Map<String, Object>> startDataProcessingJob(@RequestParam String dataId) {
		CompletableFuture<String> future = longRunningJobService.executeDataProcessingJob(dataId);

		Map<String, Object> response = new HashMap<>();
		response.put("dataId", dataId);
		response.put("status", "started");
		response.put("message", "Data processing job submitted");

		return ResponseEntity.accepted().body(response);
	}

	@PostMapping("/send-email")
	@Operation(summary = "Send email", description = "Sends an email in the background")
	public ResponseEntity<Map<String, String>> sendEmail(@RequestParam String to, @RequestParam String subject,
			@RequestParam String body) {
		backgroundJobService.sendEmail(to, subject, body);

		Map<String, String> response = new HashMap<>();
		response.put("status", "submitted");
		response.put("message", "Email queued for sending");

		return ResponseEntity.accepted().body(response);
	}

	@PostMapping("/webhook")
	@Operation(summary = "Call webhook", description = "Calls a webhook in the background")
	public ResponseEntity<Map<String, String>> callWebhook(@RequestParam String url, @RequestParam String payload) {
		backgroundJobService.callWebhook(url, payload);

		Map<String, String> response = new HashMap<>();
		response.put("status", "submitted");
		response.put("message", "Webhook call queued");

		return ResponseEntity.accepted().body(response);
	}

	@PostMapping("/notification")
	@Operation(summary = "Process notification", description = "Processes a notification in the background")
	public ResponseEntity<Map<String, String>> processNotification(@RequestParam String userId,
			@RequestParam String message) {
		backgroundJobService.processNotification(userId, message);

		Map<String, String> response = new HashMap<>();
		response.put("status", "submitted");
		response.put("message", "Notification queued for processing");

		return ResponseEntity.accepted().body(response);
	}
}
