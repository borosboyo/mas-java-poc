package com.optahaul.mas_java_poc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.info(new Info().title("MAS Java PoC API").version("1.0")
						.description(
								"Comprehensive Spring Boot application with CRUD operations, multi-tenancy, WebSocket, " + "background jobs, and role-based access control")
						.contact(new Contact().name("API Support").email("support@example.com")).license(new License()
								.name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html")));
	}
}
