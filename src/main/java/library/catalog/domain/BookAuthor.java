package library.catalog.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * Entity representing an author of a book, scoped to the {@link Book} aggregate.
 *
 * <p>The name {@code BookAuthor} — rather than just {@code Author} — is intentional: it
 * makes the aggregate boundary visible in the type name. {@code BookAuthor} is always accessed
 * through its {@link Book} aggregate root; there is no standalone {@code BookAuthorRepository}.
 * Authors are created when a book is added to the catalog and removed when their book is removed.</p>
 *
 * <p>The JPA association is owned by {@link Book} via a {@code @OneToMany} with
 * {@code cascade = ALL} and {@code orphanRemoval = true}, so authors are created and
 * deleted together with their book.</p>
 *
 * <p>Each author is uniquely identified by their {@link ContactEmail}, which serves as the
 * domain identity and allows librarians to reference a specific author when editing their
 * bio without needing a standalone repository.</p>
 */
@Entity
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "contact_email"))
    private ContactEmail contactEmail;

    private String name;

    private String bio;

    protected BookAuthor() {
    }

    /**
     * Creates a new author with the given name and contact email.
     *
     * @param name         the author's full name; must not be blank
     * @param contactEmail the author's contact email; must not be {@code null} and must be valid
     */
    public BookAuthor(String name, ContactEmail contactEmail) {
        Assert.hasText(name, "name must not be blank");
        Assert.notNull(contactEmail, "contactEmail must not be null");
        this.name = name;
        this.contactEmail = contactEmail;
    }

    /** Returns the domain identity of this author. */
    public ContactEmail getContactEmail() {
        return contactEmail;
    }

    /** Returns the author's full name. */
    public String getName() {
        return name;
    }

    /** Returns the author's biographical note, or {@code null} if not set. */
    public String getBio() {
        return bio;
    }

    /** Sets the author's biographical note. May be {@code null} to clear it. */
    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookAuthor that = (BookAuthor) o;
        return Objects.equals(contactEmail, that.contactEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contactEmail);
    }
}
