package library.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BookTest {

    private static final Isbn VALID_ISBN = new Isbn("9780132350884");

    @Test
    void createsBookWithTitleAndIsbn() {
        var book = new Book("Clean Code", VALID_ISBN);
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getIsbn()).isEqualTo(VALID_ISBN);
        assertThat(book.getBookId()).isNotNull();
    }

    @Test
    void rejectsNullTitle() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Book(null, VALID_ISBN));
    }

    @Test
    void rejectsNullIsbn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Book("Clean Code", null));
    }
}
