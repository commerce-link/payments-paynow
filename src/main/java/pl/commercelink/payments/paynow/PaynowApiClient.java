package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class PaynowApiClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final PaynowSecrets secrets;
    private final String baseUrl;

    PaynowApiClient(PaynowSecrets secrets, String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.secrets = secrets;
        this.baseUrl = baseUrl;
    }

    PaymentResponse createPayment(PaynowPaymentRequest request)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {

        Map<String, String> queryParams = new HashMap<>();

        String serializedBody = OBJECT_MAPPER.writeValueAsString(request);

        String idempotencyKey = UUID.randomUUID().toString();
        String signature = PaynowSignature.create(secrets, idempotencyKey, serializedBody, queryParams);

        String finalUrl = buildUrlWithParams(baseUrl + "/v3/payments", queryParams);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Content-Type", "application/json")
                .header("Api-Key", secrets.getApiKey())
                .header("Idempotency-Key", idempotencyKey)
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(serializedBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to create payment, status code: " + response.statusCode());
        }

        return OBJECT_MAPPER.readValue(response.body(), PaymentResponse.class);
    }

    private String buildUrlWithParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");

        queryParams.entrySet().forEach(entry -> {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        });

        return urlBuilder.substring(0, urlBuilder.length() - 1);
    }
}
