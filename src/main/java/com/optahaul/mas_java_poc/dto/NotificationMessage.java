package com.optahaul.mas_java_poc.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification messages sent through RabbitMQ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

	private String id;

	private String message;

	private String recipient;

	private LocalDateTime timestamp;

	private NotificationType type;

	/**
	 * Enum for notification types.
	 */
	public enum NotificationType {

		INFO, WARNING, ERROR, SUCCESS

	}

}
