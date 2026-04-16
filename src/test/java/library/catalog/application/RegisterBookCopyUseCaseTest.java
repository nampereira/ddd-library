package library.catalog.application;

import library.catalog.domain.BookCopy;
import library.catalog.domain.BookCopyRepository;
import library.catalog.domain.BookNotFoundException;
import library.catalog.domain.BookRepository;
import library.catalog.domain.BookTitle;
import library.catalog.domain.DuplicateBarCodeException;
import library.catalog.domain.Isbn;
import library.catalog.domain.Book;
import library.catalog.domain.BookAuthor;
import library.catalog.domain.ContactEmail;
import library.common.BarCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterBookCopyUseCaseTest {

    @Mock BookCopyRepository copyRepository;
    @Mock BookRepository bookRepository;

    RegisterBookCopyUseCase useCase;

    private static final Isbn ISBN = new Isbn("9780132350884");
    private static final BarCode BAR_CODE = new BarCode("BC-001");

    @BeforeEach
    void setUp() {
        useCase = new RegisterBookCopyUseCase(copyRepository, bookRepository);
    }

    @Test
    void savesCopyWhenBookExistsAndBarcodeIsNew() {
        Book book = new Book(new BookTitle("Clean Code"), ISBN,
                List.of(new BookAuthor("Author", new ContactEmail("a@b.com"))));
        when(bookRepository.findByIsbnValue(ISBN.value())).thenReturn(Optional.of(book));
        when(copyRepository.findByBarCode(BAR_CODE)).thenReturn(Optional.empty());

        useCase.execute(ISBN, BAR_CODE);

        ArgumentCaptor<BookCopy> saved = ArgumentCaptor.forClass(BookCopy.class);
        verify(copyRepository).save(saved.capture());
        assertThat(saved.getValue().getBarCode()).isEqualTo(BAR_CODE);
    }

    @Test
    void throwsBookNotFoundWhenIsbnNotInCatalog() {
        when(bookRepository.findByIsbnValue(ISBN.value())).thenReturn(Optional.empty());

        assertThatExceptionOfType(BookNotFoundException.class)
                .isThrownBy(() -> useCase.execute(ISBN, BAR_CODE));
    }

    @Test
    void throwsDuplicateBarCodeWhenBarcodeAlreadyExists() {
        Book book = new Book(new BookTitle("Clean Code"), ISBN,
                List.of(new BookAuthor("Author", new ContactEmail("a@b.com"))));
        when(bookRepository.findByIsbnValue(ISBN.value())).thenReturn(Optional.of(book));
        when(copyRepository.findByBarCode(BAR_CODE)).thenReturn(Optional.of(new BookCopy(ISBN, BAR_CODE)));

        assertThatExceptionOfType(DuplicateBarCodeException.class)
                .isThrownBy(() -> useCase.execute(ISBN, BAR_CODE));
    }
}
