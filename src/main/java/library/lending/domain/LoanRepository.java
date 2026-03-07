package library.lending.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LoanRepository extends CrudRepository<Loan, Long> {
    @Query("select count(*) = 0 from Loan where copyId = :id and returnedAt is null")
    boolean isAvailable(CopyId id);

    Optional<Loan> findByLoanId(LoanId loanId);

    default Loan findByIdOrThrow(LoanId loanId) {
        return findByLoanId(loanId).orElseThrow();
    }
}
