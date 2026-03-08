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
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A Loan assumes the copy is available. Use {@link RentBookService} to enforce that invariant.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_loan_copy_active", columnNames = "active_copy_id"))
public class Loan extends AbstractAggregateRoot<Loan> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "loan_id"))
    private LoanId loanId;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "copy_id"))
    private CopyId copyId;
    // Nullable sentinel: holds copy_id while the loan is active, NULL when returned.
    // The unique constraint on this column prevents two concurrent active loans for the same copy.
    @Column(name = "active_copy_id")
    private java.util.UUID activeCopyId;
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

    public Loan(CopyId copyId, UserId userId) {
        Assert.notNull(copyId, "copyId must not be null");
        Assert.notNull(userId, "userId must not be null");
        this.loanId = new LoanId();
        this.copyId = copyId;
        this.activeCopyId = copyId.id();
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expectedReturnDate = LocalDate.now().plusDays(30);
        this.registerEvent(new LoanCreated(this.copyId));
    }

    public LoanId getLoanId() {
        return loanId;
    }

    public java.util.Collection<Object> getDomainEvents() {
        return domainEvents();
    }

    public void returned() {
        this.returnedAt = LocalDateTime.now();
        this.activeCopyId = null;
        if (this.returnedAt.isAfter(expectedReturnDate.atStartOfDay())) {
            // calculate fee
        }
        this.registerEvent(new LoanClosed(this.copyId));
    }
}
