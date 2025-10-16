package com.optahaul.mas_java_poc.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optahaul.mas_java_poc.domain.Author;
import com.optahaul.mas_java_poc.dto.AuthorDto;
import com.optahaul.mas_java_poc.dto.CreateAuthorRequest;
import com.optahaul.mas_java_poc.mapper.AuthorMapper;
import com.optahaul.mas_java_poc.repository.AuthorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthorService {

	private final AuthorRepository authorRepository;
	private final AuthorMapper authorMapper;

	public AuthorDto createAuthor(CreateAuthorRequest request) {
		log.info("Creating new author: {}", request.getName());
		Author author = authorMapper.toEntity(request);
		if (request.getBirthDate() != null && !request.getBirthDate().isEmpty()) {
			author.setBirthDate(LocalDate.parse(request.getBirthDate()));
		}
		Author saved = authorRepository.save(author);
		return authorMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public AuthorDto getAuthorById(Long id) {
		log.info("Fetching author with id: {}", id);
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
		return authorMapper.toDto(author);
	}

	@Transactional(readOnly = true)
	public List<AuthorDto> getAllAuthors() {
		log.info("Fetching all authors");
		return authorMapper.toDtoList(authorRepository.findAll());
	}

	@Transactional(readOnly = true)
	public List<AuthorDto> searchAuthorsByName(String name) {
		log.info("Searching authors by name: {}", name);
		return authorMapper.toDtoList(authorRepository.findByNameContainingIgnoreCase(name));
	}

	public AuthorDto updateAuthor(Long id, CreateAuthorRequest request) {
		log.info("Updating author with id: {}", id);
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

		author.setName(request.getName());
		if (request.getBirthDate() != null && !request.getBirthDate().isEmpty()) {
			author.setBirthDate(LocalDate.parse(request.getBirthDate()));
		}

		Author updated = authorRepository.save(author);
		return authorMapper.toDto(updated);
	}

	public void deleteAuthor(Long id) {
		log.info("Deleting author with id: {}", id);
		if (!authorRepository.existsById(id)) {
			throw new RuntimeException("Author not found with id: " + id);
		}
		authorRepository.deleteById(id);
	}
}
