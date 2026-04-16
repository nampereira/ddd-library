package library.security.application;

import library.security.domain.PatronRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class LibraryPatronDetailsService implements UserDetailsService {

    private final PatronRepository patronRepository;

    public LibraryPatronDetailsService(PatronRepository patronRepository) {
        this.patronRepository = patronRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var patron = patronRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Patron not found"));

        var authorities = patron.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                patron.getUsername(),
                patron.getPassword(),
                authorities
        );
    }
}
