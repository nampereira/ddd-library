package library;

/**
 * Base class for all domain rule violations in this application.
 *
 * <p>Subclass this when a domain invariant is broken (e.g. a copy is not available, a book is
 * not found). Using a hierarchy of domain exceptions — rather than Java built-ins like
 * {@link IllegalArgumentException} — allows the {@link GlobalExceptionHandler} to map each
 * exception type to the correct HTTP status and provides meaningful names that reflect the
 * ubiquitous language.</p>
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
