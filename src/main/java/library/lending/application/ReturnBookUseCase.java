package library.lending.application;

import library.UseCase;
import library.lending.domain.Loan;
import library.lending.domain.LoanId;
import library.lending.domain.LoanRepository;

/**
 * Use case: a patron returns a borrowed copy.
 *
 * <p>Loads the active {@link Loan} by its ID, calls {@link Loan#returned()} which records the
 * return time and clears the uniqueness-constraint sentinel ({@code activeBarCode}), then
 * persists the updated loan via {@link LoanRepository#save}. After save, Spring publishes any
 * registered domain events (e.g. {@link library.lending.domain.LoanClosed}) to interested
 * listeners.</p>
 *
 * <p>Copy availability is not managed here. Whether a copy is available is always determined
 * by querying {@link LoanRepository#isAvailable} — the {@code Loan} aggregate is the single
 * source of truth.</p>
 */
@UseCase
public class ReturnBookUseCase {

    private final LoanRepository loanRepository;

    public ReturnBookUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    /**
     * Closes the loan identified by the given ID.
     *
     * @param loanId the identity of the loan to close
     * @throws library.lending.domain.LoanNotFoundException if no loan exists with the given ID
     */
    public void execute(LoanId loanId) {
        Loan loan = loanRepository.findByIdOrThrow(loanId);
        loan.returned();
        loanRepository.save(loan);
    }
}
