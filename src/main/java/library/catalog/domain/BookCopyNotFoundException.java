package library.catalog.domain;

import library.DomainException;
import library.common.BarCode;

/**
 * Thrown when a physical copy cannot be found by its barcode.
 */
public class BookCopyNotFoundException extends DomainException {
    public BookCopyNotFoundException(BarCode barCode) {
        super("No copy found with barcode '" + barCode.code() + "'");
    }
}
