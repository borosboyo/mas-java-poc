package com.optahaul.mas_java_poc.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
@Schema(description = "Author Data Transfer Object")
public class AuthorDto {

	@Schema(description = "Author ID", example = "1")
	private Long id;

	@NotBlank(message = "Name is required")
	@Schema(description = "Author name", example = "George Orwell", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "Author birth date", example = "1903-06-25")
	private LocalDate birthDate;

	@Schema(description = "List of book IDs written by this author")
	private List<Long> bookIds;

	@Schema(description = "Creation timestamp")
	private LocalDateTime createdAt;

	@Schema(description = "Last update timestamp")
	private LocalDateTime updatedAt;
}
