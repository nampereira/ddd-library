package library.catalog;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.EditAuthorDetailsUseCase;
import library.catalog.application.ListBooksUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import library.catalog.domain.Book;
import library.catalog.domain.BookAuthor;
import library.catalog.domain.ContactEmail;
import library.catalog.domain.Isbn;
import library.common.BarCode;

import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for the catalog functional area.
 *
 * <p>Exposes four endpoints:</p>
 * <ul>
 *   <li>{@code GET /catalog/books} — list or search books (no authentication required)</li>
 *   <li>{@code POST /catalog/books} — add a book by ISBN (LIBRARIAN or ADMIN)</li>
 *   <li>{@code POST /catalog/copies} — register a physical copy (LIBRARIAN or ADMIN)</li>
 *   <li>{@code PATCH /catalog/authors/{contactEmail}} — edit an author's bio (LIBRARIAN or ADMIN)</li>
 * </ul>
 *
 * <p>Responses follow the HATEOAS HAL format: each book is wrapped in an {@link EntityModel}
 * with a {@code self} link, and the list is wrapped in a {@link CollectionModel} with its own
 * {@code self} link.</p>
 */
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final AddBookToCatalogUseCase addBookToCatalog;
    private final RegisterBookCopyUseCase registerBookCopy;
    private final ListBooksUseCase listBooks;
    private final EditAuthorDetailsUseCase editAuthorDetails;

    public CatalogController(AddBookToCatalogUseCase addBookToCatalog,
                             RegisterBookCopyUseCase registerBookCopy,
                             ListBooksUseCase listBooks,
                             EditAuthorDetailsUseCase editAuthorDetails) {
        this.addBookToCatalog = addBookToCatalog;
        this.registerBookCopy = registerBookCopy;
        this.listBooks = listBooks;
        this.editAuthorDetails = editAuthorDetails;
    }

    /**
     * Lists all books, optionally filtered by title or ISBN.
     *
     * @param title partial, case-insensitive title filter (optional)
     * @param isbn  exact ISBN filter (optional); takes precedence over {@code title}
     * @return a HAL collection of book resources, each with a {@code self} link
     */
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

    /**
     * Adds a new book to the catalog by looking up its title from Open Library.
     *
     * @param request the request body containing the ISBN and authors (each with name and email)
     */
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public void addBook(@Valid @RequestBody AddBookRequest request) {
        List<AddBookToCatalogUseCase.AuthorInput> authors = request.authors().stream()
                .map(a -> new AddBookToCatalogUseCase.AuthorInput(a.name(), a.contactEmail()))
                .toList();
        addBookToCatalog.execute(new Isbn(request.isbn()), authors);
    }

    /**
     * Registers a new physical copy of an existing book.
     *
     * @param request the request body containing the ISBN and barcode of the new copy
     */
    @PostMapping("/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerCopy(@Valid @RequestBody RegisterCopyRequest request) {
        registerBookCopy.execute(new Isbn(request.isbn()), new BarCode(request.barCode()));
    }

    /**
     * Edits the bio of an existing author, identified by their contact email.
     *
     * <p>The {@code contactEmail} is returned in the {@link AuthorResponse} embedded in each
     * {@link BookResponse} from {@code GET /catalog/books}.</p>
     *
     * @param contactEmail the contact email (domain identity) of the author to update
     * @param request      the new bio; may be {@code null} to clear it
     */
    @PatchMapping("/authors/{contactEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editAuthorDetails(@PathVariable String contactEmail,
                                  @Valid @RequestBody EditAuthorRequest request) {
        editAuthorDetails.execute(new ContactEmail(contactEmail), request.bio());
    }

    /** Request body for {@code POST /catalog/books}. */
    record AddBookRequest(@NotBlank String isbn, @NotEmpty List<@Valid AuthorRequest> authors) {}

    /** Author data within {@link AddBookRequest}. */
    record AuthorRequest(@NotBlank String name, @NotBlank String contactEmail) {}

    /** Request body for {@code POST /catalog/copies}. */
    record RegisterCopyRequest(@NotBlank String isbn, @NotBlank String barCode) {}

    /** Request body for {@code PATCH /catalog/authors/{contactEmail}}. */
    record EditAuthorRequest(String bio) {}

    /**
     * Response fragment representing a single author within a {@link BookResponse}.
     *
     * <p>{@code contactEmail} is the domain identity the client must supply to
     * {@code PATCH /catalog/authors/{contactEmail}} when editing this author's bio.</p>
     */
    record AuthorResponse(String contactEmail, String name, String bio) {
        static AuthorResponse from(BookAuthor author) {
            return new AuthorResponse(
                    author.getContactEmail().value(),
                    author.getName(),
                    author.getBio());
        }
    }

    /** Response body representing a book in the catalog. */
    record BookResponse(String title, String isbn, List<AuthorResponse> authors) {
        static BookResponse from(Book book) {
            return new BookResponse(
                    book.getTitle().value(),
                    book.getIsbn().value(),
                    book.getAuthors().stream().map(AuthorResponse::from).toList());
        }
    }
}
