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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanBookServiceTest {

    @Mock
    LoanRepository loanRepository;

    private LoanBookService loanBookService;
    private BarCode barCode;
    private PatronId patronId;

    @BeforeEach
    void setUp() {
        loanBookService = new LoanBookService(loanRepository);
        barCode = new BarCode("BC-0001");
        patronId = new PatronId(UUID.randomUUID());
    }

    @Test
    void rejectsReferenceOnlyCopy() {
        BarCode refCode = new BarCode("REF-0001");

        assertThatExceptionOfType(ReferenceOnlyException.class)
                .isThrownBy(() -> loanBookService.loan(refCode, patronId));

        // availability must not be queried — reference check is earlier and cheaper
        verifyNoInteractions(loanRepository);
    }

    @Test
    void rejectsUnavailableCopy() {
        when(loanRepository.isAvailable(barCode)).thenReturn(false);

        assertThatExceptionOfType(CopyNotAvailableException.class)
                .isThrownBy(() -> loanBookService.loan(barCode, patronId));
    }

    @Test
    void createsLoanForAvailableCopy() {
        when(loanRepository.isAvailable(barCode)).thenReturn(true);

        var loan = loanBookService.loan(barCode, patronId);

        assertThat(loan).isNotNull();
        assertThat(loan.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(LoanCreated.class);
    }
}
