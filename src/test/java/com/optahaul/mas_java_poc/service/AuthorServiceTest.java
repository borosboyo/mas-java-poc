package com.optahaul.mas_java_poc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.optahaul.mas_java_poc.domain.Author;
import com.optahaul.mas_java_poc.dto.AuthorDto;
import com.optahaul.mas_java_poc.dto.CreateAuthorRequest;
import com.optahaul.mas_java_poc.mapper.AuthorMapper;
import com.optahaul.mas_java_poc.repository.AuthorRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

	@Mock
	private AuthorRepository authorRepository;

	@Mock
	private AuthorMapper authorMapper;

	@InjectMocks
	private AuthorService authorService;

	private Author author;
	private AuthorDto authorDto;
	private CreateAuthorRequest createRequest;

	@BeforeEach
	void setUp() {
		author = new Author();
		author.setId(1L);
		author.setName("John Doe");
		author.setBirthDate(LocalDate.of(1970, 1, 1));

		authorDto = new AuthorDto();
		authorDto.setId(1L);
		authorDto.setName("John Doe");
		authorDto.setBirthDate(LocalDate.of(1970, 1, 1));

		createRequest = new CreateAuthorRequest();
		createRequest.setName("John Doe");
		createRequest.setBirthDate("1970-01-01");
	}

	@Test
	void createAuthor_ShouldReturnAuthorDto() {
		// Given
		when(authorMapper.toEntity(createRequest)).thenReturn(author);
		when(authorRepository.save(any(Author.class))).thenReturn(author);
		when(authorMapper.toDto(author)).thenReturn(authorDto);

		// When
		AuthorDto result = authorService.createAuthor(createRequest);

		// Then
		assertNotNull(result);
		assertEquals("John Doe", result.getName());
		verify(authorRepository).save(any(Author.class));
		verify(authorMapper).toDto(author);
	}

	@Test
	void getAuthorById_ShouldReturnAuthorDto() {
		// Given
		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(authorMapper.toDto(author)).thenReturn(authorDto);

		// When
		AuthorDto result = authorService.getAuthorById(1L);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("John Doe", result.getName());
		verify(authorRepository).findById(1L);
	}

	@Test
	void getAuthorById_WhenNotFound_ShouldThrowException() {
		// Given
		when(authorRepository.findById(999L)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(RuntimeException.class, () -> authorService.getAuthorById(999L));
		verify(authorRepository).findById(999L);
	}

	@Test
	void getAllAuthors_ShouldReturnListOfAuthors() {
		// Given
		List<Author> authors = Arrays.asList(author);
		List<AuthorDto> authorDtos = Arrays.asList(authorDto);
		when(authorRepository.findAll()).thenReturn(authors);
		when(authorMapper.toDtoList(authors)).thenReturn(authorDtos);

		// When
		List<AuthorDto> result = authorService.getAllAuthors();

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("John Doe", result.get(0).getName());
		verify(authorRepository).findAll();
	}
}
