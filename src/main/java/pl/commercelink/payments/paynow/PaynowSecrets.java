package pl.commercelink.payments.paynow;

class PaynowSecrets {

    private final String apiKey;
    private final String signingSecret;

    PaynowSecrets(String apiKey, String signingSecret) {
        this.apiKey = apiKey;
        this.signingSecret = signingSecret;
    }

    String getApiKey() {
        return apiKey;
    }

    String getSigningSecret() {
        return signingSecret;
    }
}
