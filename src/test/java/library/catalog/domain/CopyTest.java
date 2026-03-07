package library.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CopyTest {

    private static final BookId BOOK_ID = new BookId();
    private static final BarCode BAR_CODE = new BarCode("BC-0001");

    @Test
    void newCopyIsAvailable() {
        var copy = new Copy(BOOK_ID, BAR_CODE);
        assertThat(copy.isAvailable()).isTrue();
    }

    @Test
    void rejectsNullBookId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Copy(null, BAR_CODE));
    }

    @Test
    void rejectsNullBarCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Copy(BOOK_ID, null));
    }

    @Test
    void makeUnavailableMarksCopyAsUnavailable() {
        var copy = new Copy(BOOK_ID, BAR_CODE);
        copy.makeUnavailable();
        assertThat(copy.isAvailable()).isFalse();
    }

    @Test
    void makeAvailableRestoresAvailability() {
        var copy = new Copy(BOOK_ID, BAR_CODE);
        copy.makeUnavailable();
        copy.makeAvailable();
        assertThat(copy.isAvailable()).isTrue();
    }
}
