package com.optahaul.mas_java_poc.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Book Data Transfer Object")
public class BookDto {

	@Schema(description = "Book ID", example = "1")
	private Long id;

	@NotBlank(message = "Title is required")
	@Schema(description = "Book title", example = "1984", requiredMode = Schema.RequiredMode.REQUIRED)
	private String title;

	@NotNull(message = "Author ID is required") @Schema(description = "Author ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long authorId;

	@Schema(description = "Author name", example = "George Orwell")
	private String authorName;

	@Schema(description = "Book genre", example = "Dystopian")
	private String genre;

	@Positive(message = "Page count must be positive") @Schema(description = "Number of pages", example = "328")
	private Integer pageCount;

	@Schema(description = "Book language", example = "English")
	private String language;

	@Schema(description = "Publication date", example = "1949-06-08")
	private LocalDate publicationDate;

	@Schema(description = "Creation timestamp")
	private LocalDateTime createdAt;

	@Schema(description = "Last update timestamp")
	private LocalDateTime updatedAt;
}
