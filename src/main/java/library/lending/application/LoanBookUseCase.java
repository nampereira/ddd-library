package library.lending.application;

import library.UseCase;
import library.catalog.domain.BookCopyNotFoundException;
import library.catalog.domain.BookCopyRepository;
import library.common.BarCode;
import library.lending.domain.Loan;
import library.lending.domain.LoanBookService;
import library.lending.domain.LoanRepository;
import library.lending.domain.PatronId;

/**
 * Use case: a patron borrows a physical copy of a book.
 *
 * <p>Resolves the physical copy by its barcode, then delegates the availability check and
 * loan creation to {@link LoanBookService}. The resulting {@link library.lending.domain.Loan}
 * is persisted via {@code save()}, which also triggers publication of any registered domain
 * events (e.g. {@link library.lending.domain.LoanCreated}).</p>
 *
 * <p>Copy availability is not stored on the {@code BookCopy}. It is always derived from the
 * {@code Loan} aggregate by querying {@link library.lending.domain.LoanRepository#isAvailable},
 * making the lending context the single source of truth.</p>
 */
@UseCase
public class LoanBookUseCase {
    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LoanBookService loanBookService;

    public LoanBookUseCase(LoanRepository loanRepository, BookCopyRepository bookCopyRepository, LoanBookService loanBookService) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.loanBookService = loanBookService;
    }

    /**
     * Loans the copy identified by the given barcode to the given patron.
     *
     * @param barCode  the barcode of the physical copy to borrow
     * @param patronId the identity of the patron
     * @return the persisted {@link Loan}
     * @throws BookCopyNotFoundException         if no copy exists with the given barcode
     * @throws library.lending.domain.CopyNotAvailableException if the copy is already on loan
     */
    public Loan execute(String barCode, PatronId patronId) {
        BarCode bc = new BarCode(barCode);
        // Verify the copy exists in the catalog before attempting to create a loan.
        // This use case depends on BookCopyRepository from the catalog bounded context.
        // This is an intentional design decision: catalog and lending are co-deployed modules
        // within the same application, not separately deployed services. The coupling is
        // acceptable and is confined to this single existence check at the application layer.
        bookCopyRepository.findByBarCode(bc).orElseThrow(() -> new BookCopyNotFoundException(bc));
        return loanRepository.save(loanBookService.loan(bc, patronId));
    }
}
