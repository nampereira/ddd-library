package library.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class IsbnTest {

    @Test
    void acceptsValidIsbn13() {
        var isbn = new Isbn("9780132350884");
        assertThat(isbn.value()).isEqualTo("9780132350884");
    }

    @Test
    void acceptsValidIsbn10() {
        var isbn = new Isbn("0132350882");
        assertThat(isbn.value()).isEqualTo("0132350882");
    }

    @Test
    void rejectsInvalidIsbn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Isbn("1234567890123"))
                .withMessageContaining("invalid isbn");
    }

    @Test
    void rejectsRandomString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Isbn("not-an-isbn"));
    }
}
