package library.security;

import jakarta.validation.Valid;
import library.security.application.AuthenticateUserUseCase;
import library.security.application.LoginRequest;
import library.security.application.TokenResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;

    public AuthController(AuthenticateUserUseCase authenticateUser) {
        this.authenticateUser = authenticateUser;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticateUser.execute(request);
    }
}
