package com.optahaul.mas_java_poc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optahaul.mas_java_poc.domain.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

	List<Book> findByAuthorId(Long authorId);

	List<Book> findByGenreIgnoreCase(String genre);

	List<Book> findByTitleContainingIgnoreCase(String title);

	@Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.id = :id")
	Book findByIdWithAuthor(@Param("id") Long id);
}
