package library.catalog.application;

import jakarta.validation.constraints.NotNull;
import library.UseCase;
import library.common.BarCode;
import library.catalog.domain.BookCopy;
import library.catalog.domain.BookCopyRepository;
import library.catalog.domain.BookCopyNotFoundException;
import library.catalog.domain.BookNotFoundException;
import library.catalog.domain.BookRepository;
import library.catalog.domain.DuplicateBarCodeException;
import library.catalog.domain.Isbn;

/**
 * Use case: a librarian registers a new physical copy of an existing book.
 *
 * <p>Creates a {@link BookCopy} for the given ISBN and barcode, marks it as available,
 * and persists it.</p>
 *
 * <p>The book must already exist in the catalog. The copy references the book by its
 * {@link Isbn} value only — there is no direct object reference between the two aggregates.</p>
 */
@UseCase
public class RegisterBookCopyUseCase {
    private final BookCopyRepository copyRepository;
    private final BookRepository bookRepository;

    public RegisterBookCopyUseCase(BookCopyRepository copyRepository, BookRepository bookRepository) {
        this.copyRepository = copyRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Creates and persists a new copy.
     *
     * @param isbn    the ISBN of the book title this copy belongs to; must not be {@code null}
     * @param barCode the physical barcode label on this copy; must not be {@code null}
     * @throws BookNotFoundException    if no book with the given ISBN exists in the catalog
     * @throws DuplicateBarCodeException if a copy with the given barcode already exists
     */
    public void execute(@NotNull Isbn isbn, @NotNull BarCode barCode) {
        bookRepository.findByIsbnValue(isbn.value())
                .orElseThrow(() -> new BookNotFoundException(isbn));
        if (copyRepository.findByBarCode(barCode).isPresent()) {
            throw new DuplicateBarCodeException(barCode);
        }
        copyRepository.save(new BookCopy(isbn, barCode));
    }
}
