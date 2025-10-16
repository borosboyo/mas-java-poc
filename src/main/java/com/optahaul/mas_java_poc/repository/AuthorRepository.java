package com.optahaul.mas_java_poc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optahaul.mas_java_poc.domain.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

	List<Author> findByNameContainingIgnoreCase(String name);

	@Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
	Author findByIdWithBooks(@Param("id") Long id);
}
