package pl.commercelink.payments.paynow;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.commercelink.payments.api.PaymentProvider;
import pl.commercelink.payments.api.PaymentProviderDescriptor;
import pl.commercelink.payments.api.PaymentWebhookResult;
import pl.commercelink.provider.api.EventBinding;
import pl.commercelink.provider.api.EventBinding.WebhookBinding;
import pl.commercelink.provider.api.ProviderField;
import pl.commercelink.provider.api.WebhookContext;

import java.util.List;
import java.util.Map;

import static pl.commercelink.provider.api.ProviderField.FieldType.PASSWORD;
import static pl.commercelink.provider.api.ProviderField.FieldType.URL;

public class PaynowPaymentProviderDescriptor implements PaymentProviderDescriptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String name() {
        return "paynow";
    }

    @Override
    public String displayName() {
        return "PayNow";
    }

    @Override
    public List<ProviderField> configurationFields() {
        return List.of(
                new ProviderField("apiUrl", "API URL", URL, true, "https://api.paynow.pl"),
                new ProviderField("apiKey", "API Key", PASSWORD, true, ""),
                new ProviderField("signingSecret", "Signing Secret", PASSWORD, true, "")
        );
    }

    @Override
    public PaymentProvider create(Map<String, String> configuration) {
        return new PaynowPaymentProvider(configuration);
    }

    @Override
    public List<EventBinding<?>> bindings() {
        return List.of(new WebhookBinding<>("paynow", String.class, this::handleWebhook));
    }

    private PaymentWebhookResult handleWebhook(String payload, WebhookContext ctx) {
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
