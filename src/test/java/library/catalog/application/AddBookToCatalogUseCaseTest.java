package library.catalog.application;

import library.catalog.domain.Book;
import library.catalog.domain.BookInformationNotFoundException;
import library.catalog.domain.BookRepository;
import library.catalog.domain.BookTitle;
import library.catalog.domain.Isbn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddBookToCatalogUseCaseTest {

    @Mock BookSearchService bookSearchService;
    @Mock BookRepository bookRepository;

    AddBookToCatalogUseCase useCase;

    private static final Isbn CLEAN_CODE = new Isbn("9780132350884");

    @BeforeEach
    void setUp() {
        useCase = new AddBookToCatalogUseCase(bookSearchService, bookRepository);
    }

    @Test
    void savesBookWithTitleFromSearchService() {
        when(bookSearchService.search(CLEAN_CODE)).thenReturn(new BookInformation("Clean Code"));

        useCase.execute(CLEAN_CODE, List.of(
                new AddBookToCatalogUseCase.AuthorInput("Robert C. Martin", "uncle.bob@example.com")));

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo(new BookTitle("Clean Code"));
        assertThat(saved.getValue().getIsbn()).isEqualTo(CLEAN_CODE);
    }

    @Test
    void propagatesBookInformationNotFoundExceptionFromSearchService() {
        doThrow(new BookInformationNotFoundException(CLEAN_CODE))
                .when(bookSearchService).search(CLEAN_CODE);

        assertThatExceptionOfType(BookInformationNotFoundException.class)
                .isThrownBy(() -> useCase.execute(CLEAN_CODE, List.of(
                        new AddBookToCatalogUseCase.AuthorInput("Author", "a@b.com"))));
    }

    @Test
    void rejectsEmptyAuthorsList() {
        when(bookSearchService.search(CLEAN_CODE)).thenReturn(new BookInformation("Clean Code"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> useCase.execute(CLEAN_CODE, List.of()))
                .withMessageContaining("at least one author");
    }

    @Test
    void savesAllAuthors() {
        when(bookSearchService.search(any())).thenReturn(new BookInformation("Design Patterns"));

        useCase.execute(new Isbn("9780201633610"), List.of(
                new AddBookToCatalogUseCase.AuthorInput("Erich Gamma", "e.gamma@example.com"),
                new AddBookToCatalogUseCase.AuthorInput("Richard Helm", "r.helm@example.com")));

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthors()).hasSize(2);
    }
}
