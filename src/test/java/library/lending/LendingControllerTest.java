package library.lending;

import library.catalog.domain.BookCopyNotFoundException;
import library.common.BarCode;
import library.lending.application.LoanBookUseCase;
import library.lending.application.ReturnBookUseCase;
import library.lending.domain.CopyNotAvailableException;
import library.lending.domain.Loan;
import library.lending.domain.LoanAlreadyReturnedException;
import library.lending.domain.LoanId;
import library.lending.domain.LoanNotFoundException;
import library.lending.domain.UserId;
import library.security.SecurityConfig;
import library.security.application.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LendingController.class)
@Import(SecurityConfig.class)
class LendingControllerTest {

    @Autowired MockMvc mvc;

    @MockBean LoanBookUseCase loanBook;
    @MockBean ReturnBookUseCase returnBook;
    @MockBean JwtService jwtService;

    /** Auth with a UUID string as principal, as the real JWT filter would produce. */
    private static RequestPostProcessor patronAuth() {
        return authentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_PATRON"))));
    }

    // ─── POST /loans ──────────────────────────────────────────────────────────

    @Test
    void loanReturns201WhenSuccessful() throws Exception {
        BarCode bc = new BarCode("BC-001");
        Loan loan = new Loan(bc, new UserId(UUID.randomUUID()));
        when(loanBook.execute(any(), any())).thenReturn(loan);

        mvc.perform(post("/loans").with(patronAuth()).with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"barCode\":\"BC-001\"}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.loanId").isNotEmpty());
    }

    @Test
    void loanReturns404WhenCopyNotFound() throws Exception {
        when(loanBook.execute(any(), any()))
                .thenThrow(new BookCopyNotFoundException(new BarCode("BC-001")));

        mvc.perform(post("/loans").with(patronAuth()).with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"barCode\":\"BC-001\"}"))
           .andExpect(status().isNotFound());
    }

    @Test
    void loanReturns409WhenCopyAlreadyOnLoan() throws Exception {
        when(loanBook.execute(any(), any()))
                .thenThrow(new CopyNotAvailableException(new BarCode("BC-001")));

        mvc.perform(post("/loans").with(patronAuth()).with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"barCode\":\"BC-001\"}"))
           .andExpect(status().isConflict());
    }

    @Test
    void loanReturns400WhenBarcodeIsMissing() throws Exception {
        mvc.perform(post("/loans").with(patronAuth()).with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"barCode\":\"\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void loanReturns403WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/loans").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"barCode\":\"BC-001\"}"))
           .andExpect(status().isForbidden());
    }

    // ─── POST /loans/{loanId}/return ─────────────────────────────────────────

    @Test
    void returnBookReturns204WhenSuccessful() throws Exception {
        UUID loanId = UUID.randomUUID();

        mvc.perform(post("/loans/" + loanId + "/return").with(patronAuth()).with(csrf()))
           .andExpect(status().isNoContent());
    }

    @Test
    void returnBookReturns404WhenLoanNotFound() throws Exception {
        UUID loanId = UUID.randomUUID();
        doThrow(new LoanNotFoundException(new LoanId(loanId)))
                .when(returnBook).execute(any());

        mvc.perform(post("/loans/" + loanId + "/return").with(patronAuth()).with(csrf()))
           .andExpect(status().isNotFound());
    }

    @Test
    void returnBookReturns409WhenLoanAlreadyReturned() throws Exception {
        UUID loanId = UUID.randomUUID();
        doThrow(new LoanAlreadyReturnedException(new LoanId(loanId)))
                .when(returnBook).execute(any());

        mvc.perform(post("/loans/" + loanId + "/return").with(patronAuth()).with(csrf()))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.message").value("Loan '" + loanId + "' has already been returned"));
    }

    @Test
    void returnBookReturns403WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/loans/" + UUID.randomUUID() + "/return").with(csrf()))
           .andExpect(status().isForbidden());
    }
}
