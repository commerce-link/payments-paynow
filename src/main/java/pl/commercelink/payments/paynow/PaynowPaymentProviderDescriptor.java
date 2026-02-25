package pl.commercelink.payments.paynow;

import pl.commercelink.payments.api.PaymentProvider;
import pl.commercelink.payments.api.PaymentProviderDescriptor;
import pl.commercelink.provider.api.ProviderField;

import java.util.List;
import java.util.Map;

import static pl.commercelink.provider.api.ProviderField.FieldType.PASSWORD;
import static pl.commercelink.provider.api.ProviderField.FieldType.URL;

public class PaynowPaymentProviderDescriptor implements PaymentProviderDescriptor {

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
}
