package library.lending;

import library.lending.application.RentBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.CopyId;
import library.lending.domain.Loan;
import library.lending.domain.LoanId;
import library.lending.domain.UserId;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
    public ResponseEntity<EntityModel<LoanResponse>> rent(@RequestBody RentRequest request, Authentication auth) {
        UUID userUuid = UUID.fromString((String) auth.getPrincipal());
        Loan loan = rentBook.execute(new CopyId(request.copyId()), new UserId(userUuid));
        UUID loanId = loan.getLoanId().id();

        Link returnLink = linkTo(LendingController.class).slash(loanId).slash("return").withRel("return");
        EntityModel<LoanResponse> body = EntityModel.of(new LoanResponse(loanId), returnLink);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/{loanId}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable UUID loanId) {
        returnBook.execute(new LoanId(loanId));
    }

    record RentRequest(UUID copyId) {}

    record LoanResponse(UUID loanId) {}
}
