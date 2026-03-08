package library.lending.application;

import library.UseCase;
import library.lending.domain.CopyId;
import library.lending.domain.Loan;
import library.lending.domain.LoanRepository;
import library.lending.domain.RentBookService;
import library.lending.domain.UserId;

@UseCase
public class RentBookUseCase {
    private final LoanRepository loanRepository;
    private final RentBookService rentBookService;

    public RentBookUseCase(LoanRepository loanRepository, RentBookService rentBookService) {
        this.loanRepository = loanRepository;
        this.rentBookService = rentBookService;
    }

    public Loan execute(CopyId copyId, UserId userId) {
        return loanRepository.save(rentBookService.rent(copyId, userId));
    }
}
