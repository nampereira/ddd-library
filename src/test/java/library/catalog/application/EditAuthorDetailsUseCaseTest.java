package library.catalog.application;

import library.catalog.domain.AuthorNotFoundException;
import library.catalog.domain.Book;
import library.catalog.domain.BookAuthor;
import library.catalog.domain.BookRepository;
import library.catalog.domain.BookTitle;
import library.catalog.domain.ContactEmail;
import library.catalog.domain.Isbn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditAuthorDetailsUseCaseTest {

    @Mock BookRepository bookRepository;

    EditAuthorDetailsUseCase useCase;

    private static final ContactEmail EMAIL = new ContactEmail("uncle.bob@example.com");

    @BeforeEach
    void setUp() {
        useCase = new EditAuthorDetailsUseCase(bookRepository);
    }

    @Test
    void updatesBioWhenAuthorExists() {
        BookAuthor author = new BookAuthor("Robert C. Martin", EMAIL);
        Book book = new Book(new BookTitle("Clean Code"), new Isbn("9780132350884"), List.of(author));
        when(bookRepository.findByAuthors_ContactEmail_Value(EMAIL.value())).thenReturn(Optional.of(book));

        useCase.execute(EMAIL, "The author of Clean Code");

        assertThat(author.getBio()).isEqualTo("The author of Clean Code");
    }

    @Test
    void clearsBioWhenNullBioIsProvided() {
        BookAuthor author = new BookAuthor("Robert C. Martin", EMAIL);
        author.setBio("Old bio");
        Book book = new Book(new BookTitle("Clean Code"), new Isbn("9780132350884"), List.of(author));
        when(bookRepository.findByAuthors_ContactEmail_Value(EMAIL.value())).thenReturn(Optional.of(book));

        useCase.execute(EMAIL, null);

        assertThat(author.getBio()).isNull();
    }

    @Test
    void throwsAuthorNotFoundWhenEmailHasNoMatch() {
        when(bookRepository.findByAuthors_ContactEmail_Value(EMAIL.value())).thenReturn(Optional.empty());

        assertThatExceptionOfType(AuthorNotFoundException.class)
                .isThrownBy(() -> useCase.execute(EMAIL, "Some bio"))
                .withMessageContaining(EMAIL.value());
    }
}
