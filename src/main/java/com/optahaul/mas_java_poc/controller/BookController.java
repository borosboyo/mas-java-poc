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

import com.optahaul.mas_java_poc.dto.BookDto;
import com.optahaul.mas_java_poc.dto.CreateBookRequest;
import com.optahaul.mas_java_poc.service.BookService;

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
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management APIs")
public class BookController {

	private final BookService bookService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Create a new book", description = "Creates a new book in the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Book created successfully",
					content = @Content(schema = @Schema(implementation = BookDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<BookDto> createBook(@Valid @RequestBody CreateBookRequest request) {
		BookDto created = bookService.createBook(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get book by ID", description = "Retrieves a book by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Book found",
					content = @Content(schema = @Schema(implementation = BookDto.class))),
			@ApiResponse(responseCode = "404", description = "Book not found")
	})
	public ResponseEntity<BookDto> getBookById(@Parameter(description = "Book ID") @PathVariable Long id) {
		BookDto book = bookService.getBookById(id);
		return ResponseEntity.ok(book);
	}

	@GetMapping
	@Operation(summary = "Get all books", description = "Retrieves all books from the system")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = BookDto.class)))
	public ResponseEntity<List<BookDto>> getAllBooks() {
		List<BookDto> books = bookService.getAllBooks();
		return ResponseEntity.ok(books);
	}

	@GetMapping("/author/{authorId}")
	@Operation(summary = "Get books by author", description = "Retrieves all books by a specific author")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = BookDto.class)))
	public ResponseEntity<List<BookDto>> getBooksByAuthor(
			@Parameter(description = "Author ID") @PathVariable Long authorId) {
		List<BookDto> books = bookService.getBooksByAuthor(authorId);
		return ResponseEntity.ok(books);
	}

	@GetMapping("/genre/{genre}")
	@Operation(summary = "Get books by genre", description = "Retrieves all books of a specific genre")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = BookDto.class)))
	public ResponseEntity<List<BookDto>> getBooksByGenre(@Parameter(description = "Genre") @PathVariable String genre) {
		List<BookDto> books = bookService.getBooksByGenre(genre);
		return ResponseEntity.ok(books);
	}

	@GetMapping("/search")
	@Operation(summary = "Search books by title", description = "Searches for books by title")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved list",
			content = @Content(schema = @Schema(implementation = BookDto.class)))
	public ResponseEntity<List<BookDto>> searchBooks(
			@Parameter(description = "Title to search for") @RequestParam String title) {
		List<BookDto> books = bookService.searchBooksByTitle(title);
		return ResponseEntity.ok(books);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Update book", description = "Updates an existing book")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Book updated successfully",
					content = @Content(schema = @Schema(implementation = BookDto.class))),
			@ApiResponse(responseCode = "404", description = "Book not found"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<BookDto> updateBook(@Parameter(description = "Book ID") @PathVariable Long id,
			@Valid @RequestBody CreateBookRequest request) {
		BookDto updated = bookService.updateBook(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Delete book", description = "Deletes a book from the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Book deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Book not found"),
			@ApiResponse(responseCode = "403", description = "Access denied")
	})
	public ResponseEntity<Void> deleteBook(@Parameter(description = "Book ID") @PathVariable Long id) {
		bookService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}
}
