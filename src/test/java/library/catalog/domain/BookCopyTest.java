package library.catalog.domain;

import library.common.BarCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


class BookCopyTest {

    private static final Isbn ISBN = new Isbn("9780132350884");
    private static final BarCode BAR_CODE = new BarCode("BC-0001");

    @Test
    void rejectsNullIsbn() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BookCopy(null, BAR_CODE));
    }

    @Test
    void rejectsNullBarCode() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BookCopy(ISBN, null));
    }
}
