package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.annotation.JsonProperty;

class PaynowWebhookPayload {

    @JsonProperty("paymentId")
    private String paymentId;
    @JsonProperty("externalId")
    private String externalId;
    @JsonProperty("status")
    private PaymentStatus status;
    @JsonProperty("modifiedAt")
    private String modifiedAt;

    String getPaymentId() {
        return paymentId;
    }

    String getExternalId() {
        return externalId;
    }

    PaymentStatus getStatus() {
        return status;
    }
}
