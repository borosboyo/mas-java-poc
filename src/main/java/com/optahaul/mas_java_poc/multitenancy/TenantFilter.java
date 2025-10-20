package com.optahaul.mas_java_poc.multitenancy;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter that sets tenant context BEFORE Spring Security filter chain This
 * ensures tenant context is available during JWT authentication
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Execute first, before any other filter
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public class TenantFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			// Allow access to OpenAPI/Swagger endpoints without tenant resolution
			String uri = request.getRequestURI();
			if (uri != null &&
					(uri.startsWith("/api-docs") ||
							uri.startsWith("/swagger") ||
							uri.startsWith("/v3/api-docs") ||
							uri.startsWith("/swagger-ui") ||
							uri.equals("/openapi.json") ||
							uri.startsWith("/ws") ||
							uri.startsWith("/app/") ||
							uri.startsWith("/topic/") ||
							uri.startsWith("/queue/"))) {
				filterChain.doFilter(request, response);
				return;
			}

			String host = request.getServerName();
			String tenantId = extractTenantFromHost(host);

			if (tenantId == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot identify tenant");
				return;
			}

			TenantContext.setCurrentTenant(tenantId);
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}

	private String extractTenantFromHost(String host) {
		// Example: company1.optahaul.com -> company1
		if (host.contains(".optahaul.com")) {
			return host.split("\\.")[0];
		}

		// For custom domains, you'd query the catalog database
		// to map custom domain -> tenant
		return null;
	}
}
