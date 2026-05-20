package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.commercelink.payments.api.PaymentWebhookResult;
import pl.commercelink.provider.api.WebhookContext;
import pl.commercelink.provider.api.WebhookExecutor;

class PaynowWebhookExecutor implements WebhookExecutor<PaymentWebhookResult> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public PaymentWebhookResult execute(String payload, WebhookContext ctx) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            PaynowWebhookPayload parsed = OBJECT_MAPPER.readValue(payload, PaynowWebhookPayload.class);
            if (parsed.getStatus() != PaymentStatus.CONFIRMED) {
                return new PaymentWebhookResult(null, null, 0, false);
            }

            String signingSecret = ctx.providerConfig().get("signingSecret");
            PaynowSecrets secrets = new PaynowSecrets(null, signingSecret);
            PaynowSignature.verify(secrets, payload, ctx.header("Signature"));

            return new PaymentWebhookResult(parsed.getExternalId(), parsed.getPaymentId(), 0, true);
        } catch (Exception e) {
            throw new RuntimeException("Error processing webhook: " + e.getMessage(), e);
        }
    }
}
