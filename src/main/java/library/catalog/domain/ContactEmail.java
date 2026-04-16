package library.catalog.domain;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.Assert;

/**
 * Value object representing a contact email address for a {@link BookAuthor}.
 *
 * <p>An email must be non-blank and pass RFC-compliant validation (delegated to
 * Apache Commons Validator's {@link EmailValidator}). Being a value object, two
 * {@code ContactEmail} instances with the same string are considered equal.</p>
 *
 * <p>Serves as the domain identity of a {@link BookAuthor}: each author is uniquely identified
 * by their contact email rather than by a generated UUID.</p>
 */
public record ContactEmail(String value) {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    /** Compact constructor — rejects blank values and malformed email addresses. */
    public ContactEmail {
        Assert.hasText(value, "Contact email must not be blank");
        Assert.isTrue(EMAIL_VALIDATOR.isValid(value),
                "Contact email must be a valid email address");
    }
}
