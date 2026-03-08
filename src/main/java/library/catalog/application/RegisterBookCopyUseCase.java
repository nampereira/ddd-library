package library.catalog.application;

import jakarta.validation.constraints.NotNull;
import library.UseCase;
import library.catalog.domain.BarCode;
import library.catalog.domain.Copy;
import library.catalog.domain.CopyRepository;
import library.catalog.domain.Isbn;

@UseCase
public class RegisterBookCopyUseCase {
    private final CopyRepository copyRepository;

    public RegisterBookCopyUseCase(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    public void execute(@NotNull Isbn isbn, @NotNull BarCode barCode) {
        copyRepository.save(new Copy(isbn, barCode));
    }
}
