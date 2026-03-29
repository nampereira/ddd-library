package library.catalog.domain;

import org.apache.commons.validator.routines.ISBNValidator;

/**
 * Value object representing an International Standard Book Number (ISBN).
 *
 * <p>Validates the given string on construction using Apache Commons ISBNValidator, which
 * accepts both ISBN-10 and ISBN-13 formats. If the value is not a valid ISBN, an
 * {@link IllegalArgumentException} is thrown immediately — so if you hold an {@code Isbn}
 * object, it is guaranteed to be valid; there is no need to validate it again later.</p>
 *
 * <p>{@code Isbn} is the domain identity of a {@link Book}: two books with the same ISBN
 * represent the same title.</p>
 */
public record Isbn(String value) {
    private static final ISBNValidator VALIDATOR = new ISBNValidator();

    /** Compact constructor — rejects strings that are not valid ISBN-10 or ISBN-13 values. */
    public Isbn {
        if (!VALIDATOR.isValid(value)) {
            throw new IllegalArgumentException("invalid isbn: " + value);
        }
    }
}
