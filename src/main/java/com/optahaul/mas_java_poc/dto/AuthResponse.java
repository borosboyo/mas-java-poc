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

	@Builder.Default
	private String type = "Bearer";

	private String username;

	private String role;

	private String tenantId;

	public AuthResponse(String token, String username, String role, String tenantId) {
		this.token = token;
		this.username = username;
		this.role = role;
		this.tenantId = tenantId;
	}
}
