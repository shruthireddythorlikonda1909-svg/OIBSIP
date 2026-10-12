package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findAllByOrderByTitleAsc();

    @Query("select b from Book b " +
           "where lower(b.title) like lower(concat('%', :q, '%')) " +
           "   or lower(b.author) like lower(concat('%', :q, '%')) " +
           "order by b.title")
    List<Book> search(@Param("q") String q);

    @Query("select distinct b.category from Book b order by b.category")
    List<String> findCategories();
}
