package library.catalog;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.ListBooksUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.catalog.domain.BarCode;
import library.catalog.domain.Book;
import library.catalog.domain.Isbn;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    public CollectionModel<EntityModel<BookResponse>> getBooks(@RequestParam(required = false) String title,
                                                               @RequestParam(required = false) String isbn) {
        List<Book> books;
        if (isbn != null) {
            books = listBooks.searchByIsbn(new Isbn(isbn)).map(List::of).orElse(List.of());
        } else {
            books = title != null ? listBooks.searchByTitle(title) : listBooks.listAll();
        }

        List<EntityModel<BookResponse>> bookModels = books.stream()
                .map(BookResponse::from)
                .map(br -> EntityModel.of(br,
                        linkTo(methodOn(CatalogController.class).getBooks(null, br.isbn())).withSelfRel()))
                .toList();

        return CollectionModel.of(bookModels,
                linkTo(methodOn(CatalogController.class).getBooks(null, null)).withSelfRel());
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public void addBook(@RequestBody AddBookRequest request) {
        addBookToCatalog.execute(new Isbn(request.isbn()));
    }

    @PostMapping("/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerCopy(@RequestBody RegisterCopyRequest request) {
        registerBookCopy.execute(new Isbn(request.isbn()), new BarCode(request.barCode()));
    }

    record AddBookRequest(String isbn) {}

    record RegisterCopyRequest(String isbn, String barCode) {}

    record BookResponse(String title, String isbn) {
        static BookResponse from(Book book) {
            return new BookResponse(book.getTitle(), book.getIsbn().value());
        }
    }
}
