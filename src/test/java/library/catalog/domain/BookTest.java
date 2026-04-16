package library.catalog.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BookTest {

    private static final Isbn VALID_ISBN = new Isbn("9780132350884");
    private static final BookTitle VALID_TITLE = new BookTitle("Clean Code");
    private static final List<BookAuthor> ONE_AUTHOR = List.of(
            new BookAuthor("Robert C. Martin", new ContactEmail("uncle.bob@example.com")));

    @Test
    void createsBookWithTitleAndIsbn() {
        var book = new Book(VALID_TITLE, VALID_ISBN, ONE_AUTHOR);
        assertThat(book.getTitle()).isEqualTo(VALID_TITLE);
        assertThat(book.getIsbn()).isEqualTo(VALID_ISBN);
    }

    @Test
    void rejectsNullTitle() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Book(null, VALID_ISBN, ONE_AUTHOR));
    }

    @Test
    void rejectsNullIsbn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Book(VALID_TITLE, null, ONE_AUTHOR));
    }

    @Test
    void rejectsEmptyAuthors() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Book(VALID_TITLE, VALID_ISBN, List.of()));
    }

    @Test
    void getAuthorsReturnsUnmodifiableList() {
        var book = new Book(VALID_TITLE, VALID_ISBN, ONE_AUTHOR);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> book.getAuthors().add(
                        new BookAuthor("Extra", new ContactEmail("extra@example.com"))));
    }
}
