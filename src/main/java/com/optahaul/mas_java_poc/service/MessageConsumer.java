package com.optahaul.mas_java_poc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.optahaul.mas_java_poc.config.RabbitMQConfig;
import com.optahaul.mas_java_poc.dto.NotificationMessage;

/**
 * Service for consuming messages from RabbitMQ.
 */
@Service
public class MessageConsumer {

	private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

	/**
	 * Listens to the RabbitMQ queue and processes incoming notification messages.
	 *
	 * @param notification
	 *            the received notification message
	 */
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void receiveNotification(NotificationMessage notification) {
		logger.info("Received notification from RabbitMQ: {}", notification);

		// Process the notification here
		switch (notification.getType()) {
		case INFO:
			logger.info("Processing INFO notification: {} for {}", notification.getMessage(),
					notification.getRecipient());
			break;
		case WARNING:
			logger.warn("Processing WARNING notification: {} for {}", notification.getMessage(),
					notification.getRecipient());
			break;
		case ERROR:
			logger.error("Processing ERROR notification: {} for {}", notification.getMessage(),
					notification.getRecipient());
			break;
		case SUCCESS:
			logger.info("Processing SUCCESS notification: {} for {}", notification.getMessage(),
					notification.getRecipient());
			break;
		}

		logger.info("Notification processed successfully");
	}

}
