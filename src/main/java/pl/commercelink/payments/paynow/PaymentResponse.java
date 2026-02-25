package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.annotation.JsonProperty;

class PaymentResponse {

    @JsonProperty("redirectUrl")
    private String redirectUrl;
    @JsonProperty("paymentId")
    private String paymentId;
    @JsonProperty("status")
    private PaymentStatus status;

    String getRedirectUrl() {
        return redirectUrl;
    }

    PaymentStatus getStatus() {
        return status;
    }
}
