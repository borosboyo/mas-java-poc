package com.optahaul.mas_java_poc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.dto.AuthorDto;
import com.optahaul.mas_java_poc.dto.CreateAuthorRequest;
import com.optahaul.mas_java_poc.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author management APIs")
public class AuthorController {

	private final AuthorService authorService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Create a new author", description = "Creates a new author in the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Author created successfully",
					content = @Content(schema = @Schema(implementation = AuthorDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
		AuthorDto created = authorService.createAuthor(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get author by ID", description = "Retrieves an author by their ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Author found",
					content = @Content(schema = @Schema(implementation = AuthorDto.class))),
			@ApiResponse(responseCode = "404", description = "Author not found")
	})
	public ResponseEntity<AuthorDto> getAuthorById(@Parameter(description = "Author ID") @PathVariable Long id) {
		AuthorDto author = authorService.getAuthorById(id);
		return ResponseEntity.ok(author);
	}

	@GetMapping
	@Operation(summary = "Get all authors", description = "Retrieves all authors from the system")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = AuthorDto.class)))
	public ResponseEntity<List<AuthorDto>> getAllAuthors() {
		List<AuthorDto> authors = authorService.getAllAuthors();
		return ResponseEntity.ok(authors);
	}

	@GetMapping("/search")
	@Operation(summary = "Search authors by name", description = "Searches for authors by name")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = AuthorDto.class)))
	public ResponseEntity<List<AuthorDto>> searchAuthors(
			@Parameter(description = "Name to search for") @RequestParam String name) {
		List<AuthorDto> authors = authorService.searchAuthorsByName(name);
		return ResponseEntity.ok(authors);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Update author", description = "Updates an existing author")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Author updated successfully",
					content = @Content(schema = @Schema(implementation = AuthorDto.class))),
			@ApiResponse(responseCode = "404", description = "Author not found"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<AuthorDto> updateAuthor(@Parameter(description = "Author ID") @PathVariable Long id,
			@Valid @RequestBody CreateAuthorRequest request) {
		AuthorDto updated = authorService.updateAuthor(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Delete author", description = "Deletes an author from the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Author deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Author not found"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<Void> deleteAuthor(@Parameter(description = "Author ID") @PathVariable Long id) {
		authorService.deleteAuthor(id);
		return ResponseEntity.noContent().build();
	}
}
