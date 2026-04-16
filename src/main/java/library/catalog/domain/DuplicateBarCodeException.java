package library.catalog.domain;

import library.DomainException;
import library.common.BarCode;

/**
 * Thrown when a librarian attempts to register a copy with a barcode that already exists.
 */
public class DuplicateBarCodeException extends DomainException {
    public DuplicateBarCodeException(BarCode barCode) {
        super("A copy with barcode '" + barCode.code() + "' already exists");
    }
}
