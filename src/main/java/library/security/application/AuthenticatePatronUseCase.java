package library.security.application;

import library.UseCase;
import library.SuppressArgLogging;
import library.security.domain.InvalidCredentialsException;
import library.security.domain.PatronRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application use case that authenticates a patron by username and password and returns a JWT token.
 *
 * <p>Annotated with {@link SuppressArgLogging} so that {@link library.UseCaseLoggingAdvice}
 * logs {@code [SUPPRESSED]} in place of the raw credentials instead of printing them to the
 * log. {@link UseCase} still applies, giving this class consistent transaction management,
 * Bean Validation, and timing logs.</p>
 *
 * <p>The message on failure is intentionally generic — do not distinguish between "username
 * not found" and "wrong password" to prevent username enumeration attacks.</p>
 */
@UseCase
@SuppressArgLogging
@Transactional(readOnly = true)
public class AuthenticatePatronUseCase {

    private final PatronRepository patronRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticatePatronUseCase(PatronRepository patronRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService) {
        this.patronRepository = patronRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Verifies the credentials and returns a signed JWT token on success.
     *
     * @param request the login request containing username and password
     * @return a {@link TokenResponse} containing the signed JWT
     * @throws InvalidCredentialsException if the username is not found or the password does not match
     */
    public TokenResponse execute(LoginRequest request) {
        var patron = patronRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), patron.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return new TokenResponse(jwtService.generateToken(patron));
    }
}
