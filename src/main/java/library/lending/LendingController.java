package library.lending;

import library.lending.application.RentBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.CopyId;
import library.lending.domain.LoanId;
import library.lending.domain.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LendingController {

    private final RentBookUseCase rentBook;
    private final ReturnBookUseCase returnBook;

    public LendingController(RentBookUseCase rentBook, ReturnBookUseCase returnBook) {
        this.rentBook = rentBook;
        this.returnBook = returnBook;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void rent(@RequestBody RentRequest request) {
        rentBook.execute(new CopyId(request.copyId()), new UserId(request.userId()));
    }

    @PostMapping("/{loanId}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable UUID loanId) {
        returnBook.execute(new LoanId(loanId));
    }

    record RentRequest(UUID copyId, UUID userId) {}
}
