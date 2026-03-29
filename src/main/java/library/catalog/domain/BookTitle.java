package library.catalog.domain;

import org.springframework.util.Assert;

/**
 * Value object representing the title of a book.
 *
 * <p>Enforces that a title is non-blank. Two {@code BookTitle} instances with the same
 * string value are considered equal.</p>
 */
public record BookTitle(String value) {

    /** Compact constructor — rejects blank values. */
    public BookTitle {
        Assert.hasText(value, "title must not be blank");
    }
}
