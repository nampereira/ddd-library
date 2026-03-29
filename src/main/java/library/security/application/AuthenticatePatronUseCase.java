package library.security.application;

import library.security.domain.InvalidCredentialsException;
import library.security.domain.PatronRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that authenticates a patron by username and password and returns a JWT token.
 *
 * <p>This class is annotated with {@code @Service} rather than {@code @UseCase} to keep
 * credential data out of the use-case logging advice (which logs method parameters).</p>
 *
 * <p>The message on failure is intentionally generic — do not distinguish between "username
 * not found" and "wrong password" to prevent username enumeration attacks.</p>
 */
@Service
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
