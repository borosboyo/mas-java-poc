package com.optahaul.mas_java_poc.job;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BackgroundJobService {

	@Async("taskExecutor")
	public void sendEmail(String to, String subject, String body) {
		log.info("Sending email to: {}", to);
		try {
			// Simulate email sending
			Thread.sleep(1000);
			log.info("Email sent successfully to: {} with subject: {}", to, subject);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Failed to send email to: {}", to, e);
		}
	}

	@Async("taskExecutor")
	public void callWebhook(String url, String payload) {
		log.info("Calling webhook: {}", url);
		try {
			// Simulate webhook call
			Thread.sleep(500);
			log.info("Webhook called successfully: {} with payload: {}", url, payload);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Failed to call webhook: {}", url, e);
		}
	}

	@Async("taskExecutor")
	public void processNotification(String userId, String message) {
		log.info("Processing notification for user: {}", userId);
		try {
			// Simulate notification processing
			Thread.sleep(300);
			log.info("Notification processed for user: {} with message: {}", userId, message);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Failed to process notification for user: {}", userId, e);
		}
	}
}
