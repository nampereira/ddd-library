package library.lending.domain;

import library.common.BarCode;

/**
 * Domain event published when a patron borrows a copy.
 *
 * <p>Carries the {@link BarCode} of the borrowed copy so that listeners in other functional
 * areas (e.g. catalog) can react without being directly coupled to the {@link Loan} aggregate.</p>
 */
public record LoanCreated(BarCode barCode) {
}
