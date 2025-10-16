package com.optahaul.mas_java_poc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optahaul.mas_java_poc.dto.AuthorDto;
import com.optahaul.mas_java_poc.dto.BookDto;
import com.optahaul.mas_java_poc.service.AuthorService;
import com.optahaul.mas_java_poc.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public/api")
@RequiredArgsConstructor
@Tag(name = "Public API", description = "Publicly accessible APIs without authentication")
public class PublicApiController {

	private final BookService bookService;
	private final AuthorService authorService;

	@GetMapping("/books")
	@Operation(summary = "Get all books (public)", description = "Publicly accessible endpoint to get all books")
	public ResponseEntity<List<BookDto>> getAllBooks() {
		return ResponseEntity.ok(bookService.getAllBooks());
	}

	@GetMapping("/books/{id}")
	@Operation(summary = "Get book by ID (public)", description = "Publicly accessible endpoint to get a book by ID")
	public ResponseEntity<BookDto> getBookById(@Parameter(description = "Book ID") @PathVariable Long id) {
		return ResponseEntity.ok(bookService.getBookById(id));
	}

	@GetMapping("/authors")
	@Operation(summary = "Get all authors (public)", description = "Publicly accessible endpoint to get all authors")
	public ResponseEntity<List<AuthorDto>> getAllAuthors() {
		return ResponseEntity.ok(authorService.getAllAuthors());
	}

	@GetMapping("/authors/{id}")
	@Operation(summary = "Get author by ID (public)", description = "Publicly accessible endpoint to get an author by ID")
	public ResponseEntity<AuthorDto> getAuthorById(@Parameter(description = "Author ID") @PathVariable Long id) {
		return ResponseEntity.ok(authorService.getAuthorById(id));
	}

	@GetMapping("/books/genre/{genre}")
	@Operation(summary = "Get books by genre (public)", description = "Publicly accessible endpoint to get books by genre")
	public ResponseEntity<List<BookDto>> getBooksByGenre(@Parameter(description = "Genre") @PathVariable String genre) {
		return ResponseEntity.ok(bookService.getBooksByGenre(genre));
	}
}
