package library.catalog.domain;

import library.common.BarCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BarCodeTest {

    @Test
    void rejectsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BarCode(null));
    }

    @Test
    void rejectsBlank() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BarCode("   "));
    }

    @Test
    void rejectsEmptyString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BarCode(""));
    }

    @Test
    void rejectsUnknownPrefix() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new BarCode("LIB-0001"))
                .withMessageContaining("BC- or REF-");
    }

    @Test
    void acceptsCirculatingPrefix() {
        var barCode = new BarCode("BC-0001");
        assertThat(barCode.copyType()).isEqualTo(BarCode.CopyType.CIRCULATING);
    }

    @Test
    void acceptsReferencePrefix() {
        var barCode = new BarCode("REF-0001");
        assertThat(barCode.copyType()).isEqualTo(BarCode.CopyType.REFERENCE);
    }
}
