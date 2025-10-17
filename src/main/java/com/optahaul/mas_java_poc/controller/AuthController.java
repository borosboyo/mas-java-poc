package com.optahaul.mas_java_poc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.dto.AuthResponse;
import com.optahaul.mas_java_poc.dto.LoginRequest;
import com.optahaul.mas_java_poc.multitenancy.TenantContext;
import com.optahaul.mas_java_poc.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider tokenProvider;

	@PostMapping("/login")
	@Operation(summary = "Login user", description = "Authenticate user and return JWT token")
	public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String role = userDetails.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority)
				.orElse("ROLE_USER");

		// Get tenant ID from context to include in response
		String tenantId = TenantContext.getCurrentTenant();

		return ResponseEntity
				.ok(new AuthResponse(jwt, userDetails.getUsername(), role.replace("ROLE_", ""), tenantId));
	}
}
