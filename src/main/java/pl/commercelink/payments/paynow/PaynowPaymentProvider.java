package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.commercelink.payments.api.PaymentLink;
import pl.commercelink.payments.api.PaymentProvider;
import pl.commercelink.payments.api.PaymentRequest;
import pl.commercelink.payments.api.PaymentWebhookRequest;
import pl.commercelink.payments.api.PaymentWebhookResult;

import java.util.Map;

class PaynowPaymentProvider implements PaymentProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PaynowApiClient apiClient;
    private final PaynowSecrets secrets;

    PaynowPaymentProvider(Map<String, String> configuration) {
        this.secrets = new PaynowSecrets(configuration.get("apiKey"), configuration.get("signingSecret"));
        this.apiClient = new PaynowApiClient(secrets, configuration.get("apiUrl"));
    }

    @Override
    public PaymentWebhookResult processWebhook(PaymentWebhookRequest request) {
        try {
            PaynowWebhookPayload payload = OBJECT_MAPPER.readValue(request.payload(), PaynowWebhookPayload.class);
            if (payload.getStatus() != PaymentStatus.CONFIRMED) {
                return new PaymentWebhookResult(null, null, 0, false);
            }

            String signature = request.getHeader("Signature");
            PaynowSignature.verify(secrets, request.payload(), signature);

            return new PaymentWebhookResult(payload.getExternalId(), payload.getPaymentId(), 0, true);
        } catch (Exception e) {
            throw new RuntimeException("Error processing webhook: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentLink createPaymentLink(PaymentRequest request) {
        int amount = request.lineItems().stream().mapToInt(i -> i.amount() * i.quantity()).sum();
        if (request.shippingItem() != null) {
            amount += request.shippingItem().amount();
        }

        String description = "Zamówienie #" + request.orderId() + " | " + request.storeName();

        String successUrl = request.successUrl();
        if (successUrl == null || successUrl.isEmpty()) {
            throw new IllegalArgumentException("Success URL is not configured");
        }

        if (successUrl.endsWith("/")) {
            successUrl = successUrl + request.orderId();
        } else {
            successUrl = successUrl + "/" + request.orderId();
        }

        try {
            PaymentResponse response = apiClient.createPayment(
                    new PaynowPaymentRequest(amount, request.orderId(), description, new Buyer(request.buyerEmail()), successUrl));

            if (response.getStatus() != PaymentStatus.NEW) {
                throw new RuntimeException("Unexpected payment status: " + response.getStatus());
            }

            return new PaymentLink(response.getRedirectUrl(), "GET", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
