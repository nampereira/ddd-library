package library.lending.domain;

import library.DomainException;
import library.common.BarCode;

/**
 * Thrown when a patron attempts to borrow a reference-only copy.
 *
 * <p>A copy whose barcode starts with the {@code REF-} prefix (i.e.
 * {@link BarCode.CopyType#REFERENCE}) is intended to stay in the library at all times
 * and may never be lent out. This rule is enforced by {@link LoanBookService} before a
 * {@link Loan} is created.</p>
 */
public class ReferenceOnlyException extends DomainException {
    public ReferenceOnlyException(BarCode barCode) {
        super("Copy '" + barCode.code() + "' is a reference copy and may not be borrowed");
    }
}
