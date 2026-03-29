package library.lending.domain;

import library.common.BarCode;
import org.springframework.stereotype.Service;

/**
 * Domain service that enforces the availability invariant before creating a {@link Loan}.
 *
 * <p>The rule <em>"a copy may only be loaned once at a time"</em> is checked here rather than
 * inside the {@link Loan} constructor because checking it requires a database query
 * ({@link LoanRepository#isAvailable}). Keeping the repository call in a service means the
 * {@code Loan} aggregate stays free of any infrastructure dependency while the business rule
 * remains expressed in the domain layer.</p>
 *
 * <p>Note: the database-level {@code UNIQUE} constraint on {@code active_bar_code} acts as a
 * safety net for concurrent requests that both pass the availability check simultaneously. See
 * {@link Loan} for details.</p>
 */
@Service
public class LoanBookService {
    private final LoanRepository loanRepository;

    public LoanBookService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * Checks that the copy is available and creates a new {@link Loan}.
     *
     * @param barCode  the barcode of the copy to borrow; must not be {@code null}
     * @param patronId the identity of the patron borrowing the copy; must not be {@code null}
     * @return a new, unsaved {@link Loan} (caller is responsible for persisting it)
     * @throws CopyNotAvailableException if the copy is currently on loan
     */
    public Loan loan(BarCode barCode, PatronId patronId) {
        if (!loanRepository.isAvailable(barCode)) {
            throw new CopyNotAvailableException(barCode);
        }
        return new Loan(barCode, patronId);
    }
}
