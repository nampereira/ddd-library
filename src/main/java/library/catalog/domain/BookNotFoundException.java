package library.catalog.domain;

import library.DomainException;

/**
 * Thrown when a book cannot be found in the catalog by its ISBN.
 */
public class BookNotFoundException extends DomainException {
    public BookNotFoundException(Isbn isbn) {
        super("Book with ISBN '" + isbn.value() + "' not found in the catalog");
    }
}
