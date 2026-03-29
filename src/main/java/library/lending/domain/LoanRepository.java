package library.lending.domain;

import library.common.BarCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for {@link Loan} entities.
 *
 * <p>Defined as an interface in the domain layer so that the domain model has no dependency on
 * infrastructure. Spring Data JPA provides the implementation at runtime.</p>
 */
public interface LoanRepository extends CrudRepository<Loan, Long> {

    /**
     * Returns {@code true} if the given copy has no active loan (i.e. it is available to borrow).
     *
     * @param barCode the barcode of the copy to check
     */
    @Query("select count(*) = 0 from Loan where barCode = :barCode and returnedAt is null")
    boolean isAvailable(BarCode barCode);

    /**
     * Finds a loan by its domain identity.
     *
     * @param loanId the unique identifier of the loan
     * @return the loan wrapped in an {@link Optional}, or {@link Optional#empty()} if not found
     */
    Optional<Loan> findByLoanId(LoanId loanId);

    /**
     * Finds a loan by its domain identity, throwing {@link LoanNotFoundException}
     * if no loan exists with the given identifier.
     *
     * @param loanId the unique identifier of the loan
     * @return the loan
     * @throws LoanNotFoundException if no loan with the given ID exists
     */
    default Loan findByIdOrThrow(LoanId loanId) {
        return findByLoanId(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
    }
}
