package com.optahaul.mas_java_poc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Author Request")
public class CreateAuthorRequest {

	@NotBlank(message = "Name is required")
	@Schema(description = "Author name", example = "George Orwell", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "Author birth date", example = "1903-06-25")
	private String birthDate;
}
