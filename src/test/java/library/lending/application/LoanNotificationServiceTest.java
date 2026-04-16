package library.lending.application;

import library.common.BarCode;
import library.lending.domain.LoanClosed;
import library.lending.domain.LoanCreated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanNotificationServiceTest {

    @Mock RestClient.Builder builder;
    @Mock RestClient restClient;
    @Mock RestClient.RequestBodyUriSpec uriSpec;
    @Mock RestClient.RequestBodySpec bodySpec;
    @Mock RestClient.ResponseSpec responseSpec;

    private static final BarCode BAR_CODE = new BarCode("BC-001");
    private static final String WEBHOOK_URL = "https://example.com/hook";

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(restClient);
    }

    // ─── Blank URL ────────────────────────────────────────────────────────────

    @Test
    void skipsWebhookOnLoanCreatedWhenUrlIsBlank() {
        LoanNotificationService service = new LoanNotificationService(builder, "");
        service.onLoanCreated(new LoanCreated(BAR_CODE));
        verify(restClient, never()).post();
    }

    @Test
    void skipsWebhookOnLoanClosedWhenUrlIsBlank() {
        LoanNotificationService service = new LoanNotificationService(builder, "");
        service.onLoanClosed(new LoanClosed(BAR_CODE));
        verify(restClient, never()).post();
    }

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Test
    void postsToWebhookOnLoanCreated() {
        LoanNotificationService service = new LoanNotificationService(builder, WEBHOOK_URL);
        stubRestClient();

        service.onLoanCreated(new LoanCreated(BAR_CODE));

        verify(restClient).post();
        verify(uriSpec).uri(WEBHOOK_URL);
        verify(bodySpec).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void postsToWebhookOnLoanClosed() {
        LoanNotificationService service = new LoanNotificationService(builder, WEBHOOK_URL);
        stubRestClient();

        service.onLoanClosed(new LoanClosed(BAR_CODE));

        verify(restClient).post();
        verify(uriSpec).uri(WEBHOOK_URL);
        verify(bodySpec).contentType(MediaType.APPLICATION_JSON);
    }

    // ─── Failure resilience ───────────────────────────────────────────────────

    @Test
    void swallowsWebhookFailureAndDoesNotThrow() {
        LoanNotificationService service = new LoanNotificationService(builder, WEBHOOK_URL);
        when(restClient.post()).thenThrow(new RuntimeException("connection refused"));

        // must not propagate — the loan transaction is already committed at this point
        service.onLoanCreated(new LoanCreated(BAR_CODE));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void stubRestClient() {
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any(MediaType.class))).thenReturn(bodySpec);
        when(bodySpec.body(any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);
    }
}
