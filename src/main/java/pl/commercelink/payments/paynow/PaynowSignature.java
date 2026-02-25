package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

class PaynowSignature {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PaynowSignature() {
    }

    static String create(PaynowSecrets secrets, String idempotencyKey, String serializedBody,
                  Map<String, String> queryParams)
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {

        Map<String, Object> payload = createPayload(secrets.getApiKey(), idempotencyKey, serializedBody, queryParams);

        String payloadJson = OBJECT_MAPPER.writeValueAsString(payload);

        return calculateHMAC(payloadJson, secrets.getSigningSecret());
    }

    static void verify(PaynowSecrets secrets, String responseBody, String signature)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String calculatedSignature = calculateHMAC(responseBody, secrets.getSigningSecret());
        if (!calculatedSignature.equals(signature)) {
            throw new RuntimeException("Invalid signature");
        }
    }

    private static Map<String, Object> createPayload(
            String apiKey, String idempotencyKey, String serializedBody, Map<String, String> queryParams) {

        Map<String, String> headers = new TreeMap<>();
        headers.put("Api-Key", apiKey);
        headers.put("Idempotency-Key", idempotencyKey);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("headers", headers);

        Map<String, String> parameters = queryParams != null ? new TreeMap<>(queryParams) : new TreeMap<>();
        payload.put("parameters", parameters);

        payload.put("body", serializedBody != null ? serializedBody : "");

        return payload;
    }

    private static String calculateHMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {

        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);

        byte[] hashedBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
}
