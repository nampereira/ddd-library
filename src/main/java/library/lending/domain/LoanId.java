package library.lending.domain;

import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Value object representing the unique identity of a {@link Loan}.
 *
 * <p>A randomly generated UUID is assigned when a loan is created. Being a value object,
 * two {@code LoanId} instances that hold the same UUID are considered equal.</p>
 */
public record LoanId(UUID id) {

    /** Compact constructor — rejects a {@code null} UUID. */
    public LoanId {
        Assert.notNull(id, "id must not be null");
    }

    /** Convenience constructor that generates a random UUID. */
    public LoanId() {
        this(UUID.randomUUID());
    }
}
