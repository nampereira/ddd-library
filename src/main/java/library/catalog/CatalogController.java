package library.catalog;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.ListBooksUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.catalog.domain.BarCode;
import library.catalog.domain.Book;
import library.catalog.domain.BookId;
import library.catalog.domain.Isbn;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final AddBookToCatalogUseCase addBookToCatalog;
    private final RegisterBookCopyUseCase registerBookCopy;
    private final ListBooksUseCase listBooks;

    public CatalogController(AddBookToCatalogUseCase addBookToCatalog,
                             RegisterBookCopyUseCase registerBookCopy,
                             ListBooksUseCase listBooks) {
        this.addBookToCatalog = addBookToCatalog;
        this.registerBookCopy = registerBookCopy;
        this.listBooks = listBooks;
    }

    @GetMapping("/books")
    public List<BookResponse> getBooks(@RequestParam(required = false) String title,
                                       @RequestParam(required = false) String isbn) {
        if (isbn != null) {
            return listBooks.searchByIsbn(new Isbn(isbn))
                    .map(BookResponse::from)
                    .map(List::of)
                    .orElse(List.of());
        }
        List<Book> books = title != null ? listBooks.searchByTitle(title) : listBooks.listAll();
        return books.stream().map(BookResponse::from).toList();
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public void addBook(@RequestBody AddBookRequest request) {
        addBookToCatalog.execute(new Isbn(request.isbn()));
    }

    @PostMapping("/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerCopy(@RequestBody RegisterCopyRequest request) {
        registerBookCopy.execute(new BookId(request.bookId()), new BarCode(request.barCode()));
    }

    record AddBookRequest(String isbn) {}

    record RegisterCopyRequest(UUID bookId, String barCode) {}

    record BookResponse(UUID bookId, String title, String isbn) {
        static BookResponse from(Book book) {
            return new BookResponse(book.getBookId().id(), book.getTitle(), book.getIsbn().value());
        }
    }
}
