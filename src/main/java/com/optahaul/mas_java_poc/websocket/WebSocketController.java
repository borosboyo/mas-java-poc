package com.optahaul.mas_java_poc.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/notification")
	@SendTo("/topic/notifications")
	public NotificationMessage sendNotification(NotificationMessage message) {
		log.info("Received notification: {}", message);
		return message;
	}

	public void sendUpdateNotification(String message) {
		NotificationMessage notification = new NotificationMessage("system", message);
		messagingTemplate.convertAndSend("/topic/notifications", notification);
		log.info("Sent update notification: {}", message);
	}

	public record NotificationMessage(String sender, String content) {
	}
}
