package library.security.domain;

/**
 * Thrown when a login attempt fails due to an unrecognised username or incorrect password.
 *
 * <p>The message is intentionally generic — callers must not reveal which of the two
 * fields was wrong, to avoid username enumeration attacks.</p>
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
