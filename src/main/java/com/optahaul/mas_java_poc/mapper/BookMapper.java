package com.optahaul.mas_java_poc.mapper;

import java.time.LocalDate;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.optahaul.mas_java_poc.domain.Book;
import com.optahaul.mas_java_poc.dto.BookDto;
import com.optahaul.mas_java_poc.dto.CreateBookRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

	@Mapping(target = "authorId", source = "author.id")
	@Mapping(target = "authorName", source = "author.name")
	BookDto toDto(Book book);

	@Mapping(target = "author", ignore = true)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Book toEntity(CreateBookRequest request);

	List<BookDto> toDtoList(List<Book> books);

	default LocalDate stringToLocalDate(String date) {
		return date == null || date.isEmpty() ? null : LocalDate.parse(date);
	}
}
