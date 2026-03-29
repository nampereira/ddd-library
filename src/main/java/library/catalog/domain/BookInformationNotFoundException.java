package library.catalog.domain;

import library.DomainException;

/**
 * Thrown when book information cannot be retrieved for the given ISBN from the external
 * book search service (e.g. the ISBN is unknown or the service returned no title).
 */
public class BookInformationNotFoundException extends DomainException {
    public BookInformationNotFoundException(Isbn isbn) {
        super("No book information found for ISBN '" + isbn.value() + "'");
    }
}
