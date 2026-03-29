package library.lending.application;

import library.lending.domain.LoanClosed;
import library.lending.domain.LoanCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/**
 * Demonstration notification service that forwards loan domain events to a configurable
 * HTTP webhook endpoint.
 *
 * <p>Shows how additional behaviour — notifications, audit trails, analytics — can be added
 * by listening to {@link LoanCreated} and {@link LoanClosed} without touching any lending
 * domain or use-case code.</p>
 *
 * <p>Configure the target endpoint with {@code notifications.webhook-url}. Leave it blank to
 * disable silently. A free ephemeral URL from
 * <a href="https://pipedream.com/requestbin">Pipedream RequestBin</a> works well for demos.</p>
 *
 * <p>Failures are logged as warnings and do not affect the transaction — the loan is already
 * committed by the time this listener runs ({@code AFTER_COMMIT} phase).</p>
 */
@Service
class LoanNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoanNotificationService.class);

    private final RestClient restClient;
    private final String webhookUrl;

    LoanNotificationService(RestClient.Builder builder,
            @Value("${notifications.webhook-url:}") String webhookUrl) {
        this.restClient = builder.build();
        this.webhookUrl = webhookUrl;
    }

    @TransactionalEventListener
    public void onLoanCreated(LoanCreated event) {
        post("LoanCreated", event.barCode().code());
    }

    @TransactionalEventListener
    public void onLoanClosed(LoanClosed event) {
        post("LoanClosed", event.barCode().code());
    }

    private void post(String eventType, String barCode) {
        if (webhookUrl.isBlank()) {
            log.debug("notifications.webhook-url not set — skipping notification for {}", eventType);
            return;
        }
        try {
            restClient.post().uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "event", eventType,
                            "barCode", barCode,
                            "timestamp", Instant.now().toString()
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Webhook notification sent: {} for copy [{}]", eventType, barCode);
        } catch (Exception ex) {
            log.warn("Webhook notification failed for {}: {}", eventType, ex.getMessage());
        }
    }
}
