package library.catalog.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CopyRepository extends CrudRepository<Copy, Long> {
    Optional<Copy> findByCopyId(CopyId copyId);
}
