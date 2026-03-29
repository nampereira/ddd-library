package library.lending.domain;

import library.DomainException;

/**
 * Thrown when an attempt is made to return a {@link Loan} that has already been returned.
 */
public class LoanAlreadyReturnedException extends DomainException {
    public LoanAlreadyReturnedException(LoanId loanId) {
        super("Loan '" + loanId.id() + "' has already been returned");
    }
}
