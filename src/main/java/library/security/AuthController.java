package library.security;

import jakarta.validation.Valid;
import library.security.application.AuthenticatePatronUseCase;
import library.security.application.LoginRequest;
import library.security.application.TokenResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticatePatronUseCase authenticatePatron;

    public AuthController(AuthenticatePatronUseCase authenticatePatron) {
        this.authenticatePatron = authenticatePatron;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticatePatron.execute(request);
    }
}
