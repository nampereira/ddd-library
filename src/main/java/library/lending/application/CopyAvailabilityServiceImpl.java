package library.lending.application;

import library.lending.domain.CopyAvailabilityService;
import library.lending.domain.CopyId;
import library.lending.domain.LoanRepository;
import org.springframework.stereotype.Service;

@Service
class CopyAvailabilityServiceImpl implements CopyAvailabilityService {
    private final LoanRepository loanRepository;

    CopyAvailabilityServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean isAvailable(CopyId copyId) {
        return loanRepository.isAvailable(copyId);
    }
}
