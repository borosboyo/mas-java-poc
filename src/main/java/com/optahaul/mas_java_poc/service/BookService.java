package com.optahaul.mas_java_poc.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optahaul.mas_java_poc.domain.Author;
import com.optahaul.mas_java_poc.domain.Book;
import com.optahaul.mas_java_poc.dto.BookDto;
import com.optahaul.mas_java_poc.dto.CreateBookRequest;
import com.optahaul.mas_java_poc.mapper.BookMapper;
import com.optahaul.mas_java_poc.repository.AuthorRepository;
import com.optahaul.mas_java_poc.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;
	private final BookMapper bookMapper;

	public BookDto createBook(CreateBookRequest request) {
		log.info("Creating new book: {}", request.getTitle());
		Author author = authorRepository.findById(request.getAuthorId())
				.orElseThrow(() -> new RuntimeException("Author not found with id: " + request.getAuthorId()));

		Book book = bookMapper.toEntity(request);
		book.setAuthor(author);
		if (request.getPublicationDate() != null && !request.getPublicationDate().isEmpty()) {
			book.setPublicationDate(LocalDate.parse(request.getPublicationDate()));
		}

		Book saved = bookRepository.save(book);
		return bookMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public BookDto getBookById(Long id) {
		log.info("Fetching book with id: {}", id);
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
		return bookMapper.toDto(book);
	}

	@Transactional(readOnly = true)
	public List<BookDto> getAllBooks() {
		log.info("Fetching all books");
		return bookMapper.toDtoList(bookRepository.findAll());
	}

	@Transactional(readOnly = true)
	public List<BookDto> getBooksByAuthor(Long authorId) {
		log.info("Fetching books by author id: {}", authorId);
		return bookMapper.toDtoList(bookRepository.findByAuthorId(authorId));
	}

	@Transactional(readOnly = true)
	public List<BookDto> getBooksByGenre(String genre) {
		log.info("Fetching books by genre: {}", genre);
		return bookMapper.toDtoList(bookRepository.findByGenreIgnoreCase(genre));
	}

	@Transactional(readOnly = true)
	public List<BookDto> searchBooksByTitle(String title) {
		log.info("Searching books by title: {}", title);
		return bookMapper.toDtoList(bookRepository.findByTitleContainingIgnoreCase(title));
	}

	public BookDto updateBook(Long id, CreateBookRequest request) {
		log.info("Updating book with id: {}", id);
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

		Author author = authorRepository.findById(request.getAuthorId())
				.orElseThrow(() -> new RuntimeException("Author not found with id: " + request.getAuthorId()));

		book.setTitle(request.getTitle());
		book.setAuthor(author);
		book.setGenre(request.getGenre());
		book.setPageCount(request.getPageCount());
		book.setLanguage(request.getLanguage());
		if (request.getPublicationDate() != null && !request.getPublicationDate().isEmpty()) {
			book.setPublicationDate(LocalDate.parse(request.getPublicationDate()));
		}

		Book updated = bookRepository.save(book);
		return bookMapper.toDto(updated);
	}

	public void deleteBook(Long id) {
		log.info("Deleting book with id: {}", id);
		if (!bookRepository.existsById(id)) {
			throw new RuntimeException("Book not found with id: " + id);
		}
		bookRepository.deleteById(id);
	}
}
