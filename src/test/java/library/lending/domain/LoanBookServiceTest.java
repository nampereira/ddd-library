package library.lending.domain;

import library.common.BarCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanBookServiceTest {

    @Mock
    LoanRepository loanRepository;

    private LoanBookService loanBookService;
    private BarCode barCode;
    private UserId userId;

    @BeforeEach
    void setUp() {
        loanBookService = new LoanBookService(loanRepository);
        barCode = new BarCode("BC-0001");
        userId = new UserId(UUID.randomUUID());
    }

    @Test
    void rejectsUnavailableCopy() {
        when(loanRepository.isAvailable(barCode)).thenReturn(false);

        assertThatExceptionOfType(CopyNotAvailableException.class)
                .isThrownBy(() -> loanBookService.loan(barCode, userId));
    }

    @Test
    void createsLoanForAvailableCopy() {
        when(loanRepository.isAvailable(barCode)).thenReturn(true);

        var loan = loanBookService.loan(barCode, userId);

        assertThat(loan).isNotNull();
        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }
}
