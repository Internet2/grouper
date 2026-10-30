---
title: "Grouper WS bearer token external system"
space: Grouper
---

The **Web service** external system stores a base URL and authentication for an HTTP API. Configure it under **Miscellaneous > External systems**. Its properties are in `grouper-loader.properties`, with the prefix `grouper.wsBearerToken.<configId>.`.

It supports a static bearer token (`httpAuthnType = bearerToken`, the default), basic authentication (`basicAuth`), or OAuth client credentials (`oauthClientCredentials`).

## Additional HTTP headers

Some APIs require more than authentication, such as an API version, tenant ID, or `Accept` header. Configure up to five additional headers:

| Property suffix | Default | Meaning |
| --- | --- | --- |
| `numberOfAdditionalHeaders` | `0` | Number of additional headers, from 0 to 5. Blank or zero keeps the existing behavior. |
| `additionalHeader.<n>.name` | | Required header name, where `<n>` is 0 through 4. |
| `additionalHeader.<n>.value` | | Required header value. |
| `additionalHeader.<n>.valueIsSecret` | `false` | Encrypt the value when saved through the external system configuration UI, mask it in that UI, and suppress the header value in HTTP logs. |

Only the indexes below `numberOfAdditionalHeaders` are used. The UI shows each header's name, secret flag, and value when that index is enabled.

Header names must be valid HTTP tokens: letters, digits, and token punctuation such as hyphens; not spaces, colons, or newlines. Additional headers cannot replace `Authorization`, the configured `httpHeader` (default `Authorization`), or `apiKeyHeaderName`, ignoring case. Values cannot contain newlines.

These headers apply to requests made with this external system, including the **Test** button and retries. They are defaults: if a caller explicitly sets the same header, its value takes precedence, even if it uses different capitalization. The header is sent only once.

For a secret header, set **Value is secret** to true before entering the value and save through the external system UI. Do not put plaintext secrets into a checked-in properties file. The flag does not encrypt an existing file on disk.

## Anthropic example

Anthropic requires both an API key and an `anthropic-version` header. Configure this external system in `grouper-loader.properties` (enter the key through the UI):

```properties
grouper.wsBearerToken.anthropic.endpoint = https://api.anthropic.com/v1
grouper.wsBearerToken.anthropic.httpHeader = x-api-key
grouper.wsBearerToken.anthropic.prependBearerTokenPrefix = false
grouper.wsBearerToken.anthropic.accessTokenPassword = <API key>
grouper.wsBearerToken.anthropic.testUrlSuffix = models
grouper.wsBearerToken.anthropic.numberOfAdditionalHeaders = 1
grouper.wsBearerToken.anthropic.additionalHeader.0.name = anthropic-version
grouper.wsBearerToken.anthropic.additionalHeader.0.value = 2023-06-01
```

The **Test** button makes `GET https://api.anthropic.com/v1/models` with `x-api-key` and `anthropic-version`. A valid key with access to that endpoint should return HTTP 200. There is no need to ignore a failed test caused by a missing version header.

For the UI AI assistant, set `grouper.ai.agent.externalSystemConfigId = anthropic` in `grouper.properties` along with the other assistant settings. The assistant's own API version header still takes precedence over the external system default.
