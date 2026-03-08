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
    CopyAvailabilityService availabilityService;

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
                .isThrownBy(() -> new Loan(null, userId, availabilityService));
    }

    @Test
    void rejectsNullUserId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(copyId, null, availabilityService));
    }

    @Test
    void rejectsUnavailableCopy() {
        when(availabilityService.isAvailable(copyId)).thenReturn(false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Loan(copyId, userId, availabilityService))
                .withMessageContaining("not available");
    }

    @Test
    void registersLoanCreatedEventOnCreation() {
        when(availabilityService.isAvailable(copyId)).thenReturn(true);

        var loan = new Loan(copyId, userId, availabilityService);

        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }

    @Test
    void registersLoanClosedEventOnReturn() {
        when(availabilityService.isAvailable(copyId)).thenReturn(true);
        var loan = new Loan(copyId, userId, availabilityService);

        loan.returned();

        assertThat(loan.getDomainEvents())
                .anyMatch(e -> e instanceof LoanClosed);
    }
}
