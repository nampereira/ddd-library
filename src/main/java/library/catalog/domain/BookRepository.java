package library.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle_ValueContainingIgnoreCase(String title);
    Optional<Book> findByIsbnValue(String isbn);

    /**
     * Finds the {@link Book} that contains the {@link BookAuthor} with the given contact email.
     *
     * <p>Used by {@link library.catalog.application.EditAuthorDetailsUseCase} to locate the
     * aggregate root before mutating a child author.</p>
     */
    Optional<Book> findByAuthors_ContactEmail_Value(String contactEmail);
}
