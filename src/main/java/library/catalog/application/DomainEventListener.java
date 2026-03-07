package library.catalog.application;

import library.catalog.domain.Copy;
import library.catalog.domain.CopyId;
import library.catalog.domain.CopyRepository;
import library.lending.domain.LoanClosed;
import library.lending.domain.LoanCreated;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DomainEventListener {

    private final CopyRepository copyRepository;

    public DomainEventListener(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @TransactionalEventListener
    public void handle(LoanCreated event) {
        Copy copy = copyRepository.findByCopyId(new CopyId(event.copyId().id())).orElseThrow();
        copy.makeUnavailable();
        copyRepository.save(copy);
    }

    @TransactionalEventListener
    public void handle(LoanClosed event) {
        Copy copy = copyRepository.findByCopyId(new CopyId(event.copyId().id())).orElseThrow();
        copy.makeAvailable();
        copyRepository.save(copy);
    }
}
