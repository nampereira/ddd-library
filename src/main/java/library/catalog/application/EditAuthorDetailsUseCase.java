package library.catalog.application;

import library.UseCase;
import library.catalog.domain.AuthorNotFoundException;
import library.catalog.domain.Book;
import library.catalog.domain.BookAuthor;
import library.catalog.domain.BookRepository;
import library.catalog.domain.ContactEmail;

/**
 * Use case: a librarian edits the bio of an existing author.
 *
 * <p>Authors are always part of a {@link Book} aggregate — they cannot be created or deleted
 * independently. This use case only updates the optional {@code bio} on an author that was
 * already added to the catalog as part of a book.</p>
 *
 * <p>The author is located by traversing the {@link BookRepository}: the repository finds the
 * book whose authors collection contains an author with the given {@link ContactEmail}, then
 * the author is updated in place. Because the use case is transactional (via {@code @UseCase}),
 * JPA dirty-checking will flush the change automatically on commit.</p>
 */
@UseCase
public class EditAuthorDetailsUseCase {

    private final BookRepository bookRepository;

    public EditAuthorDetailsUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Updates the bio of an existing author identified by their contact email.
     *
     * @param contactEmail the domain identity of the author to update
     * @param bio          the new biographical note; may be {@code null} to clear it
     * @throws AuthorNotFoundException if no author with the given email exists
     */
    public void execute(ContactEmail contactEmail, String bio) {
        Book book = bookRepository.findByAuthors_ContactEmail_Value(contactEmail.value())
                .orElseThrow(() -> new AuthorNotFoundException(contactEmail));
        BookAuthor author = book.getAuthors().stream()
                .filter(a -> a.getContactEmail().equals(contactEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Author found via repository query but missing from loaded aggregate: " + contactEmail.value()));
        author.setBio(bio);
    }
}
