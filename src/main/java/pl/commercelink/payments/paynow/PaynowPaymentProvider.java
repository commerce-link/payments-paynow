package pl.commercelink.payments.paynow;

import pl.commercelink.payments.api.PaymentLink;
import pl.commercelink.payments.api.PaymentProvider;
import pl.commercelink.payments.api.PaymentRequest;

import java.util.Map;

class PaynowPaymentProvider implements PaymentProvider {

    private final PaynowApiClient apiClient;

    PaynowPaymentProvider(Map<String, String> configuration) {
        PaynowSecrets secrets = new PaynowSecrets(configuration.get("apiKey"), configuration.get("signingSecret"));
        this.apiClient = new PaynowApiClient(secrets, configuration.get("apiUrl"));
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
