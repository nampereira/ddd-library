package library.catalog.domain;

import library.common.BarCode;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for {@link BookCopy} entities.
 *
 * <p>Follows the DDD pattern of defining the repository interface in the domain layer so that
 * the domain model has no dependency on infrastructure. Spring Data JPA provides the
 * implementation at runtime.</p>
 */
public interface BookCopyRepository extends CrudRepository<BookCopy, Long> {

    /**
     * Finds a copy by its domain identity (barcode).
     *
     * @param barCode the barcode of the copy
     * @return the copy wrapped in an {@link Optional}, or {@link Optional#empty()} if not found
     */
    Optional<BookCopy> findByBarCode(BarCode barCode);
}
