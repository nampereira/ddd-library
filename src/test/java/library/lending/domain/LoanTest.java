package library.lending.domain;

import library.common.BarCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LoanTest {

    private BarCode barCode;
    private UserId userId;

    @BeforeEach
    void setUp() {
        barCode = new BarCode("BC-0001");
        userId = new UserId(UUID.randomUUID());
    }

    @Test
    void rejectsNullBarCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(null, userId));
    }

    @Test
    void rejectsNullUserId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(barCode, null));
    }

    @Test
    void registersLoanCreatedEventOnCreation() {
        var loan = new Loan(barCode, userId);

        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }

    @Test
    void registersLoanClosedEventOnReturn() {
        var loan = new Loan(barCode, userId);

        loan.returned();

        assertThat(loan.getDomainEvents())
                .hasSize(2) // LoanCreated + LoanClosed
                .last()
                .isInstanceOf(LoanClosed.class);
    }

    @Test
    void rejectsReturnOfAlreadyReturnedLoan() {
        var loan = new Loan(barCode, userId);
        loan.returned();

        assertThatExceptionOfType(LoanAlreadyReturnedException.class)
                .isThrownBy(loan::returned);
    }
}
