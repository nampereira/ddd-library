package library.lending.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LoanTest {

    private CopyId copyId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        copyId = new CopyId();
        userId = new UserId();
    }

    @Test
    void rejectsNullCopyId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(null, userId));
    }

    @Test
    void rejectsNullUserId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(copyId, null));
    }

    @Test
    void registersLoanCreatedEventOnCreation() {
        var loan = new Loan(copyId, userId);

        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }

    @Test
    void registersLoanClosedEventOnReturn() {
        var loan = new Loan(copyId, userId);

        loan.returned();

        assertThat(loan.getDomainEvents())
                .anyMatch(e -> e instanceof LoanClosed);
    }
}
