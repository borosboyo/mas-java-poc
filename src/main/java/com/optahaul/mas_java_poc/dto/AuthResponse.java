package com.optahaul.mas_java_poc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

	private String token;

	private String type = "Bearer";

	private String username;

	private String role;

	public AuthResponse(String token, String username, String role) {
		this.token = token;
		this.username = username;
		this.role = role;
	}
}
