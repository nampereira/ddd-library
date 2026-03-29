package library.lending.domain;

import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Value object representing the unique identity of a library user within the lending
 * functional area.
 *
 * <p>The UUID is extracted from the JWT token by {@link library.lending.LendingController}
 * and passed through to the {@link Loan} to record who borrowed a copy. Being a value object,
 * two {@code UserId} instances that hold the same UUID are considered equal.</p>
 */
public record UserId(UUID id) {

    /** Compact constructor — rejects a {@code null} UUID. */
    public UserId {
        Assert.notNull(id, "id must not be null");
    }
}
