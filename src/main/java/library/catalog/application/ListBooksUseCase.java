package library.catalog.application;

import library.UseCase;
import library.catalog.domain.Book;
import library.catalog.domain.BookRepository;
import library.catalog.domain.Isbn;

import java.util.List;
import java.util.Optional;

@UseCase
public class ListBooksUseCase {

    private final BookRepository bookRepository;

    public ListBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> listAll() {
        return (List<Book>) bookRepository.findAll();
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public Optional<Book> searchByIsbn(Isbn isbn) {
        return bookRepository.findByIsbnValue(isbn.value());
    }
}
