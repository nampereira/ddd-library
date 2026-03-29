package library.lending.application;

import library.catalog.domain.BookCopy;
import library.catalog.domain.BookCopyNotFoundException;
import library.catalog.domain.BookCopyRepository;
import library.catalog.domain.Isbn;
import library.common.BarCode;
import library.lending.domain.CopyNotAvailableException;
import library.lending.domain.Loan;
import library.lending.domain.LoanBookService;
import library.lending.domain.LoanRepository;
import library.lending.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanBookUseCaseTest {

    @Mock LoanRepository loanRepository;
    @Mock BookCopyRepository bookCopyRepository;
    @Mock LoanBookService loanBookService;

    LoanBookUseCase useCase;

    private static final String BAR_CODE = "BC-001";
    private final UserId userId = new UserId(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        useCase = new LoanBookUseCase(loanRepository, bookCopyRepository, loanBookService);
    }

    @Test
    void savesAndReturnsLoanWhenCopyExistsAndIsAvailable() {
        BarCode bc = new BarCode(BAR_CODE);
        Loan loan = new Loan(bc, userId);
        when(bookCopyRepository.findByBarCode(bc)).thenReturn(Optional.of(new BookCopy(new Isbn("9780132350884"), bc)));
        when(loanBookService.loan(bc, userId)).thenReturn(loan);
        when(loanRepository.save(loan)).thenReturn(loan);

        Loan result = useCase.execute(BAR_CODE, userId);

        assertThat(result).isSameAs(loan);
    }

    @Test
    void throwsBookCopyNotFoundWhenNoCopyWithGivenBarcode() {
        when(bookCopyRepository.findByBarCode(any())).thenReturn(Optional.empty());

        assertThatExceptionOfType(BookCopyNotFoundException.class)
                .isThrownBy(() -> useCase.execute(BAR_CODE, userId));
    }

    @Test
    void throwsCopyNotAvailableWhenCopyIsAlreadyOnLoan() {
        BarCode bc = new BarCode(BAR_CODE);
        when(bookCopyRepository.findByBarCode(bc))
                .thenReturn(Optional.of(new BookCopy(new Isbn("9780132350884"), bc)));
        when(loanBookService.loan(bc, userId)).thenThrow(new CopyNotAvailableException(bc));

        assertThatExceptionOfType(CopyNotAvailableException.class)
                .isThrownBy(() -> useCase.execute(BAR_CODE, userId));
    }
}
