package library;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.catalog.domain.BarCode;
import library.catalog.domain.BookRepository;
import library.catalog.domain.Isbn;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class Bootstrap implements ApplicationRunner {

    private final AddBookToCatalogUseCase addBookToCatalog;
    private final RegisterBookCopyUseCase registerBookCopy;
    private final BookRepository bookRepository;

    public Bootstrap(AddBookToCatalogUseCase addBookToCatalog,
                     RegisterBookCopyUseCase registerBookCopy,
                     BookRepository bookRepository) {
        this.addBookToCatalog = addBookToCatalog;
        this.registerBookCopy = registerBookCopy;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bookRepository.count() > 0) return;

        addBookToCatalog.execute(new Isbn("9780132350884")); // Clean Code
        addBookToCatalog.execute(new Isbn("9780201633610")); // Design Patterns
        addBookToCatalog.execute(new Isbn("9780596007126")); // Head First Design Patterns

        bookRepository.findAll().forEach(book -> {
            registerBookCopy.execute(book.getBookId(), new BarCode("BC-" + book.getIsbn().value() + "-1"));
            registerBookCopy.execute(book.getBookId(), new BarCode("BC-" + book.getIsbn().value() + "-2"));
        });
    }
}
