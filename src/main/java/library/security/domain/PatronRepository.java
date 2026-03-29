package library.security.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PatronRepository extends CrudRepository<Patron, Long> {
    Optional<Patron> findByUsername(String username);
}
