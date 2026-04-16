package library.lending.application;

import library.common.BarCode;
import library.lending.domain.Loan;
import library.lending.domain.LoanAlreadyReturnedException;
import library.lending.domain.LoanId;
import library.lending.domain.LoanNotFoundException;
import library.lending.domain.LoanRepository;
import library.lending.domain.PatronId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnBookUseCaseTest {

    @Mock LoanRepository loanRepository;

    ReturnBookUseCase useCase;

    private final LoanId loanId = new LoanId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        useCase = new ReturnBookUseCase(loanRepository);
        // findByIdOrThrow is a default method; Mockito stubs it as null by default.
        // Use callRealMethod so the real implementation delegates to findByLoanId.
        doCallRealMethod().when(loanRepository).findByIdOrThrow(any());
    }

    @Test
    void marksLoanReturnedAndSaves() {
        Loan loan = new Loan(new BarCode("BC-001"), new PatronId(UUID.randomUUID()));
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(loan));

        useCase.execute(loanId);

        verify(loanRepository).save(loan);
    }

    @Test
    void throwsLoanNotFoundWhenIdDoesNotExist() {
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(LoanNotFoundException.class)
                .isThrownBy(() -> useCase.execute(loanId));
    }

    @Test
    void throwsLoanAlreadyReturnedWhenLoanIsInactive() {
        Loan alreadyReturned = new Loan(new BarCode("BC-001"), new PatronId(UUID.randomUUID()));
        alreadyReturned.returned();
        when(loanRepository.findByLoanId(loanId)).thenReturn(Optional.of(alreadyReturned));

        assertThatExceptionOfType(LoanAlreadyReturnedException.class)
                .isThrownBy(() -> useCase.execute(loanId))
                .withMessageContaining(alreadyReturned.getLoanId().id().toString());
    }
}
