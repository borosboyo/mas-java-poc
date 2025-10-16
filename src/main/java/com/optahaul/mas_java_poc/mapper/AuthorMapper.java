package com.optahaul.mas_java_poc.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.optahaul.mas_java_poc.domain.Author;
import com.optahaul.mas_java_poc.domain.Book;
import com.optahaul.mas_java_poc.dto.AuthorDto;
import com.optahaul.mas_java_poc.dto.CreateAuthorRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

	@Mapping(target = "bookIds", source = "books")
	AuthorDto toDto(Author author);

	@Mapping(target = "books", ignore = true)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Author toEntity(CreateAuthorRequest request);

	List<AuthorDto> toDtoList(List<Author> authors);

	default List<Long> mapBooksToIds(List<Book> books) {
		return books == null ? null : books.stream().map(Book::getId).collect(Collectors.toList());
	}

	default LocalDate stringToLocalDate(String date) {
		return date == null || date.isEmpty() ? null : LocalDate.parse(date);
	}
}
