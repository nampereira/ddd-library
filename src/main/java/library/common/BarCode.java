package library.common;

import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Value object representing the barcode label affixed to a physical book copy.
 *
 * <p>A barcode must be non-blank and must start with a recognised prefix that encodes the
 * copy type (e.g. {@code "BC-0001"} for a circulating copy, {@code "REF-0001"} for a
 * reference-only copy).</p>
 *
 * <p>Shared between the catalog and lending functional areas so that both can
 * refer to the same copy without coupling their domain models to each other.
 * The lending area stores a {@code BarCode} on a {@link library.lending.domain.Loan}
 * as a reference; the catalog area stores it on a {@link library.catalog.domain.BookCopy}
 * as its domain identity.</p>
 */
public record BarCode(String code) {

    /**
     * Encodes the intended use of a physical copy via its barcode prefix.
     *
     * <ul>
     *   <li>{@link #CIRCULATING} ({@code "BC-"}) — copy may be lent to patrons.</li>
     *   <li>{@link #REFERENCE} ({@code "REF-"}) — copy stays in the library at all times.</li>
     * </ul>
     */
    public enum CopyType {
        CIRCULATING("BC-"),
        REFERENCE("REF-");

        private final String prefix;

        CopyType(String prefix) {
            this.prefix = prefix;
        }

        public String prefix() {
            return prefix;
        }
    }

    /** Compact constructor — rejects blank codes and unknown prefixes. */
    public BarCode {
        Assert.hasText(code, "Bar code must not be blank");
        boolean knownPrefix = Arrays.stream(CopyType.values())
                .anyMatch(t -> code.startsWith(t.prefix()));
        Assert.isTrue(knownPrefix, "Bar code must start with a recognised prefix (BC- or REF-)");
    }

    /** Returns the {@link CopyType} encoded in this barcode's prefix. */
    public CopyType copyType() {
        return Arrays.stream(CopyType.values())
                .filter(t -> code.startsWith(t.prefix()))
                .findFirst()
                .orElseThrow(); // safe: constructor already validated
    }
}
