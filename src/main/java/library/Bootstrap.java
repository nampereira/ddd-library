package library;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.common.BarCode;
import library.catalog.domain.BookRepository;
import library.catalog.domain.Isbn;
import library.security.domain.Role;
import library.security.domain.Patron;
import library.security.domain.PatronRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class Bootstrap implements ApplicationRunner {

    private final AddBookToCatalogUseCase addBookToCatalog;
    private final RegisterBookCopyUseCase registerBookCopy;
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(AddBookToCatalogUseCase addBookToCatalog,
                     RegisterBookCopyUseCase registerBookCopy,
                     BookRepository bookRepository,
                     PatronRepository patronRepository,
                     PasswordEncoder passwordEncoder) {
        this.addBookToCatalog = addBookToCatalog;
        this.registerBookCopy = registerBookCopy;
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (bookRepository.count() == 0) {
            addBookToCatalog.execute(new Isbn("9780132350884"), List.of(
                    new AddBookToCatalogUseCase.AuthorInput("Robert C. Martin", "uncle.bob@example.com")
            )); // Clean Code
            addBookToCatalog.execute(new Isbn("9780201633610"), List.of(
                    new AddBookToCatalogUseCase.AuthorInput("Erich Gamma", "erich.gamma@example.com"),
                    new AddBookToCatalogUseCase.AuthorInput("Richard Helm", "richard.helm@example.com"),
                    new AddBookToCatalogUseCase.AuthorInput("Ralph Johnson", "ralph.johnson@example.com"),
                    new AddBookToCatalogUseCase.AuthorInput("John Vlissides", "john.vlissides@example.com")
            )); // Design Patterns
            addBookToCatalog.execute(new Isbn("9780596007126"), List.of(
                    new AddBookToCatalogUseCase.AuthorInput("Eric Freeman", "eric.freeman@example.com"),
                    new AddBookToCatalogUseCase.AuthorInput("Elisabeth Robson", "elisabeth.robson@example.com")
            )); // Head First Design Patterns

            bookRepository.findAll().forEach(book -> {
                registerBookCopy.execute(book.getIsbn(), new BarCode("BC-" + book.getIsbn().value() + "-1"));
                registerBookCopy.execute(book.getIsbn(), new BarCode("BC-" + book.getIsbn().value() + "-2"));
            });
        }

        if (patronRepository.count() == 0) {
            patronRepository.save(new Patron("admin",     passwordEncoder.encode("admin123"), Set.of(Role.ADMIN)));
            patronRepository.save(new Patron("librarian", passwordEncoder.encode("lib123"),   Set.of(Role.LIBRARIAN)));
            patronRepository.save(new Patron("patron",    passwordEncoder.encode("pat123"),   Set.of(Role.PATRON)));
        }
    }
}
