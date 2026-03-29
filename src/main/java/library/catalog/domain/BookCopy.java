package library.catalog.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import library.common.BarCode;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * Aggregate root representing a single physical copy of a book that can be placed on a shelf
 * and lent to a patron.
 *
 * <p>Each copy is identified by its {@link BarCode} (the sticker affixed to the physical object),
 * which is unique across all copies. It also carries the {@link Isbn} of the book title it belongs
 * to (referenced by value, not by object).</p>
 *
 * <h2>Availability</h2>
 * <p>{@code BookCopy} does not track its own availability. The authoritative source of truth is
 * the lending context: a copy is available if and only if it has no active loan, as determined
 * by {@link library.lending.domain.LoanRepository#isAvailable}. Keeping availability exclusively
 * in the lending context avoids the need for event-driven synchronisation and eliminates the
 * risk of the two contexts diverging.</p>
 */
@Entity
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "isbn"))
    private Isbn isbn;
    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "bar_code", unique = true))
    private BarCode barCode;

    protected BookCopy() {
    }

    /**
     * Creates a new copy for the given ISBN with the given barcode.
     *
     * @param isbn    the ISBN of the book title this copy belongs to; must not be {@code null}
     * @param barCode the physical barcode label on this copy; must not be {@code null}
     */
    public BookCopy(Isbn isbn, BarCode barCode) {
        Assert.notNull(isbn, "isbn must not be null");
        Assert.notNull(barCode, "barCode must not be null");
        this.isbn = isbn;
        this.barCode = barCode;
    }

    /** Returns the domain identity of this copy. */
    public BarCode getBarCode() {
        return barCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy bookCopy = (BookCopy) o;
        return Objects.equals(barCode, bookCopy.barCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(barCode);
    }
}
