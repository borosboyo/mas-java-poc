package com.optahaul.mas_java_poc.multitenancy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Intercepts every request to identify and set the tenant Extracts tenant from
 * subdomain (e.g., company1.yourapp.com) Only active when
 * multitenancy.enabled=true
 */
@Component
@ConditionalOnProperty(name = "multitenancy.enabled", havingValue = "true")
public class TenantInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {

		String host = request.getServerName();
		String tenantId = extractTenantFromHost(host);

		if (tenantId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Cannot identify tenant");
			return false;
		}

		TenantContext.setCurrentTenant(tenantId);
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			Exception ex) throws Exception {
		TenantContext.clear();
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
