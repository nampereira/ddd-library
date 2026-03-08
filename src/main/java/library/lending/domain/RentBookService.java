package library.lending.domain;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class RentBookService {
    private final LoanRepository loanRepository;

    public RentBookService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Loan rent(CopyId copyId, UserId userId) {
        Assert.isTrue(loanRepository.isAvailable(copyId), "copy with id = " + copyId + " is not available");
        return new Loan(copyId, userId);
    }
}
