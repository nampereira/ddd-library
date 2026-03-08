package library.lending;

import library.lending.application.RentBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.CopyId;
import library.lending.domain.LoanId;
import library.lending.domain.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public void rent(@RequestBody RentRequest request, Authentication auth) {
        UUID userUuid = UUID.fromString((String) auth.getPrincipal());
        rentBook.execute(new CopyId(request.copyId()), new UserId(userUuid));
    }

    @PostMapping("/{loanId}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable UUID loanId) {
        returnBook.execute(new LoanId(loanId));
    }

    record RentRequest(UUID copyId) {}
}
