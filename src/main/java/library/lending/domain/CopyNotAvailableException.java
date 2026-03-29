package library.lending.domain;

import library.DomainException;
import library.common.BarCode;

/**
 * Thrown when a patron attempts to borrow a copy that is already on loan.
 */
public class CopyNotAvailableException extends DomainException {
    public CopyNotAvailableException(BarCode barCode) {
        super("Copy '" + barCode.code() + "' is not available");
    }
}
