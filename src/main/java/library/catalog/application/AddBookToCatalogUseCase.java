package library.catalog.application;

import library.UseCase;
import library.catalog.domain.Book;
import library.catalog.domain.BookAuthor;
import library.catalog.domain.BookRepository;
import library.catalog.domain.BookTitle;
import library.catalog.domain.ContactEmail;
import library.catalog.domain.Isbn;

import java.util.List;

/**
 * Use case: a librarian adds a new book title to the catalog.
 *
 * <p>Given an ISBN, this use case looks up the book's title from an external source
 * ({@link BookSearchService}) and saves a new {@link Book} to the catalog.</p>
 *
 * <p>If the ISBN is already in the catalog the database unique constraint on the
 * {@code isbn} column will reject the duplicate.</p>
 */
@UseCase
public class AddBookToCatalogUseCase {
    private final BookSearchService bookSearchService;
    private final BookRepository bookRepository;

    public AddBookToCatalogUseCase(BookSearchService bookSearchService, BookRepository bookRepository) {
        this.bookSearchService = bookSearchService;
        this.bookRepository = bookRepository;
    }

    /**
     * Looks up the title for the given ISBN, creates a {@link Book} with the given authors,
     * and persists it.
     *
     * @param isbn    the ISBN of the book to add; must be a valid ISBN-10 or ISBN-13
     * @param authors the authors of the book, each with a name and contact email
     */
    public void execute(Isbn isbn, List<AuthorInput> authors) {
        BookInformation result = bookSearchService.search(isbn);
        List<BookAuthor> authorEntities = authors.stream()
                .map(a -> new BookAuthor(a.name(), new ContactEmail(a.contactEmail())))
                .toList();
        bookRepository.save(new Book(new BookTitle(result.title()), isbn, authorEntities));
    }

    /** Input data for a single author when adding a book. */
    public record AuthorInput(String name, String contactEmail) {}
}
