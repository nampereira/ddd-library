package library.catalog.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Note: the @OneToMany collection uses BookAuthor (not Author) to make the aggregate boundary
// explicit in the type name: a BookAuthor only exists as part of a Book and has no life outside it.

/**
 * Aggregate root representing a book title in the library catalog.
 *
 * <p>A {@code Book} is defined by its {@link Isbn} — the globally recognised, unique identifier
 * for a book title — and a human-readable {@code title}. Two {@code Book} instances are
 * considered equal if they share the same ISBN, regardless of their database primary key.</p>
 *
 * <p>Physical copies of this title are modelled separately as {@link BookCopy} entities. A book
 * in the catalog may have zero or more copies.</p>
 *
 * <h2>Database identity vs. domain identity</h2>
 * <p>The database uses an auto-generated {@code Long pk} as the physical primary key to avoid
 * storing a string (ISBN) as a primary key. The domain identity remains the {@link Isbn}.</p>
 */
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title"))
    private BookTitle title;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "isbn", unique = true))
    private Isbn isbn;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "book_pk")
    private List<BookAuthor> authors = new ArrayList<>();

    protected Book() {
    }

    /**
     * Creates a new book with the given title, ISBN, and authors.
     *
     * @param title   the human-readable title of the book; must not be {@code null}
     * @param isbn    the validated ISBN that uniquely identifies this title; must not be {@code null}
     * @param authors the authors of this book; must not be {@code null} or empty
     */
    public Book(BookTitle title, Isbn isbn, List<BookAuthor> authors) {
        Assert.notNull(title, "title must not be null");
        Assert.notNull(isbn, "isbn must not be null");
        Assert.notEmpty(authors, "a book must have at least one author");
        this.title = title;
        this.isbn = isbn;
        this.authors = new ArrayList<>(authors);
    }

    /** Returns the title of this book. */
    public BookTitle getTitle() {
        return title;
    }

    /** Returns the ISBN of this book. */
    public Isbn getIsbn() {
        return isbn;
    }

    /** Returns an unmodifiable view of the authors of this book. */
    public List<BookAuthor> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    /** Two books are equal when they share the same ISBN. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }
}
