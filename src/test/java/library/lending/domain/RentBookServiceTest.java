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
class RentBookServiceTest {

    @Mock
    LoanRepository loanRepository;

    private RentBookService rentBookService;
    private CopyId copyId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        rentBookService = new RentBookService(loanRepository);
        copyId = new CopyId();
        userId = new UserId();
    }

    @Test
    void rejectsUnavailableCopy() {
        when(loanRepository.isAvailable(copyId)).thenReturn(false);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> rentBookService.rent(copyId, userId))
                .withMessageContaining("not available");
    }

    @Test
    void createsLoanForAvailableCopy() {
        when(loanRepository.isAvailable(copyId)).thenReturn(true);

        var loan = rentBookService.rent(copyId, userId);

        assertThat(loan).isNotNull();
        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }
}
