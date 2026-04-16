package library.security.domain;

import library.DomainException;

/**
 * Thrown when a login attempt fails due to an unrecognised username or incorrect password.
 *
 * <p>The message is intentionally generic — callers must not reveal which of the two
 * fields was wrong, to avoid username enumeration attacks.</p>
 *
 * <p>Extends {@link DomainException} because an authentication failure is a domain rule
 * violation in the security bounded context, and is handled by
 * {@link library.GlobalExceptionHandler} like all other domain exceptions.</p>
 */
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
