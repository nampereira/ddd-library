package library.catalog.domain;

import library.DomainException;

/**
 * Thrown when no author with the given {@link ContactEmail} can be found in the catalog.
 */
public class AuthorNotFoundException extends DomainException {
    public AuthorNotFoundException(ContactEmail contactEmail) {
        super("Author with email '" + contactEmail.value() + "' not found");
    }
}
