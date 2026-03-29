package library.lending.domain;

import library.DomainException;

/**
 * Thrown when a loan cannot be found by its domain identity.
 */
public class LoanNotFoundException extends DomainException {
    public LoanNotFoundException(LoanId loanId) {
        super("Loan '" + loanId.id() + "' not found");
    }
}
