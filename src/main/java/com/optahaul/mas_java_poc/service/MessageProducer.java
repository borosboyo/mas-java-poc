package com.optahaul.mas_java_poc.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.optahaul.mas_java_poc.config.RabbitMQConfig;
import com.optahaul.mas_java_poc.dto.NotificationMessage;

import lombok.RequiredArgsConstructor;

/**
 * Service for producing messages to RabbitMQ.
 */
@Service
@RequiredArgsConstructor
@Profile("!openapi")
public class MessageProducer {

	private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

	private final RabbitTemplate rabbitTemplate;

	/**
	 * Sends a notification message to the RabbitMQ queue.
	 *
	 * @param message
	 *            the message content
	 * @param recipient
	 *            the recipient of the notification
	 * @param type
	 *            the type of notification
	 */
	public void sendNotification(String message, String recipient, NotificationMessage.NotificationType type) {
		NotificationMessage notification = new NotificationMessage(UUID.randomUUID().toString(), message, recipient,
				LocalDateTime.now(), type);

		logger.info("Sending notification to RabbitMQ: {}", notification);

		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, notification);

		logger.info("Notification sent successfully");
	}

}
