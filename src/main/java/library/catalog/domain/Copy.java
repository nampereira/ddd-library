package library.catalog.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.util.Assert;

@Entity
public class Copy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "copy_id"))
    private CopyId copyId;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "isbn"))
    private Isbn isbn;
    @Embedded
    private BarCode barCode;
    private boolean available;

    protected Copy() {
    }

    public Copy(Isbn isbn, BarCode barCode) {
        Assert.notNull(isbn, "isbn must not be null");
        Assert.notNull(barCode, "barCode must not be null");
        this.copyId = new CopyId();
        this.isbn = isbn;
        this.barCode = barCode;
        this.available = true;
    }

    public void makeUnavailable() {
        this.available = false;
    }

    public void makeAvailable() {
        this.available = true;
    }

    public boolean isAvailable() {
        return available;
    }
}
