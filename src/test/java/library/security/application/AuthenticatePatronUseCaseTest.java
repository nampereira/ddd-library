package library.security.application;

import library.security.domain.InvalidCredentialsException;
import library.security.domain.Patron;
import library.security.domain.PatronRepository;
import library.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatePatronUseCaseTest {

    @Mock PatronRepository patronRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    AuthenticatePatronUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticatePatronUseCase(patronRepository, passwordEncoder, jwtService);
    }

    @Test
    void returnsTokenWhenCredentialsAreValid() {
        Patron patron = new Patron("alice", "encoded-pw", Set.of(Role.PATRON));
        when(patronRepository.findByUsername("alice")).thenReturn(Optional.of(patron));
        when(passwordEncoder.matches("secret", "encoded-pw")).thenReturn(true);
        when(jwtService.generateToken(patron)).thenReturn("signed-jwt");

        TokenResponse response = useCase.execute(new LoginRequest("alice", "secret"));

        assertThat(response.token()).isEqualTo("signed-jwt");
    }

    @Test
    void throwsInvalidCredentialsWhenUsernameUnknown() {
        when(patronRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> useCase.execute(new LoginRequest("ghost", "any")));
    }

    @Test
    void throwsInvalidCredentialsWhenPasswordIsWrong() {
        Patron patron = new Patron("alice", "encoded-pw", Set.of(Role.PATRON));
        when(patronRepository.findByUsername("alice")).thenReturn(Optional.of(patron));
        when(passwordEncoder.matches("wrong", "encoded-pw")).thenReturn(false);

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> useCase.execute(new LoginRequest("alice", "wrong")));
    }

    @Test
    void usernameAndPasswordFailureProduceSameMessageToPreventEnumeration() {
        // unknown username
        when(patronRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // wrong password
        Patron patron = new Patron("alice", "encoded-pw", Set.of(Role.PATRON));
        when(patronRepository.findByUsername("alice")).thenReturn(Optional.of(patron));
        when(passwordEncoder.matches("wrong", "encoded-pw")).thenReturn(false);

        InvalidCredentialsException unknownUser = catchThrowableOfType(
                () -> useCase.execute(new LoginRequest("unknown", "pw")),
                InvalidCredentialsException.class);
        InvalidCredentialsException wrongPassword = catchThrowableOfType(
                () -> useCase.execute(new LoginRequest("alice", "wrong")),
                InvalidCredentialsException.class);

        assertThat(unknownUser.getMessage()).isEqualTo(wrongPassword.getMessage());
    }
}
