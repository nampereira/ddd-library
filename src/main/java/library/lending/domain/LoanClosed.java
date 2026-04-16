package library.lending.domain;

import library.common.BarCode;

/**
 * Domain event published when a patron returns a borrowed copy.
 *
 * <p>Carries the {@link BarCode} of the returned copy so that listeners in other functional
 * areas (e.g. catalog) can react without being directly coupled to the {@link Loan} aggregate.</p>
 */
public record LoanClosed(BarCode barCode) {
}
