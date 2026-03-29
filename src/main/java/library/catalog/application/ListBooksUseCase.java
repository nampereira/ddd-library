package library.catalog.application;

import library.UseCase;
import library.catalog.domain.Book;
import library.catalog.domain.BookRepository;
import library.catalog.domain.Isbn;

import java.util.List;
import java.util.Optional;

/**
 * Use case: list or search for books in the catalog.
 *
 * <p>Provides three query methods used by {@link library.catalog.CatalogController}:</p>
 * <ul>
 *   <li>{@link #listAll()} — returns every book in the catalog</li>
 *   <li>{@link #searchByTitle(String)} — case-insensitive partial title match</li>
 *   <li>{@link #searchByIsbn(Isbn)} — exact ISBN lookup</li>
 * </ul>
 */
@UseCase
public class ListBooksUseCase {

    private final BookRepository bookRepository;

    public ListBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /** Returns all books in the catalog. */
    public List<Book> listAll() {
        return bookRepository.findAll();
    }

    /**
     * Returns books whose title contains the given string (case-insensitive).
     *
     * @param title the partial title to search for
     */
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitle_ValueContainingIgnoreCase(title);
    }

    /**
     * Returns the book with the given ISBN, if it exists in the catalog.
     *
     * @param isbn the exact ISBN to look up
     */
    public Optional<Book> searchByIsbn(Isbn isbn) {
        return bookRepository.findByIsbnValue(isbn.value());
    }
}
