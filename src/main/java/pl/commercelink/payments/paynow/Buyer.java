package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.annotation.JsonProperty;

class Buyer {

    @JsonProperty("email")
    private String email;

    Buyer(String email) {
        this.email = email;
    }
}
