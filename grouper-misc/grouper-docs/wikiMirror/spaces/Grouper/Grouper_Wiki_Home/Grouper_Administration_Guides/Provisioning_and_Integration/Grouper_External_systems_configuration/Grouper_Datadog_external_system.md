---
title: "Grouper Datadog external system"
space: Grouper
pageId: 28548830
version: 6
lastUpdated: 2026-07-12T15:26:58.880Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548830/Grouper+Datadog+external+system
---

## Datadog external system

See also: [Grouper Datadog provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555435/Grouper+Datadog+provisioner) | [Datadog provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555435/Grouper+Datadog+provisioner)

Datadog uses two API keys for authentication: an API key and an Application key. Since the Grouper WsBearerToken external system only has one password field, both keys are stored together as a JSON object in the accessTokenPassword field.

## Configuration

Use a WsBearerToken external system with the following settings:

| Setting | Value |
| --- | --- |
| httpAuthnType | bearerToken |
| endpoint | Your Datadog site base URL (see table below) |
| accessTokenPassword | JSON object with apiKey and applicationKey (see below) |
| prependBearerTokenPrefix | Does not matter (the provisioner reads the password directly) |

## accessTokenPassword format

The accessTokenPassword must be a JSON object containing both keys. This value will be encrypted by Grouper's morphString encryption.

{"apiKey": "abc123def456", "applicationKey": "xyz789ghi012"}The provisioner reads this field, parses the JSON, and attaches two headers to every request:

DD-API-KEY: abc123def456 DD-APPLICATION-KEY: xyz789ghi012

## Endpoint (base URL)

The endpoint depends on your Datadog site:

| Site | Base URL |
| --- | --- |
| US1 (default) | [https://api.datadoghq.com](https://api.datadoghq.com) |
| US3 | [https://api.us3.datadoghq.com](https://api.us3.datadoghq.com) |
| US5 | [https://api.us5.datadoghq.com](https://api.us5.datadoghq.com) |
| EU | [https://api.datadoghq.eu](https://api.datadoghq.eu) |
| AP1 | [https://api.ap1.datadoghq.com](https://api.ap1.datadoghq.com) |
| US1-FED | [https://api.ddog-gov.com](https://api.ddog-gov.com) |

## Example configuration properties

# External system config id: datadogProd grouper.wsBearerToken.datadogProd.endpoint = https://api.us5.datadoghq.com grouper.wsBearerToken.datadogProd.accessTokenPassword = {"apiKey": "abc123def456", "applicationKey": "xyz789ghi012"}Note: in practice the accessTokenPassword value would be encrypted. The above shows the plaintext for illustration.

## Connection test

The Grouper UI "test connection" button for WsBearerToken external systems will not work with Datadog because Datadog requires custom authentication headers (DD-API-KEY and DD-APPLICATION-KEY) rather than a standard Bearer token. The provisioner attaches these headers itself when making API calls. To verify connectivity, run the main method in DatadogApiCommands against a real Datadog endpoint.

## Getting your keys

In the Datadog UI:

- **API Key**: Organization Settings > API Keys. Create a new key or use an existing one.
- **Application Key**: Organization Settings > Application Keys. Create a new key. The application key determines the permissions (scoped to the user who created it).

See [https://docs.datadoghq.com/account_management/api-app-keys/](https://docs.datadoghq.com/account_management/api-app-keys/) for details.
