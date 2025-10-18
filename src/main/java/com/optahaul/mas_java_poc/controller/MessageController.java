package com.optahaul.mas_java_poc.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.dto.NotificationMessage;
import com.optahaul.mas_java_poc.service.MessageProducer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for testing RabbitMQ message sending.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "RabbitMQ message testing API")
@Profile("!openapi")
public class MessageController {

	private final MessageProducer messageProducer;

	/**
	 * Sends a notification message to RabbitMQ.
	 *
	 * @param request
	 *            the notification request
	 * @return ResponseEntity with success message
	 */
	@PostMapping("/send")
	@Operation(summary = "Send a notification message", description = "Sends a notification message to RabbitMQ queue")
	public ResponseEntity<String> sendMessage(@RequestBody NotificationRequest request) {
		messageProducer.sendNotification(request.message, request.recipient, request.type);
		return ResponseEntity.ok("Message sent to RabbitMQ successfully!");
	}

	/**
	 * DTO for notification request.
	 */
	public record NotificationRequest(String message, String recipient, NotificationMessage.NotificationType type) {
	}

}
