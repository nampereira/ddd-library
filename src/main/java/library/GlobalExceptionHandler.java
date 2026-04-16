package library;

import library.catalog.domain.AuthorNotFoundException;
import library.catalog.domain.BookCopyNotFoundException;
import library.catalog.domain.BookInformationNotFoundException;
import library.catalog.domain.BookNotFoundException;
import library.catalog.domain.DuplicateBarCodeException;
import library.lending.domain.CopyNotAvailableException;
import library.lending.domain.LoanAlreadyReturnedException;
import library.lending.domain.LoanNotFoundException;
import library.lending.domain.ReferenceOnlyException;
import library.security.domain.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralised exception handler that translates domain and infrastructure exceptions into
 * meaningful HTTP responses.
 *
 * <p>Annotated with {@link RestControllerAdvice}, which applies to every
 * {@code @RestController} in the application.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 401 — authentication failed. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 — book not found in catalog. */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 — external book search returned no information for the given ISBN. */
    @ExceptionHandler(BookInformationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookInfoNotFound(BookInformationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 — author not found by contact email. */
    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthorNotFound(AuthorNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 — physical copy not found by barcode. */
    @ExceptionHandler(BookCopyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCopyNotFound(BookCopyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 — loan not found by ID. */
    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLoanNotFound(LoanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 409 — loan has already been returned. */
    @ExceptionHandler(LoanAlreadyReturnedException.class)
    public ResponseEntity<ErrorResponse> handleLoanAlreadyReturned(LoanAlreadyReturnedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 409 — copy is already on loan (domain check caught this before reaching the DB). */
    @ExceptionHandler(CopyNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleCopyNotAvailable(CopyNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 409 — attempt to borrow a reference-only copy (REF- prefix). */
    @ExceptionHandler(ReferenceOnlyException.class)
    public ResponseEntity<ErrorResponse> handleReferenceOnly(ReferenceOnlyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 409 — duplicate barcode on copy registration. */
    @ExceptionHandler(DuplicateBarCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBarCode(DuplicateBarCodeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * 409 — last-resort handler for database unique constraint violations.
     *
     * <p>This fires only when two concurrent requests both pass the {@code isAvailable} check
     * simultaneously and race to insert a loan. The {@code UNIQUE} constraint on
     * {@code active_bar_code} causes the second insert to fail here. The domain-level check
     * in {@link library.lending.domain.LoanBookService} prevents this in the non-concurrent case.
     * See {@link library.lending.domain.Loan} for details on the concurrency safety design.</p>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("A concurrent modification conflict occurred. Please retry."));
    }

    /**
     * 409 — optimistic locking failure on a concurrent update to the same aggregate.
     *
     * <p>Fires when two requests race to update (e.g. return) the same {@link library.lending.domain.Loan}
     * simultaneously. The {@code @Version} field on {@code Loan} detects the conflict and throws
     * this exception. The client should retry the request.</p>
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("The resource was modified concurrently. Please retry."));
    }

    /**
     * 400 — request body failed Bean Validation.
     *
     * <p>Collects all field-level constraint violations and returns them as a single,
     * comma-separated message so the client knows exactly which fields were invalid.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /** Error response body. */
    record ErrorResponse(String message) {}
}
