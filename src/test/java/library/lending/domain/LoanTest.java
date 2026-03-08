package library.lending.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanTest {

    @Mock
    LoanRepository loanRepository;

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
                .isThrownBy(() -> new Loan(null, userId, loanRepository));
    }

    @Test
    void rejectsNullUserId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(copyId, null, loanRepository));
    }

    @Test
    void rejectsUnavailableCopy() {
        when(loanRepository.isAvailable(copyId)).thenReturn(false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(copyId, userId, loanRepository))
                .withMessageContaining("not available");
    }

    @Test
    void registersLoanCreatedEventOnCreation() {
        when(loanRepository.isAvailable(copyId)).thenReturn(true);

        var loan = new Loan(copyId, userId, loanRepository);

        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }

    @Test
    void registersLoanClosedEventOnReturn() {
        when(loanRepository.isAvailable(copyId)).thenReturn(true);
        var loan = new Loan(copyId, userId, loanRepository);

        loan.returned();

        assertThat(loan.getDomainEvents())
                .anyMatch(e -> e instanceof LoanClosed);
    }
}
