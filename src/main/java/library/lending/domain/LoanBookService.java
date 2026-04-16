package library.lending.domain;

import library.common.BarCode;
import org.springframework.stereotype.Service;

/**
 * Domain service that enforces lending invariants before creating a {@link Loan}.
 *
 * <p>Two rules are checked here rather than inside the {@link Loan} constructor:</p>
 * <ul>
 *   <li><em>"A reference copy stays in the library at all times"</em> — copies whose barcode
 *       starts with the {@code REF-} prefix ({@link BarCode.CopyType#REFERENCE}) may never
 *       be lent out.</li>
 *   <li><em>"A copy may only be loaned once at a time"</em> — availability is confirmed via
 *       {@link LoanRepository#isAvailable}, which requires a database query. Keeping that
 *       query here rather than in {@code Loan} keeps the aggregate free of infrastructure
 *       dependencies.</li>
 * </ul>
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
     * Enforces lending rules and creates a new {@link Loan}.
     *
     * @param barCode  the barcode of the copy to borrow; must not be {@code null}
     * @param patronId the identity of the patron borrowing the copy; must not be {@code null}
     * @return a new, unsaved {@link Loan} (caller is responsible for persisting it)
     * @throws ReferenceOnlyException    if the copy is a reference-only copy ({@code REF-} prefix)
     * @throws CopyNotAvailableException if the copy is currently on loan
     */
    public Loan loan(BarCode barCode, PatronId patronId) {
        if (barCode.copyType() == BarCode.CopyType.REFERENCE) {
            throw new ReferenceOnlyException(barCode);
        }
        if (!loanRepository.isAvailable(barCode)) {
            throw new CopyNotAvailableException(barCode);
        }
        return new Loan(barCode, patronId);
    }
}
