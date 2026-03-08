package library.catalog.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    Optional<Book> findByIsbnValue(String isbn);
}
