package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.annotation.JsonProperty;

class PaynowPaymentRequest {

    @JsonProperty("amount")
    private int amount;
    @JsonProperty("currency")
    private String currency = "PLN";
    @JsonProperty("externalId")
    private String externalId;
    @JsonProperty("description")
    private String description;
    @JsonProperty("buyer")
    private Buyer buyer;
    @JsonProperty("continueUrl")
    private String continueUrl;
    @JsonProperty("validityTime")
    private int validityTime = 3600;

    PaynowPaymentRequest(int amount, String externalId, String description, Buyer buyer, String continueUrl) {
        this.amount = amount;
        this.externalId = externalId;
        this.description = description;
        this.buyer = buyer;
        this.continueUrl = continueUrl;
    }
}
