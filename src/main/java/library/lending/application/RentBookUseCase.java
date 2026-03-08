package library.lending.application;

import library.UseCase;
import library.lending.domain.CopyAvailabilityService;
import library.lending.domain.CopyId;
import library.lending.domain.Loan;
import library.lending.domain.LoanRepository;
import library.lending.domain.UserId;

@UseCase
public class RentBookUseCase {
    private final LoanRepository loanRepository;
    private final CopyAvailabilityService availabilityService;

    public RentBookUseCase(LoanRepository loanRepository, CopyAvailabilityService availabilityService) {
        this.loanRepository = loanRepository;
        this.availabilityService = availabilityService;
    }

    public void execute(CopyId copyId, UserId userId) {
        loanRepository.save(new Loan(copyId, userId, availabilityService));
    }
}
