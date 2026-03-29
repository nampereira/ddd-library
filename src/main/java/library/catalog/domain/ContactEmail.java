package library.catalog.domain;

import org.springframework.util.Assert;

/**
 * Value object representing a contact email address for a {@link BookAuthor}.
 *
 * <p>An email must be non-blank and match a basic format ({@code local@domain.tld}).
 * Being a value object, two {@code ContactEmail} instances with the same string are equal.</p>
 *
 * <p>Serves as the domain identity of a {@link BookAuthor}: each author is uniquely identified
 * by their contact email rather than by a generated UUID.</p>
 */
public record ContactEmail(String value) {

    /** Compact constructor — rejects blank values and malformed email addresses. */
    public ContactEmail {
        Assert.hasText(value, "Contact email must not be blank");
        Assert.isTrue(value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"),
                "Contact email must be a valid email address");
    }
}
