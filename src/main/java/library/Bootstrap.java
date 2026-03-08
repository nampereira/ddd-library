package library;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.catalog.domain.BarCode;
import library.catalog.domain.BookRepository;
import library.catalog.domain.Isbn;
import library.security.domain.Role;
import library.security.domain.User;
import library.security.domain.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("dev")
public class Bootstrap implements ApplicationRunner {

    private final AddBookToCatalogUseCase addBookToCatalog;
    private final RegisterBookCopyUseCase registerBookCopy;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(AddBookToCatalogUseCase addBookToCatalog,
                     RegisterBookCopyUseCase registerBookCopy,
                     BookRepository bookRepository,
                     UserRepository userRepository,
                     PasswordEncoder passwordEncoder) {
        this.addBookToCatalog = addBookToCatalog;
        this.registerBookCopy = registerBookCopy;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bookRepository.count() == 0) {
            addBookToCatalog.execute(new Isbn("9780132350884")); // Clean Code
            addBookToCatalog.execute(new Isbn("9780201633610")); // Design Patterns
            addBookToCatalog.execute(new Isbn("9780596007126")); // Head First Design Patterns

            bookRepository.findAll().forEach(book -> {
                registerBookCopy.execute(book.getBookId(), new BarCode("BC-" + book.getIsbn().value() + "-1"));
                registerBookCopy.execute(book.getBookId(), new BarCode("BC-" + book.getIsbn().value() + "-2"));
            });
        }

        if (userRepository.count() == 0) {
            userRepository.save(new User("admin",     passwordEncoder.encode("admin123"), Set.of(Role.ADMIN)));
            userRepository.save(new User("librarian", passwordEncoder.encode("lib123"),   Set.of(Role.LIBRARIAN)));
            userRepository.save(new User("patron",    passwordEncoder.encode("pat123"),   Set.of(Role.PATRON)));
        }
    }
}
