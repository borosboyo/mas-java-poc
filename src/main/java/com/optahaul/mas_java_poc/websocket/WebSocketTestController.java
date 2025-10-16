package com.optahaul.mas_java_poc.websocket;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Tag(name = "WebSocket", description = "WebSocket demonstration endpoints")
@Slf4j
class WebSocketTestController {

	private final WebSocketController webSocketController;

	@PostMapping("/api/test/websocket")
	public String testWebSocket(@RequestBody String message) {
		webSocketController.sendUpdateNotification(message);
		return "Notification sent: " + message;
	}
}
