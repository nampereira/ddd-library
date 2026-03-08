package library.catalog.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CopyTest {

    private static final Isbn ISBN = new Isbn("9780132350884");
    private static final BarCode BAR_CODE = new BarCode("BC-0001");

    @Test
    void newCopyIsAvailable() {
        var copy = new Copy(ISBN, BAR_CODE);
        assertThat(copy.isAvailable()).isTrue();
    }

    @Test
    void rejectsNullIsbn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Copy(null, BAR_CODE));
    }

    @Test
    void rejectsNullBarCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Copy(ISBN, null));
    }

    @Test
    void makeUnavailableMarksCopyAsUnavailable() {
        var copy = new Copy(ISBN, BAR_CODE);
        copy.makeUnavailable();
        assertThat(copy.isAvailable()).isFalse();
    }

    @Test
    void makeAvailableRestoresAvailability() {
        var copy = new Copy(ISBN, BAR_CODE);
        copy.makeUnavailable();
        copy.makeAvailable();
        assertThat(copy.isAvailable()).isTrue();
    }
}
