# PayNow Payments

[PayNow](https://www.paynow.pl) implementation of the [payments-api](https://github.com/commerce-link/payments-api) provider interface.

Supports creating PayNow payment links and processing webhooks with HMAC-SHA256 signature verification.

## Provider Discovery

This library registers itself for `ServiceLoader` discovery. Add it to your classpath and the provider will be available automatically via `PaymentProviderDescriptor` SPI. See the [provider-api README](https://github.com/commerce-link/provider-api) for details.

## Configuration Fields

| Key             | Label          | Type     | Required | Default                  |
|-----------------|----------------|----------|----------|--------------------------|
| `apiUrl`        | API URL        | URL      | yes      | `https://api.paynow.pl`  |
| `apiKey`        | API Key        | PASSWORD | yes      |                          |
| `signingSecret` | Signing Secret | PASSWORD | yes      |                          |
