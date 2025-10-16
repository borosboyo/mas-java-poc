package com.optahaul.mas_java_poc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.optahaul.mas_java_poc.domain.Author;
import com.optahaul.mas_java_poc.domain.Book;
import com.optahaul.mas_java_poc.dto.BookDto;
import com.optahaul.mas_java_poc.dto.CreateBookRequest;
import com.optahaul.mas_java_poc.mapper.BookMapper;
import com.optahaul.mas_java_poc.repository.AuthorRepository;
import com.optahaul.mas_java_poc.repository.BookRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	@Mock
	private AuthorRepository authorRepository;

	@Mock
	private BookMapper bookMapper;

	@InjectMocks
	private BookService bookService;

	private Book book;
	private BookDto bookDto;
	private Author author;
	private CreateBookRequest createRequest;

	@BeforeEach
	void setUp() {
		author = new Author();
		author.setId(1L);
		author.setName("John Doe");

		book = new Book();
		book.setId(1L);
		book.setTitle("Test Book");
		book.setPublicationDate(LocalDate.of(2024, 1, 1));
		book.setAuthor(author);

		bookDto = new BookDto();
		bookDto.setId(1L);
		bookDto.setTitle("Test Book");
		bookDto.setPublicationDate(LocalDate.of(2024, 1, 1));

		createRequest = new CreateBookRequest();
		createRequest.setTitle("Test Book");
		createRequest.setPublicationDate("2024-01-01");
		createRequest.setAuthorId(1L);
	}

	@Test
	void createBook_ShouldReturnBookDto() {
		// Given
		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(bookMapper.toEntity(createRequest)).thenReturn(book);
		when(bookRepository.save(any(Book.class))).thenReturn(book);
		when(bookMapper.toDto(book)).thenReturn(bookDto);

		// When
		BookDto result = bookService.createBook(createRequest);

		// Then
		assertNotNull(result);
		assertEquals("Test Book", result.getTitle());
		verify(authorRepository).findById(1L);
		verify(bookRepository).save(any(Book.class));
	}

	@Test
	void createBook_WhenAuthorNotFound_ShouldThrowException() {
		// Given
		when(authorRepository.findById(999L)).thenReturn(Optional.empty());
		createRequest.setAuthorId(999L);

		// When & Then
		assertThrows(RuntimeException.class, () -> bookService.createBook(createRequest));
		verify(authorRepository).findById(999L);
		verify(bookRepository, never()).save(any());
	}

	@Test
	void getBookById_ShouldReturnBookDto() {
		// Given
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
		when(bookMapper.toDto(book)).thenReturn(bookDto);

		// When
		BookDto result = bookService.getBookById(1L);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Book", result.getTitle());
		verify(bookRepository).findById(1L);
	}

	@Test
	void getBookById_WhenNotFound_ShouldThrowException() {
		// Given
		when(bookRepository.findById(999L)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(RuntimeException.class, () -> bookService.getBookById(999L));
		verify(bookRepository).findById(999L);
	}
}
