package library.lending.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import library.common.BarCode;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root representing the act of a patron borrowing a physical copy of a book.
 *
 * <p>A {@code Loan} captures which copy was borrowed ({@link BarCode}), who borrowed it
 * ({@link UserId}), when it was created, and when it is expected back (30 days). The
 * {@code returnedAt} field is {@code null} while the loan is active and set when
 * {@link #returned()} is called.</p>
 *
 * <h2>Concurrency safety</h2>
 * <p>The {@code active_bar_code} column holds the copy's barcode while the loan is active and
 * is cleared to {@code NULL} on return. A {@code UNIQUE} database constraint on this column
 * prevents two concurrent loans for the same copy: the second insert fails with a constraint
 * violation, which {@link library.GlobalExceptionHandler} translates into HTTP 409 Conflict.</p>
 *
 * <p>The {@link Version} field provides optimistic locking for concurrent <em>updates</em>
 * to the same loan (e.g. two threads trying to return the same copy simultaneously).</p>
 *
 * <h2>Domain events</h2>
 * <p>{@link LoanCreated} is published on construction; {@link LoanClosed} is published when
 * {@link #returned()} is called. Both events carry the {@link BarCode} so that the catalog
 * can update copy availability without any direct coupling to this aggregate.</p>
 *
 * <p>A {@code Loan} assumes the copy is available. Use {@link LoanBookService} to enforce
 * that invariant before constructing an instance.</p>
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_loan_copy_active", columnNames = "active_bar_code"))
public class Loan extends AbstractAggregateRoot<Loan> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "loan_id"))
    private LoanId loanId;
    // BarCode is a plain value object, not a @OneToOne to BookCopy, because BookCopy belongs to the
    // catalog bounded context. Aggregates in different bounded contexts must reference each other
    // by identity only — never by direct object reference — so that each context can evolve
    // independently. The same reasoning applies to UserId below.
    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "bar_code"))
    private BarCode barCode;
    // Nullable sentinel: holds bar_code while the loan is active, NULL when returned.
    // The unique constraint on this column prevents two concurrent active loans for the same copy.
    @Column(name = "active_bar_code")
    private String activeBarCode;
    // Same rule: UserId is a plain value object, not a @ManyToOne, because users are managed
    // in a separate bounded context (security).
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id"))
    private UserId userId;
    private LocalDateTime createdAt;
    private LocalDate expectedReturnDate;
    private LocalDateTime returnedAt;

    @Version
    private Long version;

    protected Loan() {
    }

    /**
     * Creates a new active loan. Assumes the copy is available — use {@link LoanBookService}
     * to check availability before calling this constructor.
     *
     * @param barCode the barcode of the copy being borrowed; must not be {@code null}
     * @param userId  the identity of the patron borrowing the copy; must not be {@code null}
     */
    public Loan(BarCode barCode, UserId userId) {
        Assert.notNull(barCode, "barCode must not be null");
        Assert.notNull(userId, "userId must not be null");
        this.loanId = new LoanId();
        this.barCode = barCode;
        this.activeBarCode = barCode.code();
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expectedReturnDate = LocalDate.now().plusDays(30);
        this.registerEvent(new LoanCreated(this.barCode));
    }

    /** Returns the domain identity of this loan. */
    public LoanId getLoanId() {
        return loanId;
    }

    /**
     * Returns {@code true} if this loan has not yet been returned.
     *
     * <p>This is the explicit domain concept that the {@code active_bar_code} nullable sentinel
     * exists to enforce at the database level. Prefer this method over checking
     * {@code returnedAt == null} directly.</p>
     */
    public boolean isActive() {
        return returnedAt == null;
    }

    /** Exposes collected domain events (for testing). */
    public java.util.Collection<Object> getDomainEvents() {
        return domainEvents();
    }

    /**
     * Marks this loan as returned, clears the active-copy sentinel (releasing the uniqueness
     * constraint slot), and publishes a {@link LoanClosed} event.
     *
     * @throws LoanAlreadyReturnedException if this loan has already been returned
     */
    public void returned() {
        if (!isActive()) {
            throw new LoanAlreadyReturnedException(this.loanId);
        }
        this.returnedAt = LocalDateTime.now();
        this.activeBarCode = null;
        this.registerEvent(new LoanClosed(this.barCode));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(loanId, loan.loanId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(loanId);
    }
}
