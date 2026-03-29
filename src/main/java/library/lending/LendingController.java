package library.lending;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import library.lending.application.LoanBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.Loan;
import library.lending.domain.LoanId;
import library.lending.domain.UserId;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * REST controller for the lending functional area.
 *
 * <p>Exposes two endpoints:</p>
 * <ul>
 *   <li>{@code POST /loans} — borrow a copy (PATRON or ADMIN)</li>
 *   <li>{@code POST /loans/{loanId}/return} — return a borrowed copy (PATRON or ADMIN)</li>
 * </ul>
 *
 * <p>The authenticated patron's identity is extracted from the JWT token via
 * {@link Authentication#getPrincipal()}, which holds the user UUID as a {@code String}.</p>
 *
 * <p>Responses follow the HATEOAS HAL format: the loan creation response includes a
 * {@code return} link so clients can immediately discover how to return the book.</p>
 */
@RestController
@RequestMapping("/loans")
public class LendingController {

    private final LoanBookUseCase loanBook;
    private final ReturnBookUseCase returnBook;

    public LendingController(LoanBookUseCase loanBook, ReturnBookUseCase returnBook) {
        this.loanBook = loanBook;
        this.returnBook = returnBook;
    }

    /**
     * Borrows a copy on behalf of the authenticated patron.
     *
     * @param request the request body containing the {@code barCode} of the copy to borrow
     * @param auth    the JWT-backed authentication object; principal holds the patron's UUID
     * @return {@code 201 Created} with the loan ID and a hypermedia {@code return} link
     */
    @PostMapping
    public ResponseEntity<EntityModel<LoanResponse>> loan(@Valid @RequestBody LoanRequest request, Authentication auth) {
        UUID userUuid = UUID.fromString((String) auth.getPrincipal());
        Loan loan = loanBook.execute(request.barCode(), new UserId(userUuid));
        UUID loanId = loan.getLoanId().id();

        Link returnLink = linkTo(LendingController.class).slash(loanId).slash("return").withRel("return");
        EntityModel<LoanResponse> body = EntityModel.of(new LoanResponse(loanId), returnLink);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Returns a borrowed copy, closing the associated loan.
     *
     * @param loanId the UUID of the loan to close
     */
    @PostMapping("/{loanId}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable UUID loanId) {
        returnBook.execute(new LoanId(loanId));
    }

    /** Request body for the {@code POST /loans} endpoint. */
    record LoanRequest(@NotBlank String barCode) {}

    /** Response body for the {@code POST /loans} endpoint. */
    record LoanResponse(UUID loanId) {}
}
